package org.aerogear.offix

import android.util.Log
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor

class OfflineCallback(val request: ApolloInterceptor.InterceptorRequest) : ApolloInterceptor.CallBack {
    val TAG = javaClass.simpleName
    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {
        Log.d(TAG, "onResponse()")
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