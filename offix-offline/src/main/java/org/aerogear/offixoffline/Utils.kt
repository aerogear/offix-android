package org.aerogear.offixoffline

import android.content.Context
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.api.Operation

object Utils {
    private val context = Offline.getContextOffline()
    val sharedPreferences = context?.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)
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

    /*
    Function to set the url of the server to which call is made and save it in shared prefernces.
     */
    fun editSharedPref(string: String) {
        val editor = sharedPreferences?.edit()
        editor?.let {
            it.putString("url", string).apply()
        }
    }
}