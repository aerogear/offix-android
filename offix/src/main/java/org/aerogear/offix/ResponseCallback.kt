package org.aerogear.offix

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

/**
ResponseCallback is used by the users in making a callback object which is passed to the Offix enqueue() function.
 */
interface ResponseCallback {
    /**
     * Called when the request is successfully executed and we get the response from the server.
     */
    fun onSuccess(response: Response<Any>)

    /**
     * Called when the request could not be executed due to cancellation, a connectivity problem or
     * timeout.
     */
    fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>)

    /*
     Called when the library detects error from the server upon Conflict detection.
     @param serverState: String
     @param clientState: String
     @return type:  Mutation<Operation.Data, Any, Operation.Variables>
     */
    fun onConflictDetected(serverState: String, clientState: String): Mutation<Operation.Data, Any, Operation.Variables>?
}