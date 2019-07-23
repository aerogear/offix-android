package org.aerogear.offix

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

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
        val apolloClient = OfflineList.apClient

        /*
          Create an object of ApolloCall.Callback
         */
        val apolloCallback = object : ApolloCall.Callback<Any>() {
            override fun onFailure(e: ApolloException) {
                Log.d("Extension Callback - ", "$e")
            }

            override fun onResponse(response: Response<Any>) {
                Log.d(TAG, response.data().toString())
            }
        }

        mutationList.forEachIndexed { index, currentMutation ->
            if (isNetworkAvail) {
                Log.d(TAG, "Network Connected :  $isNetworkAvail")
                apolloClient?.mutate(currentMutation)?.enqueue(apolloCallback)
            } else {
                Log.d(TAG, "Network Connected :  $isNetworkAvail")
            }
        }

        /*
         Remove mutations
        */
        mutationList.clear()


        /* Delete the mutations stored in the database as they have already been replicated to the server
           via the array list method having mutations and callbacks because the app was still in foreground.
           (This service starts when the app is in foreground, so no need to take the database approach here.)
        */
        Offline.getDb()?.mutationDao()?.deleteAllMutations()
        Log.d(TAG, " Size of database list : ${Offline.getDb()?.mutationDao()?.getAllMutations()?.size}")
    }
}

