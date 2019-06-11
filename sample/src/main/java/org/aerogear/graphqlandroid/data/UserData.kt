package org.aerogear.graphqlandroid.data

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.model.Task

class UserData {


    val TAG = javaClass.simpleName

    lateinit var apolloStore: ApolloStore

    fun getTasks(context: Context): ArrayList<Task> {

        var noteslist = arrayListOf<Task>()

        Log.e(TAG, "inside getTasks")

        val client = Utils.getApolloClient(context)?.query(
            AllTasksQuery.builder().build()
        )

        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(context)?.activeCallsCount()}")

        apolloStore = Utils.getApolloClient(context)?.apolloStore()!!

        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(context)?.apolloStore()}")

        client?.enqueue(object : ApolloCall.Callback<AllTasksQuery.Data>() {

            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
                Log.e(TAG, e.toString())
            }

            override fun onResponse(response: Response<AllTasksQuery.Data>) {

                val result = response.data()?.allTasks()
                Log.e(
                    TAG,
                    "onResponse-getTasks : ${result?.get(result.size - 1)?.title()} ${result?.get(result.size - 1)?.version()}"
                )

                result?.forEach {
                    val title = it.title()
                    val desc = it.description()
                    val id = it.id()
                    val version: Int? = it.version()
                    val task = Task(title, desc, id.toInt(), version!!)
                    noteslist.add(task)
                }
            }
        })

        return noteslist
    }

    fun updateTask(id: String, title: String, version: Int, context: Context) {

        Log.e(TAG, "inside update task")

        val client = Utils.getApolloClient(context)?.mutate(
            UpdateCurrentTask.builder().id(id).title(title).version(version).build()
        )

//        client = Utils.getApolloClient(this)?.mutate(mutation)?.refetchQueries(object : OperationName {
//            override fun name(): String {
//                return AllTasksQuery.OPERATION_DEFINITION
//            }
//
//        })

        client?.enqueue(object : ApolloCall.Callback<UpdateCurrentTask.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "updateTask", e.toString())
            }

            override fun onResponse(response: Response<UpdateCurrentTask.Data>) {
                val result = response.data()?.updateTask()

                Log.e(TAG, "onResponse-UpdateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")

                getTasks(context)
            }
        })
    }

     fun createtask(title: String, description: String, context: Context) {

        Log.e(TAG, "inside create task")

        val client = Utils.getApolloClient(context)?.mutate(
            CreateTask.builder().title(title).description(description).build()
        )

        client?.enqueue(object : ApolloCall.Callback<CreateTask.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "createTask", e.toString())
            }

            override fun onResponse(response: Response<CreateTask.Data>) {
                val result = response.data()?.createTask()

                Log.e(TAG, "onResponse-CreateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")

                getTasks(context)
            }
        })
    }


    fun deleteTask(id: String,context: Context) {
        Log.e(TAG, "inside delete task")

        val client = Utils.getApolloClient(context)?.mutate(
            DeleteTask.builder().id(id).build()
        )
        client?.enqueue(object : ApolloCall.Callback<DeleteTask.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "deleteTask", e.toString())
            }

            override fun onResponse(response: Response<DeleteTask.Data>) {
                val result = response.data()?.deleteTask()

                Log.e(TAG, "onResponse-DeleteTask")

                Log.e(TAG, "$result")
                getTasks(context)
            }
        })
    }


}