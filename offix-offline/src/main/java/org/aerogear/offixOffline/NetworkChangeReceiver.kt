package org.aerogear.offixOffline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation


/*
 BroadcastReceiver that listens for connectivity change while tha app is in foreground (in-memory).
 In it's onReceive() method we check the network connection. If the user is connected to the net, then get access to the arraylist
 of mutations and callbacks, get the apollo client provided by the user and make a call to the server.
 */

class NetworkChangeReceiver : BroadcastReceiver() {

    val NETWORK_AVAILABLE_ACTION = "org.aerogear.offixsdk.NetworkAvailable"
    val ctx = Offline.getContextOffline()
    val TAG = javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {

        val bool = Offline.isNetwork()
        Log.d(TAG, "${intent?.data}")

        if (bool) {
            /*
             Active network connection is there.
             */
            Log.d(TAG, "Network connectivity change $bool")
            val mutationList = Utils.offlineArrayList
            val callbackList = Utils.callbacksList
            val apolloClient = Utils.getApolloClient()

            Log.d(TAG, "mutation list : ${mutationList.size}, callback list : ${callbackList.size}")

            mutationList.forEachIndexed { index, mutation ->
                val client = apolloClient?.mutate(mutation)
                AsyncTask.execute {
                    client?.enqueue(callbackList[index] as ApolloCall.Callback<Operation.Data>)
                }
            }
            Utils.offlineArrayList.clear()
            Utils.callbacksList.clear()
        } else {

        }
    }
}
