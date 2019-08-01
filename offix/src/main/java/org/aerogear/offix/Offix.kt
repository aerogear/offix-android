@file:JvmName("Offix")

package org.aerogear.offix

import android.util.Log
import androidx.work.*
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import okhttp3.Interceptor
import org.json.JSONObject
import java.nio.charset.Charset

/*
Global variable which will keep track of whether the conflict is detected or not.
In case any conflicts come, the error string would be stored in this variable.
 */
lateinit var conflcitDetected: String

/*
Initialised gson variable.
 */
val gson = Gson()

/* Extension function on ApolloClient which will be used by the user while making a call request.
   @receiver parameter is ApolloClient on which the call will be made by the user.
   @param mutation which will be stored in the list if network connection is not there.
   @param responseCallback which is of type ResponseCallback.
 */
fun ApolloClient.enqueue(
    mutation: Mutation<Operation.Data, Any, Operation.Variables>,
    responseCallback: ResponseCallback
) {
    /* Set apollo client given by the user.
     */
    Offline.apClient = this

    /*
     Create an object of ApolloCall.Callback
    */
    val apolloCallback = object : ApolloCall.Callback<Any>() {

        /**
         * Called when the request could not be executed due to cancellation, a connectivity problem or
         * timeout.
         */
        override fun onFailure(e: ApolloException) {
            Log.d("Extension Callback - ", "$e")

            /* If the user is offline:
               1. Make an object of (org.aerogear.offix.persistence)Mutation.
               2. Store it in database
             */

            /* Get access to the dao of the database
             */
            val libDao = getDao()

            /* Insert mutation object in the database.
            */
            libDao?.insertMutation(getPersistenceMutation(mutation))
            Log.d("Extension", " serial number: ${libDao?.getAllMutations()?.get(0)?.sNo}")
            Log.d("Extension", " size of db list after inserting mutations: ${libDao?.getAllMutations()?.size}")

            /*
             Set the exception that caused onFailure() and the mutation object in the onSchedule() of responseCallback.
             */
            responseCallback.onSchedule(e, mutation)
        }

        override fun onResponse(response: Response<Any>) {
            val result = response.data().toString()
            Log.d("Extension Callback * ", result)

            /*
            Check if the response data is null or not. If it's null that means the conflict has happened.
             */
            if (response.data() == null) {
                retryConflictedMutation(mutation)
            }

            /*
            Set the response received from the server in the onSuccess() of responseCallback.
            In case of conflicts null is returned from the server as response.
            */
            responseCallback.onSuccess(response)
        }
    }

    /*
      Make a call with the mutation to the server.
     */
    this.mutate(mutation).enqueue(apolloCallback)
}

/*
 This function is called whenever we get a conflict.
 Here the mutation with the correct data is retried again.
 @param mutation: Mutation<Operation.Data, Any, Operation.Variables>

 */
fun retryConflictedMutation(mutation: Mutation<Operation.Data, Any, Operation.Variables>) {

    /*
      1. Map the json response String to the Conflict pojo class.
      2. Extract the correct version from the server data.
      3. Set the client version to the correct one. (server+1)
      4. Again make a call with the mutation to the server.
    */
    var parsedObject = gson.fromJson(conflcitDetected, ConflictPojo::class.java)
    var versionUpdated = parsedObject.errors[0].extensions.exception.conflictInfo.serverState.version + 1
    Log.d("Offix makePojoOfRes", " ${parsedObject.errors[0].extensions.exception.conflictInfo.serverState}")

    //TODO
    var hashMap: HashMap<String, Any> = HashMap()

    mutation.variables().valueMap()
        .forEach {
            if (it.key.equals("version")) {
                hashMap.put(it.key, versionUpdated)
            } else {
                hashMap.put(it.key, it.value)
            }
        }

    val jsonObj = JSONObject(hashMap).toString()
}


/*
 This function takes in an object of Mutation<D,T,V> and returns an object of com.aerogear.offix.persistence.Mutation.
 */
fun getPersistenceMutation(mutation: Mutation<Operation.Data, Any, Operation.Variables>): org.aerogear.offix.persistence.Mutation {

    val operationId = mutation.operationId()
    val operationDoc = mutation.queryDocument()
    val operationName = mutation.name()
    val valMap = mutation.variables().valueMap()
    val jsonObject = JSONObject(valMap)
    val responseClassName = mutation.javaClass.name

    /* Make an object of com.aerogear.offix.persistence.Mutation
     */
    val mutationDbObj = org.aerogear.offix.persistence.Mutation(
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
fun getMutation(storedmutation: org.aerogear.offix.persistence.Mutation): Mutation<Operation.Data, Operation.Data, Operation.Variables> {

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

/*
 This interceptor should be used by the user while making an instance of apollo client
 which will help in detecting conflicts.
 */
fun getResponseInterceptor(): Interceptor? {

    val conflictInterceptor = Interceptor {
        val request = it.request()

        val response = it.proceed(request)

        //https://stackoverflow.com/a/33862068/10189663
        val responseBody = response.body()
        val bufferedSource = responseBody?.source()
        bufferedSource?.request(Long.MAX_VALUE)
        val buffer = bufferedSource?.buffer()
        val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""

        Log.d("OffixClass", " Interceptor ** : $responseBodyString")

        //To see for conflict, "VoyagerConflict" which comes in the message is searched for.
        if (responseBodyString.contains("VoyagerConflict")) {
            conflcitDetected = responseBodyString
            Log.d("Offix conflcit", " VoyagerConflict")
            Log.d("Offix conflcit **", conflcitDetected)
        }
        return@Interceptor response
    }
    return conflictInterceptor
}

/*  Extension function for ApolloClient Builder
    @receiver param: ApolloClient.Buidler, which can be used by the user for creating a custom client.
    @return ApolloClient.Buidler
 */
fun ApolloClient.Builder.OfflineClientBuilder(): ApolloClient.Builder = this


