package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.apollographql.apollo.api.OperationName
import org.json.JSONObject


@Entity(tableName = "MutationOffline")
data class Mutation(
    @PrimaryKey(autoGenerate = true)
    val SNo: Int = 0,
    val operationID: String,
    val queryDoc: String,
    val operationName: OperationName,
    val valuemap: JSONObject
)