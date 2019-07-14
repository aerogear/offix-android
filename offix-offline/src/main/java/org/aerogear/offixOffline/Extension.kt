package org.aerogear.offixOffline

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation

/*  Extension function on ApolloCall which will be used by the user while making a call request.

   @param mutation whose data will be stored in the database if network connection is not there.
   @param callback Callback which will handle the response or a failure exception.
 */
fun ApolloCall<Any>.offQueue(
    apolloClient: ApolloClient,
    mutation: com.apollographql.apollo.api.Mutation<Operation.Data, Operation.Data, Operation.Variables>,
    callback: ApolloCall.Callback<Any>
) {
    /*
      Set the apollo client made by the user.
     */
    if (Utils.apClient != null) {
        Utils.apClient = apolloClient
    }

    if (Offline.isNetwork()) {
        Log.d("Extension", " Network is there.")
        this.enqueue(callback)
    } else {
        Log.d("Extension", " Network not there.")

        /* If the user is offline: (Case 1: When the app is in foreground, i.e. in-memory)
           1. Store the mutation object and callback in an array-list.
         */
        Log.d("Extension", " mutation : ${mutation.variables().valueMap()}")
        Log.d("Extension", " callback : $callback")

        Utils.offlineArrayList.add(mutation)
        Utils.callbacksList.add(callback)
    }
}

/*
   Extension function on ApolloClient.Buidler which can be used by the user for creating a custom client.
   @return ApolloClient.Buidler
 */
fun ApolloClient.Builder.OfflineClientBuilder(): ApolloClient.Builder = this

/*
  Extension function for logging.
  */
fun Class<Any>.logd(message: String) {
    if (BuildConfig.DEBUG) Log.d(this.simpleName, message)
}

