package org.aerogear.graphqlandroid

import android.content.Context
import android.util.Log
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.offix.interfaces.ConflictResolutionImpl
import org.aerogear.offix.interfaces.ResponseCallback
import org.aerogear.offix.enqueue

/*
UserConflictResolutionHandler extends ConflictResolutionImpl.
Here the user provides the custom implementation of resolving conflicts.
 */
class UserConflictResolutionHandler(context: Context) : ConflictResolutionImpl {
    val context = context
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

            when (operationType) {
                /*
                According to the schema structure, used a version based approach of resolving conflicts.
                If operationType is "UpdateCurrentTaskMutation" perform the following steps to resolve conflicts.
                 */
                "UpdateCurrentTaskMutation" -> {
                    var mut = UpdateCurrentTaskMutation.builder().id(clientState["id"].toString())
                        .title(clientState["title"].toString()).version(
                            versionAfterConflict
                        ).build()

                    val customCallback = object : ResponseCallback {

                        override fun onSuccess(response: Response<Any>) {
                            Log.e("onSuccess() updateTask", "${response.data()}")
                            val result = response.data()

                            //In case of conflicts data returned from the server id null.
                            result?.let {
                                Log.e(TAG, "onResponse-UpdateTask- $it")
                            }
                        }

                        override fun onSchedule(
                            e: ApolloException,
                            mutation: Mutation<Operation.Data, Any, Operation.Variables>
                        ) {
                            Log.e("onSchedule() updateTask", "${mutation.variables().valueMap()}")
                            e.printStackTrace()
                        }
                    }

                    Utils.getApolloClient(context)?.enqueue(
                        mut as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
                        customCallback
                    )
                }
            }
        }
    }
}