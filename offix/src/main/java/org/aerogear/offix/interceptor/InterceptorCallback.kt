package org.aerogear.offix.interceptor

import android.util.Log
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import org.aerogear.offix.ConflictResolutionHandler
import org.aerogear.offix.responseWithConflicts
import java.nio.charset.Charset

class InterceptorCallback : ApolloInterceptor.CallBack {
    private val TAG = javaClass.simpleName

    override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {
        Log.d(TAG + 1, ConflictResolutionHandler().conflictPresent(response.parsedResponse).toString())

        if (ConflictResolutionHandler().conflictPresent(response.parsedResponse)) {

            Log.d(TAG + 2, response.parsedResponse.get().errors().toString())
            Log.d(TAG + 3, response.httpResponse.get().body().toString())
            Log.d(TAG + 4, ((response.parsedResponse.get().errors()[0] as Error).message()).toString())


            val responseBody = response.httpResponse.get().body()
            val bufferedSource = responseBody?.source()
            val buffer = bufferedSource?.buffer()
            val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""

            Log.d(TAG + 5, responseBodyString)

//            val conflictString = response.parsedResponse.get().errors().toString()
//            responseWithConflicts = conflictString
//
//            list = getServerClientData(responseWithConflicts)

            Log.d(TAG + 6, responseWithConflicts)
        }
        Log.d(TAG + 7, response.parsedResponse.toString())
        Log.d(TAG + 8, response.httpResponse.toString())
    }

    //First onFetch is called
    override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
        Log.d(TAG + 1, "Thread:[" + Thread.currentThread().id + "]: onCompleted()")
    }

    override fun onCompleted() {
        Log.d(TAG + 2, "Thread:[" + Thread.currentThread().id + "]: onCompleted()")
    }

    override fun onFailure(e: ApolloException) {
        Log.d(TAG + 3, "Thread:[" + Thread.currentThread().id + "]: onCompleted()")
    }
}