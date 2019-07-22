@file:JvmName("Offix")

package org.aerogear.offixoffline

import android.util.Log
import androidx.work.*
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
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
        val libDao = getDao()

        /* Insert mutation object in the database.
        */
        libDao?.insertMutation(getPersistenceMutation(mutation))
        Log.d("Extension", " serial number: ${libDao?.getAllMutations()?.get(0)?.sNo}")
        Log.d("Extension", " values of mutation: ${libDao?.getAllMutations()?.get(0)?.valueMap}")
        Log.d("Extension", " size of db list after inserting mutations: ${libDao?.getAllMutations()?.size}")
    }
}

/*
 This function takes in an object of Mutation<D,T,V> and returns an object of Mutation persistence.
 */
fun getPersistenceMutation(mutation: Mutation<Operation.Data, Any, Operation.Variables>): org.aerogear.offixoffline.persistence.Mutation {

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
    return mutationDbObj
}

/* scheduleWorker() takes in a worker class and schedule a work manager to replicate all the
   mutations stored in database to the server when the app is in background, i.e when the app is closed.
   @param Class of type worker
   @return unit
 */
fun <T : Worker> scheduleWorker(workerClass: Class<T>) {
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

/*
 @param storedmutation : (org.aerogear.offixoffline.persistence)Mutation
 @return Mutation<Operation.Data, Operation.Data, Operation.Variables>
 */
fun getMutation(storedmutation: org.aerogear.offixoffline.persistence.Mutation): Mutation<Operation.Data, Operation.Data, Operation.Variables> {

    //Get the class name of the mutation to which it has to be mapped
    val responseClassName = storedmutation.responseClassName

    val classReflected: Class<*> = Class.forName(responseClassName)

    //Get the constructor of the class, and as apollo generated classes have only one constructor, so take the first one.
    val constructor = classReflected.constructors.first()

    //Get the parameterTypes
    val parameters = constructor.parameterTypes
    val jsonValues = arrayListOf<Any>()

    //Get the json object i.e the variables map given as input by the user
    val jsonObj = storedmutation.valueMap

    //Put all the json values into a list
    val iter = jsonObj.keys()
    iter.forEach { key ->
        jsonValues.add(jsonObj.get(key))
    }

    Log.d("jsonValuesList ", " ${jsonValues.size}")

    //Check if the input parameter is of type Input<*>, if yes typecast it to be of the type Input<*>
    parameters.forEachIndexed { index, clazz ->

        Log.e("parameters : ", " ${clazz.name}")
        if (inputTypeChecker(clazz.name)) {
            jsonValues[index] = Input.optional(jsonValues[index])
            Log.e("parameters **: ", " ${jsonValues[index].javaClass.name}")
        }
    }

    //Make an object of mutation (done by reflection)
    val obj = constructor.newInstance(*jsonValues.toArray())

    return obj as Mutation<Operation.Data, Operation.Data, Operation.Variables>
}

/*
 Get the mutationDao of the database.
 @return Database Dao.
 */
fun getDao() = Offline.getDb()?.mutationDao()

/*
   To check if the string provided in the function matches apollo Input class or not.
 */
fun inputTypeChecker(string: String) = string.equals("com.apollographql.apollo.api.Input")

/*  Extension function for ApolloClient Builder
    @receiver param: ApolloClient.Buidler, which can be used by the user for creating a custom client.
    @return ApolloClient.Buidler
 */
fun ApolloClient.Builder.OfflineClientBuilder(): ApolloClient.Builder = this


