package org.aerogear.offix.callbacks

import android.util.Log
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import org.aerogear.offix.ConflictResolutionHandler
import org.aerogear.offix.conflictedMutationClass
import org.aerogear.offix.interfaces.ConflictResolutionInterface

/*Callback class which handles conflicts.
 */
class OffixConflictCallback(
    val conflictResolutionImpl: ConflictResolutionInterface,
    val callBack: ApolloInterceptor.CallBack
) :
    ApolloInterceptor.CallBack {
    private val TAG = javaClass.simpleName

    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {
        Log.d(TAG, "OffixConflictCallback : response ****** ${response}")

        /* Check if the conflict is present in the response of not using the ConflictResolutionHandler class.
         */
        if (ConflictResolutionHandler().conflictPresent(response.parsedResponse)) {

            Log.d("$TAG 100", "**********")
            /* Parse the response from the server into a Map object and extract the serverState and clientState.
               Make an object of ServerClientData and add to the list.
            */
            val conflictInfo =
                (((response.parsedResponse.get().errors()[0] as Error).customAttributes()["extensions"] as Map<*, *>)["exception"] as Map<*, *>)["conflictInfo"] as Map<*, *>

            val serverStateMap = conflictInfo["serverState"] as Map<String, Any>
            val clientStateMap = conflictInfo["clientState"] as Map<String, Any>

            Log.e("serverStateMap", "${serverStateMap.entries}")
            Log.e("clientStateMap", "${clientStateMap.entries}")

            conflictResolutionImpl.resolveConflict(serverStateMap, clientStateMap, conflictedMutationClass)
        } else {
            callBack.onResponse(response)
        }
    }

    override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
        Log.d(TAG, "onFetch()*******")
    }

    override fun onCompleted() {
        Log.d(TAG, "onCompleted()*******")
    }

    override fun onFailure(e: ApolloException) {
        callBack.onFailure(e)
        Log.d(TAG, "onFailure()*******")
    }
}