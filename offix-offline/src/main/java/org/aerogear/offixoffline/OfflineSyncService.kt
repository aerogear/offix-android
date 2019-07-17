package org.aerogear.offixoffline

import android.app.IntentService
import android.content.Intent
import android.util.Log

/* Start a service from broadcast receiver which hits mutation to the server.
 */
class OfflineSyncService : IntentService("OfflineService") {
    val TAG = javaClass.simpleName

    /*
     onCreate() called when service is started
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    /*
     Take the mutation from the array list one by one and make a call to the server.
     */
    override fun onHandleIntent(intent: Intent?) {
        val isNetworkAvail = Offline.isNetwork()

        val mutationList = OfflineList.getInstance().offlineArrayList
        val callbackList = OfflineList.getInstance().callbacksList
        val apolloClient = OfflineList.apClient

        mutationList.forEachIndexed { index, currentMutation ->
            if (isNetworkAvail) {
                Log.d(TAG, "Network Connected :  $isNetworkAvail")
                val currentCallback = callbackList[index]
                apolloClient?.mutate(currentMutation)?.enqueue(currentCallback)
            } else {
                Log.d(TAG, "Network Connected :  $isNetworkAvail")
            }

            /* Remove callbacks and mutations
            */
            mutationList.clear()
            callbackList.clear()
        }
    }
}

