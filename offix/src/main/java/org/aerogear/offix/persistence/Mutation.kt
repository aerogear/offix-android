package org.aerogear.offix.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.apollographql.apollo.api.OperationName
import org.json.JSONObject

/*
persistent offline mutation object
 */
@Entity(tableName = "mutationOffline")
data class Mutation(
    var operationId: String,
    var queryDoc: String,
    var operationName: OperationName,
    var valueMap: JSONObject,
    var responseClassName: String,
    @PrimaryKey(autoGenerate = true) var sNo: Int = 0
)