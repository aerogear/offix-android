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
import android.util.Log

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

    /**
     * Callback that's invoked every time a new activity's lifecycle method was called
     */
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        /**
         * Called whenever an activity's onCreate method was called, automatically sends a pageView call to the server
         */
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity?) {
            /*
            Register the broadcast receiver which listens to the connectivity changes.
             */
            Log.d(TAG, "Offline Library, onActivityStarted")
            val filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            context.registerReceiver(br, filter)
        }

        override fun onActivityResumed(activity: Activity?) = Unit

        override fun onActivityPaused(activity: Activity?) = Unit

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit

        /**
         * When an activity was stopped, fire a worker that tries the requests stored in the database
         */
        override fun onActivityStopped(activity: Activity?) {
            Log.d(TAG, "Offline Library, onActivityStopped")
            context.unregisterReceiver(br)
        }

        override fun onActivityDestroyed(activity: Activity?) = Unit
    }

    fun start() {
        //TODO This database approach will be used when solving for background.
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
            Log.d(TAG, "isNetwork() of Offline class called")
            val connectivityManager =
                offline?.ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}