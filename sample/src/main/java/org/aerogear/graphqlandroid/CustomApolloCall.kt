package org.aerogear.graphqlandroid

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

interface CustomApolloCall : ApolloCall<Void> {

    override fun enqueue(callback: ApolloCall.Callback<Void>?)

    abstract class CustomCallback : ApolloCall.Callback<Void>() {

        abstract override fun onResponse(response: Response<Void>)

        abstract override fun onFailure(e: ApolloException)
    }
}