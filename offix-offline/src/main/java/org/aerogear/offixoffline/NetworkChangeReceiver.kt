package org.aerogear.offixoffline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/*
 BroadcastReceiver that listens for connectivity change while the app is in foreground (in-memory).
 In it's onReceive() method we check the network connection. If the user is connected to the net, then get access to the arraylist
 of mutations and callbacks, get the apollo client provided by the user and make a call to the server.
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    val ctx = Offline.getContextOffline()
    val TAG: String = javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        /*
        Offload the work to an IntentService that syncs the mutations with the server only when list is not empty.
         */
        if (intent?.action == "android.net.conn.CONNECTIVITY_CHANGE" &&
            OfflineList.getInstance().offlineArrayList.isNotEmpty()
        ) {
            val foregroundService = Intent(context, OfflineSyncService::class.java)
            context?.startService(foregroundService)
        }
    }
}
