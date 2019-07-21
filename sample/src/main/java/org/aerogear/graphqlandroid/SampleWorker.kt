package org.aerogear.graphqlandroid

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.offixoffline.InputTypeChecker
import org.aerogear.offixoffline.OffixWorker
import org.aerogear.offixoffline.Offline

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
        Log.e(TAG, "Size of list : ${listOfMutations.size}")

        /*
         Check again if the network connection is there or not and also if the list is not empty.
         */
        if (Offline.isNetwork() && listOfMutations.isNotEmpty()) {
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
                val jsonObj = storedmutation.valueMap

                //Put all the json values into a list
                val iter = jsonObj.keys()
                iter.forEach { key ->
                    jsonValues.add(jsonObj.get(key))
                }

                Log.d("jsonValuesList ", " ${jsonValues.size}")

                jsonValues.forEach {
                    Log.d("jsonValuesList : ", " $it")
                }

                //Check if the input parameter is of type Input<*>, if yes typecast it to be of the type Input<*>
                parameters.forEachIndexed { index, clazz ->

                    Log.e("parameters : ", " ${clazz.name}")
                    if (InputTypeChecker(clazz.name).inCheck()) {
                        jsonValues[index] = Input.optional(jsonValues[index])
                        Log.e("parameters **: ", " ${jsonValues[index].javaClass.name}")
                    }
                }

                //Make an object of mutation (done by reflection)
                val obj = constructor.newInstance(*jsonValues.toArray())

                Log.e(TAG, "Current mutation serial number is: ${storedmutation.sNo}")

                /* Make an apollo client which takes in mutation object and makes a call to server.
                When the app is in background, then we can't access to the client made by the user. In this scenario, we have to make a
                custom minimal client to make the server call in the background.
                */
                val customClient = Utils.getApolloClient(ctx)?.mutate(
                    obj as Mutation<Operation.Data, Operation.Data, Operation.Variables>
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
                        deleteAMutation(storedmutation.sNo)
                    }
                })
            }
        }
        return Result.success()
    }
}

