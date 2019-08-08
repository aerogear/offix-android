package org.aerogear.offix.interceptor

import android.util.Log
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import java.util.concurrent.Executor

class OffixInterceptor : ApolloInterceptor {

    private val TAG = javaClass.simpleName

    override fun interceptAsync(
        request: ApolloInterceptor.InterceptorRequest,
        chain: ApolloInterceptorChain,
        dispatcher: Executor,
        callBack: ApolloInterceptor.CallBack
    ) {
        //Check if this is a mutation request.
        if (request.operation !is Mutation) {
            //Not a mutation. Nothing to do here - move on to the next link in the chain.
            chain.proceedAsync(request, dispatcher, callBack)
            return
        }

        Log.d("$TAG 1", request.requestHeaders.headers().toString())

        chain.proceedAsync(request, dispatcher, OffixConflictCallback())
    }

    override fun dispose() {
        Log.v(TAG, "Dispose called")
    }
}
