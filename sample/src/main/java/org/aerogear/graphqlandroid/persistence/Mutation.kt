package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "MutationOffline")
data class Mutation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mutation: String
)