package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Mutation::class), version = 1)
abstract class Database : RoomDatabase() {

    abstract fun mutationDao(): MutationDao

}