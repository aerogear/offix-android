package org.aerogear.offix

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation

/* This class helps in getting access to the instance of apollo client provided by the user.
   Also, in future the static objects can be placed here.
 */
class OfflineList {

    companion object {
        var apClient: ApolloClient? = null
        private var instance: OfflineList? = null

        /* Returns an existing instance of the OfflineList class if it exists, else returns a new instance
         */
        @JvmStatic
        fun getInstance(): OfflineList {
            return instance?.let {
                instance
            } ?: run {
                instance = OfflineList()
                instance!!
            }
        }
    }
}