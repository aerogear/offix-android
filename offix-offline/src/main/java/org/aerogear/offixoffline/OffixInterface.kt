package org.aerogear.offixoffline

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

/**
 OffixInterface is used by the users in making a callback object which is passed to the Offix enqueue() function.
 */
interface OffixInterface {
    /**
     * Called when the request is successfully executed and we get the response from the server.
     */
    fun onSuccess(response: Response<Any>)

    /**
     * Called when the request could not be executed due to cancellation, a connectivity problem or
     * timeout.
     */
    fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>)
}