package org.aerogear.offixsdk

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation
import org.aerogear.offixsdk.persistence.Mutation
import org.json.JSONObject

val application = Application

/*  Extension function on ApolloCall which will be used by the user while making a call request.

   @param mutation whose data will be stored in the database if network connection is not there.
   @param callback Callback which will handle the response or a failure exception.
 */

fun ApolloCall<Any>.offQueue(
    mutation: com.apollographql.apollo.api.Mutation<Operation.Data, Operation.Data, Operation.Variables>,
    callback: ApolloCall.Callback<Any>
) {

    if (isNetwork(getContext())) {

        Log.e("Extension", " Network is there.")
        this.enqueue(callback)

    } else {

        Log.e("Extension", " Network not there.")

        /* If the user is offline:
           1. Store the object in database.
           2. Schedule a work manager to process that request in future. (This is scheduled from LoggingInterceptor class)
        */

        val operationID = this.operation().operationId()
        val queryDoc = this.operation().queryDocument()
        val operationName = this.operation().name()
        val valuemap = this.operation().variables().valueMap()
        val jsonObject = JSONObject(valuemap)
        val responseClassName = mutation.javaClass.name
        val mutationObj = Mutation(operationID, queryDoc, operationName, jsonObject, responseClassName)

        application.database.mutationDao().insertMutation(mutationObj)

    }
}

