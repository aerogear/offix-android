package org.aerogear.offix.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

/*
Database persistence approach is used when the app is in background.
 */
@Database(entities = [Mutation::class], version = 4)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    /*
    Dao object to access methods of the database.
     */
    abstract fun mutationDao(): MutationDao

}