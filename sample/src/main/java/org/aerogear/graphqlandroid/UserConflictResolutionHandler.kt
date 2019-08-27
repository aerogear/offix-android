package org.aerogear.graphqlandroid

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.offix.interfaces.ConflictResolutionInterface
import com.apollographql.apollo.api.Operation as Operation1

/*
UserConflictResolutionHandler extends ConflictResolutionInterface.
Here the user provides the custom implementation of resolving conflicts.
 */
class UserConflictResolutionHandler(val context: Context) : ConflictResolutionInterface {
    val TAG = javaClass.simpleName

    /*
    Function which resolve the conflicts based on the user business logic.
     */
    override fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    ) {
        /*
       Version based approach of Conflict Resolution.
        */
        val serverMap = serverState
        val containsVersion = serverMap.containsKey("version")

        if (containsVersion) {
            var versionAfterConflict = serverMap.get("version") as Int + 1

            /* You can run a switch case on the operation type to detect which type of mutation is it in which conflict occured
               and accordingly create an object of that mutation, resolve conflict and make a server call with it.
             */
            when (operationType) {
                /*
                According to the schema structure, used a version based approach of resolving conflicts.
                If operationType is "UpdateCurrentTaskMutation" perform the following steps to resolve conflicts.
                 */
                "UpdateTaskMutation" -> {
                    //TODO Check the input type.
                    /*As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
                     */
                    val input = TaskInput.builder().title(clientState["title"].toString()).version(versionAfterConflict)
                        .description(clientState["description"].toString()).status("test").build()

                    var mutation = UpdateTaskMutation.builder().id(clientState["taskId"].toString()).input(input).build()
                    val mutationCall = Utils.getApolloClient(context)?.mutate(
                        mutation
                    )

                    val callback = object : ApolloCall.Callback<UpdateTaskMutation.Data>() {
                        override fun onFailure(e: ApolloException) {
                            Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                            e.printStackTrace()
                        }

                        override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                            Log.e("onResponse() updateTask", "${response.data()?.updateTask()?.title()}")
                            val result = response.data()?.updateTask()

                            //In case of conflicts data returned from the server taskId null.
                            result?.let {
                                Log.e(TAG, "onResponse-UpdateTask- $it")
                            }
                        }

                    }
                    mutationCall?.enqueue(callback)
                }
                "UpdateUserMutation" -> {
                    /*Similar for UpdateUserMutation
                    1. Get all the fields from the clientState.
                    2. Create an object of mutation.
                    3. Again make a call to the server.
                    */
                }
            }
        }
    }
}