package org.aerogear.offixoffline.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.apollographql.apollo.api.OperationName
import org.json.JSONObject

/*
persistent offline mutation object
 */
@Entity(tableName = "MutationOffline")
data class Mutation(
    var operationID: String,
    var queryDoc: String,
    var operationName: OperationName,
    var valueMap: JSONObject,
    var responseClassName: String,
    @PrimaryKey(autoGenerate = true) var sNo: Int = 0
)