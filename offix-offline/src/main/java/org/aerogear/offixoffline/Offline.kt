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
     * Event database is initialized and stored once
     */
    private var libdb: org.aerogear.offixoffline.persistence.Database? = null

    /*
    sharedPref for storing url of the server (will be used when app is in background)
     */
    val sharedPreferences by lazy {
        ctx.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)
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
            Log.d(TAG, "Offline Library, onActivityStarted")
        }

        override fun onActivityResumed(activity: Activity?) = Unit

        override fun onActivityPaused(activity: Activity?) = Unit

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit

        /**
         * When an activity was stopped, fire a worker that tries the requests stored in the database
         */
        override fun onActivityStopped(activity: Activity?) {
            Log.d(TAG, "Offline Library, onActivityStopped")
        }

        override fun onActivityDestroyed(activity: Activity?) = Unit
    }

    /*
      Objects in this function will be initialised when the application using the library starts, i.e. in content provider.
     */
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

        /*
         Function to set the url of the server to which call is made and save it in shared prefernces.
         */
        fun editSharedPref(string: String) {
            val editor = getContextOffline()?.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)?.edit()
            editor?.let {
                it.putString("url", string).apply()
            }
        }
    }
}