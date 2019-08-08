package org.aerogear.offix.interceptor

import android.util.Log
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import org.aerogear.offix.*
import org.json.JSONObject
import com.google.gson.Gson

/*
OffixConflictCallback
 */
class OffixConflictCallback : ApolloInterceptor.CallBack {
    private val TAG = javaClass.simpleName

    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {

        /* Check if the conflict is present in the response of not using the ConflictResolutionHandler class.
         */
        if (ConflictResolutionHandler().conflictPresent(response.parsedResponse)) {

            /* Parse the response into a json object from the server and extract the serverState and clientState.
               Make an object of ServerClientData and add to the list.
            */
            val extensions = JSONObject(
                ((response.parsedResponse.get().errors()[0] as Error).customAttributes()["extensions"] as Map<*, *>)["exception"] as Map<*, *>
            )

            val exception = extensions.optJSONObject("conflictInfo")
            val serverState = exception.optJSONObject("serverState")
            val clientState = exception.optJSONObject("clientState")

            val serverDataMap = Gson().fromJson(serverState.toString(), HashMap::class.java)
            val clientDataMap = Gson().fromJson(clientState.toString(), HashMap::class.java)

            Log.d("$TAG 3", serverState.toString())
            Log.d("$TAG 3", serverDataMap.toString())
            Log.d("$TAG 4", clientState.toString())

            val serverClientData = ServerClientData(serverDataMap, clientDataMap)
            scData = serverClientData
        }
    }

    override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
        Log.d(TAG + 1, "Thread:[" + Thread.currentThread().id + "]:onFetch()")
    }

    override fun onCompleted() {
        Log.d(TAG + 2, "Thread:[" + Thread.currentThread().id + "]: onCompleted()")
    }

    override fun onFailure(e: ApolloException) {
        Log.d(TAG + 3, "Thread:[" + Thread.currentThread().id + "]: onFailure()")
    }
}