package org.aerogear.offix

import android.util.Log
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.internal.Optional
import org.json.JSONException
import org.json.JSONObject

/*
ConflictResolutionHandler class helps in detecting the conflicts present in the server response.
 */
class ConflictResolutionHandler {

    private val TAG = javaClass.simpleName

    fun conflictPresent(responseString: String?): Boolean {
        try {
            if (responseString == null) {
                return false
            }
            val jsonObject = JSONObject(responseString)
            val errorArray = jsonObject.optJSONArray("errors")
            if (errorArray == null || errorArray.length() < 1) {
                return false
            }
            if (responseString.contains("conflictInfo")) {
                Log.d("ConflictInterceptor", " ******")
                return true
            }
        } catch (e: JSONException) {
            Log.d(TAG, "Inside catch statement in ConflictResolutionHandler")
        }
        return false
    }

    internal fun conflictPresent(parsedResponse: Optional<Response<Any>>?): Boolean {
        Log.d(TAG, "conflictPresent parsed response")

        /*Check if the parsed response contains a conflict.
        The contract for conflicts is that the response will contain an error with the
        string "conflictInfo".
        */
        if (parsedResponse == null || parsedResponse.get() == null || parsedResponse.get().hasErrors() == false) {
            return false
        }

        Log.d(TAG, "Thread:[" + Thread.currentThread().id + "]: onResponse -- found error")

        if (!parsedResponse.get().errors()[0].toString().contains("conflictInfo")) {
            return false
        }
      return true
    }

}