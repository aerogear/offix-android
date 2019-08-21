package org.aerogear.offix.callbacks

import android.util.Log
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import org.aerogear.offix.Offline

/*Callbacks class which handles offline requests.
 */
class OfflineCallback(
    val request: ApolloInterceptor.InterceptorRequest,
    val callBack: ApolloInterceptor.CallBack
) : ApolloInterceptor.CallBack {
    private val TAG = javaClass.simpleName

    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {
        Log.d(TAG, "onResponse()")
        Log.d(TAG, "SIZE OF Request LIST in OfflineCallback ****: ${Offline.requestList.size}")
        callBack.onResponse(response)
        Offline.requestList.remove(request)
    }

    override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
        Log.d(TAG, "onFetch()---------")
    }

    override fun onCompleted() {
        Log.d(TAG, "onCompleted()--------")
    }

    override fun onFailure(e: ApolloException) {
        Log.d(TAG, "onFailure()----------")
        Log.d(TAG, "-- ${e.printStackTrace()}")
    }
}