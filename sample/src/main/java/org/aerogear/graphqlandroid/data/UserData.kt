package org.aerogear.graphqlandroid.data

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.persistence.Mutation
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference

class UserData(val context: Context) {

    val noteslist = arrayListOf<Task>()
    val newNotes = arrayListOf<Task>()

    val adapter = TaskAdapter(noteslist, context)

    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null

    val watchResponse = AtomicReference<Response<AllTasksQuery.Data>>()

    val TAG = javaClass.simpleName

    lateinit var apolloStore: ApolloStore

    val dbDao = MyApplciation.database.mutationDao()

    fun getTasks(): ArrayList<Task> {

        noteslist.clear()

        Log.e(TAG, "inside getTasks")

        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(context)?.activeCallsCount()}")

        apolloStore = Utils.getApolloClient(context)?.apolloStore()!!
        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(context)?.apolloStore()}")

        AllTasksQuery.builder()?.build()?.let {
            Utils.getApolloClient(context)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                ?.enqueue(object : ApolloCall.Callback<AllTasksQuery.Data>() {

                    override fun onFailure(e: ApolloException) {

                        e.printStackTrace()
                        Log.e(TAG, "----$e ")
                    }

                    override fun onResponse(response: Response<AllTasksQuery.Data>) {

                        //watchResponse.set(response)

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

        // Log.e(TAG, "watched operation ${client?.operation()}")
    }

    fun updateTask(id: String, title: String, version: Int) {

        Log.e(TAG, "inside update title")

        val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()

        val responseClassName = mutation.javaClass.name

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

        client?.enqueue(object : ApolloCall.Callback<UpdateCurrentTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "updateTask", e.toString())

                val operationID = client.operation().operationId()
                val queryDoc = client.operation().queryDocument()
                val operationName = client.operation().name()
                val valuemap = client.operation().variables().valueMap()
                val jsonObject = JSONObject(valuemap)
                val mutationObj = Mutation(operationID, queryDoc, operationName, jsonObject, responseClassName)
                OffUpdateMut(mutationObj)
            }

            override fun onResponse(response: Response<UpdateCurrentTaskMutation.Data>) {
//                if(offlineArrayList.size!=0)
//                offlineArrayList.removeAt(offlineArrayList.size-1)
                val result = response.data()?.updateTask()

                Log.e(TAG, "onResponse-UpdateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")
            }
        })
    }

    fun OffUpdateMut(mutationObj: Mutation) {

        Log.e("UtilClass ", " Offline Update Mutation function called")

        val longid = dbDao.insertMutation(mutationObj)

        Log.e("UtilClass ", " OffUpdateMut id of inserted mutation : $longid ")

        val getListOfInsertedMutation = dbDao.getAllMutations()
        Log.e("UtilClass ", " OffUpdateMut size of list : ${getListOfInsertedMutation.size} ")
        Log.e("UtilClass ", " OffUpdateMut valuemap : ${getListOfInsertedMutation[0].valuemap} ")
        Log.e("UtilClass ", " OffUpdateMut valuemap : ${getListOfInsertedMutation[0].queryDoc} ")
    }

    fun createtask(title: String, description: String): ArrayList<Task> {

        Log.e(TAG, "inside create title")

        val mutation = CreateTaskMutation.builder().title(title).description(description).build()

        val client = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())
        //?.refetchQueries(CreateTaskMutation.builder().title(title).description(description).build()?.name())

        client?.enqueue(object : ApolloCall.Callback<CreateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "createTask", e.toString())

                val operationID = client.operation().operationId()
                val queryDoc = client.operation().queryDocument()
                val operationName = client.operation().name()
                val valuemap = client.operation().variables().valueMap()

                val jsonObject = JSONObject(valuemap)
                val mutationObj = Mutation(operationID, queryDoc, operationName, jsonObject, mutation.javaClass.name)

                OffCreateMut(mutationObj)

            }

            override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                val result = response.data()?.createTask()

                Log.e(TAG, "onResponse-CreateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")

                val obj = result?.title()?.let {
                    Task(
                        it, result.description(), result.id().toInt(),
                        result.version()!!.toInt()
                    )
                }
                if (obj != null) {
                    newNotes.add(obj)
                }
            }
        })
        return newNotes

    }

    fun OffCreateMut(mutationObj: Mutation) {

        Log.e("UtilClass ", " OffCreateMut function called")

        val longid = dbDao.insertMutation(mutationObj)

        Log.e("UtilClass ", " OffCreateMut id of inserted mutation : $longid ")

        val getInsertedMutation = dbDao.getAllMutations()
        Log.e("UtilClass ", " OffCreateMut 2 : ${getInsertedMutation.size} ")
        Log.e("UtilClass ", " OffCreateMut 3: ${dbDao.getAMutation(mutationObj.SNo)} ")
    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete title")

        val mutation = DeleteTaskMutation.builder().id(id).build()

        val client = Utils.getApolloClient(context)?.mutate(
            mutation
        )
        client?.enqueue(object : ApolloCall.Callback<DeleteTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "deleteTask", e.toString())

                val operationID = client.operation().operationId()
                val queryDoc = client.operation().queryDocument()
                val operationName = client.operation().name()
                val valuemap = client.operation().variables().valueMap()

                val jsonObject = JSONObject(valuemap)
                val mutationObj = Mutation(operationID, queryDoc, operationName, jsonObject, mutation.javaClass.name)

                OffDeleteMut(mutationObj)
            }

            override fun onResponse(response: Response<DeleteTaskMutation.Data>) {
                val result = response.data()?.deleteTask()

                Log.e(TAG, "onResponse-DeleteTask")

                Log.e(TAG, "$result")

                getTasks()
            }
        })
    }

    fun OffDeleteMut(mutationObj: Mutation) {

        Log.e("UtilClass ", " OffDeleteMut function called")

        val longid = dbDao.insertMutation(mutationObj)

        Log.e("UtilClass ", " OffDeleteMut id of inserted mutation : $longid ")

    }

    fun doYourUpdate(): ArrayList<Task> {

        noteslist.clear()

        Utils.getApolloClient(context)?.query(
            AllTasksQuery.builder().build()
        )?.watcher()
            ?.refetchResponseFetcher(ApolloResponseFetchers.CACHE_FIRST)
            ?.enqueueAndWatch(object : ApolloCall.Callback<AllTasksQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    e.printStackTrace()
                    Log.e(TAG, " doYourUpdate onFailure----$e ")
                }

                override fun onResponse(response: Response<AllTasksQuery.Data>) {

                    watchResponse.set(response)
                    //noteslist.clear()

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