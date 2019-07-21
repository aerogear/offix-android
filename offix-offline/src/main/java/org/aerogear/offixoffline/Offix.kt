@file:JvmName("Offix")

package org.aerogear.offixoffline

import android.util.Log
import androidx.work.*
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/* Extension function on ApolloClient which will be used by the user while making a call request.
   @receiver parameter is ApolloClient on which the call will be made by the user.
   @param mutation which will be stored in the list if network connection is not there.
   @param callback which will be stored in the list if network connection is not there.
 */
fun ApolloClient.enqueue(
    mutation: Mutation<Operation.Data, Any, Operation.Variables>,
    callback: ApolloCall.Callback<Any>
) {
    /* Set apollo client given by the user.
     */
    OfflineList.apClient = this

    /* Check is the network is available or not.
     */
    if (Offline.isNetwork()) {
        Log.d("Extension", " Network connected")
        this.mutate(mutation).enqueue(callback)
    } else {
        Log.d("Extension", "Network not connected")

        /* If the user is offline:
           (For Case 1: When the app is in foreground, i.e. in-memory)
           1. Store the mutation object and callback in an array-list.
         */
        Log.d("Extension", " mutation : ${mutation.variables().valueMap()}")
        OfflineList.getInstance().offlineArrayList.add(mutation)
        OfflineList.getInstance().callbacksList.add(callback)

        /* If the user is offline:
          (For Case 2: When the app is in background, we will scheduleWorker a worker to replicate the mutations stored in database to the server)
           1. Make an object of mutation persistence class.
           2.Store it in database
         */

        /* Get access to the dao of the database
         */
        val libDao = Offline.getDb()?.mutationDao()

        val operationId = mutation.operationId()
        val operationDoc = mutation.queryDocument()
        val operationName = mutation.name()
        val valMap = mutation.variables().valueMap()
        val jsonObject = JSONObject(valMap)
        val responseClassName = mutation.javaClass.name

        /* Make an object of Mutation persistence class
         */
        val mutationDbObj = org.aerogear.offixoffline.persistence.Mutation(
            operationId,
            operationDoc,
            operationName,
            jsonObject,
            responseClassName
        )

        /* Insert mutation object in the database.
        */
        libDao?.insertMutation(mutationDbObj)
        Log.d("Extension", " serial number: ${libDao?.getAllMutations()?.get(0)?.sNo}")
        Log.d("Extension", " values of mutation: ${libDao?.getAllMutations()?.get(0)?.valueMap}")
        Log.d("Extension", " size of db list after inserting mutations: ${libDao?.getAllMutations()?.size}")
    }
}

/* scheduleWorker() takes in a worker class and schedule a work manager to replicate all the
   mutations stored in database to the server when the app is in background, i.e when the app is closed.
   @param Class of type worker
   @return unit
 */
fun <T : Worker> scheduleWorker(workerClass: Class<T>){
    Log.d("Extension sWorker", "Inside worker")

    /* Set the constraints to check the network connection.
     */
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /* Create an object of oneTimeWorkRequest.
    */
    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(workerClass)
        .setConstraints(constraints)
        .build()

    /* Get an instance of WorkManager and pass the oneTimeWorkRequest to it.
    */
    WorkManager.getInstance().enqueue(oneTimeWorkRequest)
}

/*  Extension function for ApolloClient Builder
    @receiver param: ApolloClient.Buidler, which can be used by the user for creating a custom client.
    @return ApolloClient.Buidler
 */
fun ApolloClient.Builder.OfflineClientBuilder(): ApolloClient.Builder = this


