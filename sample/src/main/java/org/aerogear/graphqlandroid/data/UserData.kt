package org.aerogear.graphqlandroid.data

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCall.Callback
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.offix.ResponseCallback
import org.aerogear.offix.enqueue
import java.util.concurrent.atomic.AtomicReference

class UserData(val context: Context) {

    val noteslist = arrayListOf<Task>()
    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null
    val watchResponse = AtomicReference<Response<AllTasksQuery.Data>>()
    val TAG = javaClass.simpleName
    lateinit var apolloStore: ApolloStore
    fun getTasks(): ArrayList<Task> {

        noteslist.clear()
        Log.e(TAG, "inside getTasks")
        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(context)?.activeCallsCount()}")

        apolloStore = Utils.getApolloClient(context)?.apolloStore()!!
        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(context)?.apolloStore()}")

        AllTasksQuery.builder()?.build()?.let {
            Utils.getApolloClient(context)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                ?.enqueue(object : Callback<AllTasksQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                        Log.e(TAG, "----$e ")
                    }

                    override fun onResponse(response: Response<AllTasksQuery.Data>) {
                        Log.e(TAG, "on Response : response.data ${response.data()}")
                        val result = response.data()?.allTasks()
//                        Log.e(
//                            TAG,
//                            "onResponse-getTasks : ${result?.get(result.size - 1)?.title()} ${result?.get(result.size - 1)?.version()}"
//                        )
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
        }
        return noteslist
    }

    fun updateTask(id: String, title: String, version: Int) {
        Log.e(TAG, "inside update title")
        val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()
        Log.e(TAG, " updateTask ********: - $mutation")

        val client = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        val s: String = com.apollographql.apollo.internal.json.Utils.toJsonString(client.toString())

        Log.e(TAG, " updateTask 1: - $s")
        Log.e(
            TAG,
            " updateTask 20: - ${client?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " updateTask 21: - ${client?.operation()?.queryDocument()}")
        Log.e(TAG, " updateTask 23: - ${client?.operation()?.variables()?.valueMap()}")
        Log.e(TAG, " updateTask 25: - ${client?.operation()?.name()}")

        val customCallback = object : ResponseCallback {
            override fun onSuccess(response: Response<Any>) {
                Log.e("onSuccess() updateTask", "${response.data()}")
                val result = response.data()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }

            override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
                Log.e("onSchedule() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }
        }

        Utils.getApolloClient(context)?.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
        )
    }

    fun createtask(title: String, description: String) {

        Log.e(TAG, "inside create title")
        val mutation = CreateTaskMutation.builder().title(title).description(description).build()

        val customCallback = object : ResponseCallback {
            override fun onSuccess(response: Response<Any>) {
                Log.e("onSuccess() createTask", "${response.data()}")
            }

            override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
                e.printStackTrace()
                Log.e("onSchedule() createTask", "${mutation.variables().valueMap()}")
            }
        }

        Utils.getApolloClient(context)?.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
        )
    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete title")
        val mutation = DeleteTaskMutation.builder().id(id).build()

        val callback = object : ApolloCall.Callback<DeleteTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "updateTask", e.toString())
            }

            override fun onResponse(response: Response<DeleteTaskMutation.Data>) {
                val result = response.data()?.deleteTask()
                Log.e(TAG, "onResponse-UpdateTask")
            }
        }

        val customCallback = object : ResponseCallback {
            override fun onSuccess(response: Response<Any>) {
                Log.e("onSuccess() deleteTask", "${response.data()}")
            }

            override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
                e.printStackTrace()
                Log.e("onSchedule() deleteTask", "${mutation.variables().valueMap()}")
            }
        }

        Utils.getApolloClient(context)?.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
        )
    }

    fun doYourUpdate(): ArrayList<Task> {
        noteslist.clear()
        Utils.getApolloClient(context)?.query(
            AllTasksQuery.builder().build()
        )?.watcher()
            ?.refetchResponseFetcher(ApolloResponseFetchers.CACHE_FIRST)
            ?.enqueueAndWatch(object : Callback<AllTasksQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    e.printStackTrace()
                    Log.e(TAG, " doYourUpdate onFailure----$e ")
                }

                override fun onResponse(response: Response<AllTasksQuery.Data>) {
                    watchResponse.set(response)
                    Log.e(TAG, "on Response doYourUpdate: Watcher ${response.data()}")
                    val result = watchResponse.get()?.data()?.allTasks()
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
}