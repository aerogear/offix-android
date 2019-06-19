package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.apollographql.apollo.api.OperationName
import org.json.JSONObject


@Entity(tableName = "MutationOffline")
data class Mutation(
    var operationID: String,
    var queryDoc: String,
    var operationName: OperationName,
    var valuemap: JSONObject,
    @PrimaryKey(autoGenerate = true) var SNo: Int = 0

)