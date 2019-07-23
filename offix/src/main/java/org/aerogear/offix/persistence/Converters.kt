package org.aerogear.offix.persistence

import android.arch.persistence.room.TypeConverter
import com.apollographql.apollo.api.OperationName
import org.json.JSONObject

/*
Converters are used to serialise and de-serialise objects for storing and fetching from database.
 */
class Converters {

    @TypeConverter
    fun jsonToString(jsonObject: JSONObject): String {
        return jsonObject.toString()
    }

    @TypeConverter
    fun OperationNameToString(name: OperationName): String {
        return name.name()
    }

    @TypeConverter
    fun StringToJson(string: String): JSONObject {
        val jsonObject = JSONObject(string)
        return jsonObject
    }

    @TypeConverter
    fun StringToOperName(string: String): OperationName {
        val operationName = OperationName {
            string
        }
        return operationName
    }
}