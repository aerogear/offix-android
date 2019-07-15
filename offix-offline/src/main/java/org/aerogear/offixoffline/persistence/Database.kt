package org.aerogear.offixoffline.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

/*
Database persistence approach is used when the app is in background.
 */
@Database(entities = [Mutation::class], version = 4)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun mutationDao(): MutationDao

}