package org.aerogear.graphqlandroid.worker

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.graphqlandroid.Utils
import org.aerogear.offix.worker.OffixWorker
import org.aerogear.offix.Offline
import org.aerogear.offix.getMutation

/*
 This class extends the OffixWorker class of the Offix-Offline library.
 @params context and workParameters
 */
class SampleWorker(context: Context, workParameters: WorkerParameters) : OffixWorker(context, workParameters) {

    private val TAG = javaClass.simpleName
    val ctx = context

    /*
     In doWork(), we get access to list of mutations stored in database.
     Iterate over the list and make a call to the server by accessing the mutations one by one.
     */
    override fun doWork(): Result {
        Log.d(TAG, "In Worker")

        /*
         getListOfMutations() returns the list of mutations stored in database in the library.
         It's present in the parent class, i.e OffixWorker.
         */
        val listOfMutations = getListOfMutations()
        Log.d(TAG, "Size of list----- : ${listOfMutations.size}")

        /*
         Check again if the network connection is there or not.
         */
        if (Offline.isNetwork()) {
            //get the mutation one by one from database
            listOfMutations.forEach { storedmutation ->

                Log.d(TAG, "Current mutation serial number is: ${storedmutation.sNo}")

                /*
                 Create an object of Mutation<D,T,V> from the stored mutation in the database.
                 */
                val obj = getMutation(storedmutation)

                /* Make an apollo client which takes in mutation object and makes a call to server.
                When the app is in background, then we can't access to the client made by the user. In this scenario, we have to make a
                custom minimal client to make the server call in the background.
                */
                val customClient = Utils.getApolloClient(ctx)?.mutate(
                    obj
                )

                customClient?.enqueue(object : ApolloCall.Callback<Operation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                    }

                    /*
                     On getting a successful response back from the server, delete mutation from the database.
                     */
                    override fun onResponse(response: Response<Operation.Data>) {
                        Log.e(TAG, response.hasErrors().toString())
                        Log.e(TAG, response.data().toString())
                        Log.e(TAG, response.errors().toString())
                        deleteMutation(storedmutation)
                    }
                })
            }
        }
        Log.d(TAG, "Size of list****** : ${listOfMutations.size}")
        return Result.success()
    }
}

