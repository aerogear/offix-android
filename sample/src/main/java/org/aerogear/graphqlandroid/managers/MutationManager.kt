package org.aerogear.graphqlandroid.managers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.InputValidator.taskValidation
import org.aerogear.graphqlandroid.InputValidator.userValidation
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserInput
import org.aerogear.offix.Offline

object MutationManager {
    private val TAG = "MutationManager"
    private var apolloQueryWatcher: ApolloQueryWatcher<FindAllTasksQuery.Data>? = null

     fun updateTask(id: String, title: String, description: String, context: Context) {
        Log.e(TAG, "inside update title in MainActivity")

        /*
        As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
        */
         if(taskValidation(id, title, description)) {
             val input =
                 TaskInput.builder().title(title).version(1).description(description).status("test")
                     .build()

             val mutation = UpdateTaskMutation.builder().id(id).input(input).build()


             Log.e(TAG, " updateTask ********: - $mutation")

             val mutationCall = Utils.getApolloClient(context)?.mutate(
                 mutation
             )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

             Log.e(TAG, " updateTask class name: - ${mutation.javaClass.simpleName}")
             Log.e(
                 TAG,
                 " updateTask 20: - ${mutationCall?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
             )
             Log.e(TAG, " updateTask 21: - ${mutationCall?.operation()?.queryDocument()}")
             Log.e(TAG, " updateTask 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
             Log.e(TAG, " updateTask 23: - ${mutationCall?.operation()?.name()}")

             val callback = object : ApolloCall.Callback<UpdateTaskMutation.Data>() {
                 override fun onFailure(e: ApolloException) {
                     Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                     e.printStackTrace()
                 }

                 override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                     val result = response.data()?.updateTask()

                     //In case of conflicts data returned from the server id null.
                     result?.let {
                         Log.e(TAG, "onResponse-UpdateTask- $it")
                     }
                 }
             }
             mutationCall?.enqueue(callback)
             if (Offline.isNetwork()) {
                 Toast.makeText(context, "Task with id $id is updated", Toast.LENGTH_LONG)
                     .show()
             } else {
                 Toast.makeText(
                     context,
                     "Task with id $id is stored offline. Changes will be synced to the server when app comes online.",
                     Toast.LENGTH_LONG
                 ).show()

             }
         } else {
             Toast.makeText(context, "Task input not valid", Toast.LENGTH_SHORT)
         }
    }

    fun updateUser(
        idUser: String,
        taskId: String,
        title: String,
        firstName: String,
        lastName: String,
        email: String,
        context: Context
    ) {
        Log.e(TAG, "inside updateUser in MainActivity")
        if(userValidation(idUser, taskId, title, firstName, lastName, email)){
        val input =
            UserInput.builder().title(title).lastName(lastName).firstName(firstName).email(email)
                .taskId(taskId)
                .creationmetadataId(taskId).build()

        val mutation = UpdateUserMutation.builder().id(idUser).input(input).build()

        Log.e(TAG, " updateUser ********: - $mutation")

        val mutationCall = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " updateUser class name: - ${mutation.javaClass.simpleName}")
        Log.e(
            TAG,
            " updateUser 20: - ${mutationCall?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " updateUser 21: - ${mutationCall?.operation()?.queryDocument()}")
        Log.e(TAG, " updateUser 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        Log.e(TAG, " updateUser 23: - ${mutationCall?.operation()?.name()}")

        val callback = object : ApolloCall.Callback<UpdateUserMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<UpdateUserMutation.Data>) {
                val result = response.data()?.updateUser()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(
                context,
                context.resources.getText(R.string.user_updated, idUser),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                context.resources.getText(R.string.user_updates_stored, idUser),
                Toast.LENGTH_LONG
            ).show()
        }
        } else {
            Toast.makeText(context, "User input not valid", Toast.LENGTH_SHORT)
        }

    }

    fun createTask(title: String, description: String, context: Context) {
        Log.e(TAG, "inside create title")
        if(taskValidation("0", title, description)){
        /*
         As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
         */
        val input =
            TaskInput.builder().title(title).description(description).version(1).status("test")
                .build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        val mutationCall = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " updateTask 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")


        val callback = object : ApolloCall.Callback<CreateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                val result = response.data()?.createTask()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)

        if (Offline.isNetwork()) {
            Toast.makeText(context, "Mutation with title $title is created", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                context,
                "Create mutation with title $title is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }
        } else {
            Toast.makeText(context, "Task input not valid", Toast.LENGTH_SHORT)
        }
    }

    fun createUser(
        title: String,
        firstName: String,
        lastName: String,
        email: String,
        taskId: String,
        context: Context
    ) {
        Log.e(TAG, "inside create user")
        if(userValidation("0", taskId, title, firstName, lastName, email)){
        val input =
            UserInput.builder().taskId(taskId).email(email).firstName(firstName).lastName(lastName)
                .title(title)
                .creationmetadataId(taskId).build()

        val mutation = CreateUserMutation.builder().input(input).build()

        val mutationCall = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " createUser 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        val callback = object : ApolloCall.Callback<CreateUserMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<CreateUserMutation.Data>) {
                val result = response.data()?.createUser()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(
                context,
                "Task with id $taskId is assigned to $firstName $lastName",
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            Toast.makeText(
                context,
                "Update in user where Task with id $taskId is assigned to $firstName $lastName is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }
        } else {
            Toast.makeText(context, "User input not valid", Toast.LENGTH_SHORT)
        }
    }

    fun checkAndUpdateTask(id: String, title: String, description: String, context: Context) {
        Log.e(TAG, "inside checkAndUpdateTask in MainActivity")

        /*
        As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
        */
        if(taskValidation(id, title, description)){
        val mutation =
            CheckAndUpdateTaskMutation.builder().version(1).id(id).title(title)
                .description(description).status("test")
                .build()

        Log.e(TAG, " checkAndUpdateTask ********: - $mutation")

        val mutationCall = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " checkAndUpdateTask class name: - ${mutation.javaClass.simpleName}")
        Log.e(
            TAG,
            " checkAndUpdateTask 20: - ${mutationCall?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " checkAndUpdateTask 21: - ${mutationCall?.operation()?.queryDocument()}")
        Log.e(
            TAG,
            " checkAndUpdateTask 22: - ${mutationCall?.operation()?.variables()?.valueMap()}"
        )
        Log.e(TAG, " checkAndUpdateTask 23: - ${mutationCall?.operation()?.name()}")

        val callback = object : ApolloCall.Callback<CheckAndUpdateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<CheckAndUpdateTaskMutation.Data>) {
                val result = response.data()?.checkAndUpdateTask()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(
                context,
                "Task with id $id is updated without any Conflict.",
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            Toast.makeText(
                context,
                "Task with id $id is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }
        } else {
            Toast.makeText(context, "Task input not valid", Toast.LENGTH_SHORT)
        }
    }
}