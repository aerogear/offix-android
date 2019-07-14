package org.aerogear.offixOffline

import android.content.Context
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation

object Utils {

    private val context = Offline.getContextOffline()
    val sharedPreferences = context?.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)
    var apClient: ApolloClient? = null

    val offlineArrayList =
        arrayListOf<com.apollographql.apollo.api.Mutation<Operation.Data, Operation.Data, Operation.Variables>>()

    val callbacksList = arrayListOf<ApolloCall.Callback<Any>>()

    fun editSharedPref(string: String) {
        val editor = sharedPreferences?.edit()
        editor?.let {
            it.putString("url", string).apply()
        }
    }

    fun getApolloClient() = apClient
}