package org.aerogear.graphqlandroid

import android.app.Application
import android.arch.persistence.room.Room
import android.util.Log
import org.aerogear.graphqlandroid.persistence.Database

class MyApplciation : Application() {

    companion object {
        lateinit var database: Database
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, Database::class.java, "myDatabase.db").build()

        Log.e("TAG", "Application was started")

    }
}