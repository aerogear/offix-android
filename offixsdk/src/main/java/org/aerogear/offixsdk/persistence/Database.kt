package org.aerogear.offixsdk.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [Mutation::class, Url::class], version = 2)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun mutationDao(): MutationDao

    abstract fun urlDao(): UrlDao

}