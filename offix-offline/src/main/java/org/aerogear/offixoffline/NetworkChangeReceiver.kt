package org.aerogear.offixoffline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/*
 BroadcastReceiver that listens for connectivity change while the app is in foreground (in-memory).
 In it's onReceive() method we check the network connection. If the user is connected to the net, then get access to the arraylist
 of mutations and callbacks, get the apollo client provided by the user and make a call to the server.
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    val ctx = Offline.getContextOffline()
    val TAG = javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        val isNetworkAvail = Offline.isNetwork()
        Log.d(TAG, "${intent?.data}")
        if (intent?.action == "android.net.conn.CONNECTIVITY_CHANGE") {
            if (isNetworkAvail) {
                Log.d(TAG, "Network connectivity change $isNetworkAvail")
                val mutationList = Utils.offlineArrayList
                val callbackList = Utils.callbacksList
                val apolloClient = Utils.apClient

                Log.d(TAG, "mutation list : ${mutationList.size}, callback list : ${callbackList.size}")
                mutationList.forEachIndexed { index, mutation ->
                    apolloClient?.mutate(mutation)?.enqueue(callbackList[index])
                }
            } else {
                Log.d(TAG, "Network connectivity change $isNetworkAvail")
            }
            /*
             Clear the array list of mutations and callbacks after the call to the server is made with them.
             */
            Utils.offlineArrayList.clear()
            Utils.callbacksList.clear()
        }
    }
}
