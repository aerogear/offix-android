package org.aerogear.offixsdk.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.offixsdk.Application
import org.aerogear.offixsdk.ClientGenerator


/**
 * Workmanager that replicates the mutations to the server that are stored in the database.
 * Post firing the mutation, it is deleted from the db
 */

class OfflineMutationsWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {


    private val getMutationDao = Application.database.mutationDao()
    private val getUrlDao = Application.database.urlDao()

    val TAG = javaClass.simpleName

    override fun doWork(): Result {

        Log.e(TAG, "In Worker")

        //get the list of mutations

        val listOfMutations = getMutationDao.getAllMutations()
        Log.e(TAG, "Size of list : ${listOfMutations.size}")

        //get the mutation one by one from database

        listOfMutations.forEach { storedmutation ->

            //Get the class name of the mutation to which it has to be mapped
            val responseClassName = storedmutation.responseClassName

            val classReflected: Class<*> = Class.forName(responseClassName)

            //Get the constructor of the class, and as apollo generated classes have only one constructor, so take the first one.
            val constructor = classReflected.constructors.first()

            //Get the parameterTypes
            val parameters = constructor.parameterTypes

            val jsonValues = arrayListOf<Any>()

            //Get the json object i.e the variables map given as input by the user
            val jsonObj = storedmutation.valuemap

            //Put all the json values into a list
            val iter = jsonObj.keys()
            iter.forEach { key ->
                jsonValues.add(jsonObj.get(key))
            }

            Log.e("jsonValuesList ", " ${jsonValues.size}")

            jsonValues.forEach {
                Log.e("jsonValuesList : ", " $it")
            }

            //Check if the input parameter is of type Input<*>, if yes typecast it to be of the type Input<*>
            parameters.forEachIndexed { index, clazz ->

                Log.e("parameters : ", " ${clazz.name}")
                if (clazz.name.equals("com.apollographql.apollo.api.Input")) {
                    jsonValues[index] = Input.optional(jsonValues[index])
                    Log.e("parameters **: ", " ${jsonValues[index].javaClass.name}")

                }
            }

            //Make an object of mutation (done by reflection)
            val obj = constructor.newInstance(*jsonValues.toArray())

            //get the server url related to this mutation.
            val currentUrl = getUrlDao.getAMutationUrl(storedmutation.SNo)

            Log.e(TAG, "Current mutation serial number is: ${storedmutation.SNo}")
            Log.e(TAG, "String url serial number is: ${currentUrl.SNo}")

            // Make an apollo client which takes in mutation object and makes a call to server.
            val customClient = ClientGenerator(currentUrl.urlString).getApolloClient()?.mutate(
                obj as Mutation<Operation.Data, Operation.Data, Operation.Variables>
            )

            customClient?.enqueue(object : ApolloCall.Callback<Operation.Data>() {
                override fun onFailure(e: ApolloException) {
                    e.printStackTrace()
                }

                override fun onResponse(response: Response<Operation.Data>) {
                    Log.e(TAG, response.hasErrors().toString())
                    Log.e(TAG, response.data().toString())
                    Log.e(TAG, response.errors().toString())

                    getMutationDao.deleteCurrentMutation(storedmutation.SNo)
                    getUrlDao.deleteCurrentMutationUrl(storedmutation.SNo)
                }

            })
        }

        return Result.success()
    }
}
