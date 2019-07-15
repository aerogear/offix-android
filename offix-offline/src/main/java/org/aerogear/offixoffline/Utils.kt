package org.aerogear.offixoffline

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation

object Utils {
    var apClient: ApolloClient? = null

    /*
    Array List to store mutation objects.
     */
    val offlineArrayList =
        arrayListOf<com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>>()

    /*
    Array List to store callbacks objects.
     */
    val callbacksList = arrayListOf<ApolloCall.Callback<Any>>()
}