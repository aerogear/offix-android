package org.aerogear.offix.interceptor

import android.util.Log
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import org.aerogear.offix.ConflictResolutionHandler
import org.aerogear.offix.Offline
import org.aerogear.offix.callbacks.OffixConflictCallback
import org.aerogear.offix.callbacks.OfflineCallback
import org.aerogear.offix.conflictedMutationClass
import org.aerogear.offix.interfaces.ConflictResolutionInterface
import java.util.*
import java.util.concurrent.Executor

/*
User adds ConflictInterceptor to the apolloClient while making it in the app.
@param conflictResolutionImpl: ConflictResolutionInterface where the user can provide custom conflict resolution strategy.
 */
class ConflictInterceptor(private val conflictResolutionImpl: ConflictResolutionInterface) : ApolloInterceptor {

    private val TAG = javaClass.simpleName

    /* Implemented queue using a linked list to store user callbacks.
       This is done to ensure that there is no overlapping of subsequent callbacks and every callback is associated with
       it's correct response.
     */
    val queueCallback = LinkedList<ApolloInterceptor.CallBack>()

    override fun interceptAsync(
        request: ApolloInterceptor.InterceptorRequest,
        chain: ApolloInterceptorChain,
        dispatcher: Executor,
        callBack: ApolloInterceptor.CallBack
    ) {
        Log.d("$TAG 1", "${request.operation}")

        /* Check is the network connection is there or not.
         */
        if (!Offline.isNetwork()) {
            /* Add the request to the list only when it's of type mutation.
               Responses to the requests containing query would be fetched from cache.
             */
            if (request.operation is Mutation) {
                Offline.requestList.add(request)
                queueCallback.add(callBack)
            }
            Log.d(TAG, "SIZE OF Request LIST : ${Offline.requestList.size}")
        } else {
            //Check if this is a mutation request.
            if (request.operation !is Mutation) {
                //Not a mutation. Nothing to do here - move on to the next link in the chain.
                chain.proceedAsync(request, dispatcher, callBack)
                return
            }

            /*
             Flow of code coming to this region depicts that network connection is there and
             the request is of Mutation type.
             */

            /* requestList is not empty means that it must be containing some mutation requests which were stored in it
               when the user was offline.
             */
            if (Offline.requestList.isNotEmpty()) {
                chain.proceedAsync(request, dispatcher, OffixConflictCallback(conflictResolutionImpl, callBack))
                Log.d("$TAG 100", "Net comes and requestList is not empty: ${Offline.requestList.size}")
                /* When user comes from offline to online, we wait for the user to perform any mutation.
                   Now along with the mutation performed by the user, we fetch the mutation requests stored in the list and
                   replicate them back to the server.
                   We are not providing UI bindings to the user currently.
                 */

                Offline.requestList.forEach {
                    Log.d("$TAG", "size %% ${queueCallback.size}")
                    if (queueCallback.isNotEmpty()) {
                        chain.proceedAsync(it, dispatcher, OfflineCallback(it, queueCallback.removeFirst()))
                    }
                    Log.d("$TAG", "size %% %%  ${queueCallback.size}")
                }
            } else {
                Log.d("$TAG 200", "--------")
                chain.proceedAsync(request, dispatcher, OffixConflictCallback(conflictResolutionImpl, callBack))
            }
        }
    }

    override fun dispose() {
        Log.v(TAG, "Dispose called")
    }
}