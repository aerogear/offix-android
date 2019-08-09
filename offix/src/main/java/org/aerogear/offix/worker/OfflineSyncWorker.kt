package org.aerogear.offix.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.offix.Offline
import org.aerogear.offix.getMutation

/* Start a worker from broadcast receiver which hits mutation to the server when in foreground.
 */
class OfflineSyncWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    val TAG = javaClass.simpleName

    /* Get access to the mutationdao of the database.
     */
    val mutationDao = Offline.getDb()?.mutationDao()

    override fun doWork(): Result {
        /* Get access to the list of mutations stored in the database.
        */
        val mutationList = mutationDao?.getAllMutations()
        val apolloClient = Offline.apClient

        /*
         Create an object of ApolloCall.Callback
        */
        val apolloCallback = object : ApolloCall.Callback<Operation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.d("Extension Callback - ", "$e")
            }

            override fun onResponse(response: Response<Operation.Data>) {
                Log.d(TAG, response.data().toString())
            }
        }

        Log.d(TAG, " Size of database list ** : ${mutationDao?.getAllMutations()?.size}")

        mutationList?.forEach { storedmutation ->

            if (Offline.isNetwork()) {
                /*
                 Create an object of Mutation<D,T,V> from the stored mutation in the database.
                 */
                val obj = getMutation(storedmutation)

                /* Make an apollo call which takes in mutation object and makes a call to server.
                */
                val apollcall = apolloClient?.mutate(obj)

                apollcall?.enqueue(apolloCallback)

                /*
                 Remove processed mutation.
                */
                mutationDao?.deleteMutation(storedmutation)
            }
        }
        return if (mutationDao?.getAllMutations()?.isEmpty()!!) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}