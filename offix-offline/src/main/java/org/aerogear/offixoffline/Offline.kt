package org.aerogear.offixoffline

import android.app.Activity
import android.app.Application
import android.arch.persistence.room.Room
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.aerogear.offixoffline.persistence.Mutation
import org.aerogear.offixoffline.worker.OfflineMutationsWorker
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class Offline private constructor(context: Context) {

    internal val ctx by lazy {
        context
    }

    private val packageManager by lazy {
        ctx.packageManager
    }

    /*
    Register a broadcast receiver for listening to network status while the app is foreground
     */
    private val br: BroadcastReceiver = NetworkChangeReceiver()

    /**
     * Package appName with the app
     */
    private val applicationId by lazy {
        ctx.packageName
    }

    /**
     * Event database is initialized and stored once
     */
    private var libdb: org.aerogear.offixoffline.persistence.Database? = null

    /**
     * Return the appName with the app by using the context provided by the content provider
     */
    private val applicationName by lazy {
        packageManager
            .getApplicationLabel(
                packageManager
                    .getApplicationInfo(
                        applicationId,
                        PackageManager.GET_META_DATA
                    )
            ).toString()
    }

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val periodicWorker = PeriodicWorkRequestBuilder<OfflineMutationsWorker>(
        15,
        TimeUnit.MINUTES
    ).setConstraints(constraints).build()

    /**
     * Callback that's invoked every time a new activity's lifecycle method was called
     */
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        /**
         * Called whenever an activity's onCreate method was called, automatically sends a pageView call to the server
         */
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity?) {
            val filter = IntentFilter(NetworkChangeReceiver().NETWORK_AVAILABLE_ACTION)
            LocalBroadcastManager.getInstance(ctx).registerReceiver(br, filter)
        }

        override fun onActivityResumed(activity: Activity?) = Unit

        override fun onActivityPaused(activity: Activity?) = Unit

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit

        /**
         * When an activity was stopped, fire a worker that tries the requests stored in the database
         */
        override fun onActivityStopped(activity: Activity?) {

            /*
            When onActivityStopped() is called, then the app is in background :
            1. We get access to the array list of mutations and apollo call.
            2.We extract the data from the mutation and store it in database.
            3.We fire a worker to make the call to the server for the requests stored in database.
             */

            val mutationList = Utils.offlineArrayList
            val callbackList = Utils.callbacksList
            val apolloClient = Utils.getApolloClient()

            Log.d(TAG, "mutation list : ${mutationList.size}, callback list : ${callbackList.size}")

            if (mutationList.size != 0) {

                mutationList.forEachIndexed { index, mutation ->

                    val client = apolloClient?.mutate(mutation)
                    val clientOp = client?.operation()
                    val responseClassName = mutation.javaClass.name

                    clientOp?.let {

                        val operationID = it.operationId()
                        val queryDoc = it.queryDocument()
                        val operationName = it.name()
                        val valuemap = it.variables().valueMap()
                        val jsonObject = JSONObject(valuemap)
                        val mutationObj = Mutation(operationID, queryDoc, operationName, jsonObject, responseClassName)
                        libdb?.mutationDao()?.insertMutation(mutationObj)
                    }
                }
            }

            /* If the user is offline:
               1. Store the object in database.
               2. Schedule a work manager to process that request in future. Execute a periodic work Request with the set constraints
             */
            WorkManager
                .getInstance()
                .enqueue(
                    periodicWorker
                )

            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(br)
        }

        override fun onActivityDestroyed(activity: Activity?) {
        }
    }

    fun start() {
        (ctx.applicationContext as Application).registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        libdb = Room.databaseBuilder(
            ctx,
            org.aerogear.offixoffline.persistence.Database::class.java, "offline_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Common params that wil be constant across all instances with the Offline object
     */
    companion object {

        private var offline: Offline? = null

        /**
         * Returns an existing instance with the Offline class if it exists, else returns a new instance
         */
        @JvmStatic
        fun with(context: Context): Offline {
            if (offline == null) {
                offline = Offline(context)
            }
            return offline!!
        }

        /**
         * Logcat tag for debugging purpose
         */
        private val TAG = javaClass::class.java.simpleName

        fun getContextOffline() = offline?.ctx

        /*
         Function to check the network connectivity.
         */
        internal fun isNetwork(): Boolean {
            val connectivityManager =
                offline?.ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    }
}