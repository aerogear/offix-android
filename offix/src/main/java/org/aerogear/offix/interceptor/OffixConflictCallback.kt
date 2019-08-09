package org.aerogear.offix.interceptor

import android.util.Log
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import org.aerogear.offix.*

/*
OffixConflictCallback
 */
class OffixConflictCallback : ApolloInterceptor.CallBack {
    private val TAG = javaClass.simpleName

    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {

        /* Check if the conflict is present in the response of not using the ConflictResolutionHandler class.
         */
        if (ConflictResolutionHandler().conflictPresent(response.parsedResponse)) {

            /* Parse the response from the server into a Map object and extract the serverState and clientState.
               Make an object of ServerClientData and add to the list.
            */
            val conflictInfo =
                (((response.parsedResponse.get().errors()[0] as Error).customAttributes()["extensions"] as Map<*, *>)["exception"] as Map<*, *>)["conflictInfo"] as Map<*, *>

            val serverStateMap = conflictInfo["serverState"] as Map<*, *>
            val clientStateMap = conflictInfo["clientState"] as Map<*, *>

            val serverClientData = ServerClientData(serverStateMap, clientStateMap)
            scData = serverClientData
        }
    }

    override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
        Log.d(TAG, "onFetch()")
    }

    override fun onCompleted() {
        Log.d(TAG, "onCompleted()")
    }

    override fun onFailure(e: ApolloException) {
        Log.d(TAG, "onFailure()")
    }
}