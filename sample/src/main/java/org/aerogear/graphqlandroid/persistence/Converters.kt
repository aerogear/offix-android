package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.TypeConverter
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun jsonToString(jsonObject: JSONObject) : String {
        return jsonObject.toString()
    }
}