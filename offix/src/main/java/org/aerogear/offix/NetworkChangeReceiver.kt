package org.aerogear.offix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

/*
 BroadcastReceiver that listens for connectivity change while the app is in foreground (in-memory).
 In it's onReceive() method we check the network connection. If the user is connected to the net, then start a worker which will process
 all the stored mutations to the server.
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    val ctx = Offline.getContextOffline()
    val TAG: String = javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        /*
        Offload the work to an IntentService that syncs the mutations with the server only when list is not empty.
         */
        if (intent?.action == "android.net.conn.CONNECTIVITY_CHANGE" &&
            Offline.getDb()?.mutationDao()?.getAllMutations()?.isNotEmpty()!!
        ) {
            /* Set the constraints to check the network connection.
    */
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            /* Create an object of oneTimeWorkRequest.
            */
            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(OfflineSyncWorker::class.java)
                .setConstraints(constraints)
                .build()

            /* Get an instance of WorkManager and pass the oneTimeWorkRequest to it.
            */
            WorkManager.getInstance().enqueue(oneTimeWorkRequest)
        }
    }
}
