package org.aerogear.offixsdk

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import org.aerogear.offixsdk.persistence.Database

class Application : Application() {

    companion object {
        lateinit var database: Database
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, Database::class.java, "libb_db.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

        Log.e("TAG", "Library Application was started")

    }
}

fun getContext() = Application().applicationContext

/**
 * Checks to see if the device is connected to the network
 * @return true if the device is connected to a network else false
 * @param context Application or activity context
 */
internal fun isNetwork(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

