package org.aerogear.graphqlandroid.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.android.synthetic.main.alertdialog_task.view.etDescTask
import kotlinx.android.synthetic.main.alertdialog_task.view.etTitleTask
import kotlinx.android.synthetic.main.alertfrag_createtasks.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.android.synthetic.main.fragment_tasks.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.offix.enqueue
import org.aerogear.offix.interfaces.ResponseCallback
import java.util.concurrent.atomic.AtomicReference

class Fragment_Tasks : Fragment() {

    var noteslist = arrayListOf<Task>()
    val TAG = javaClass.simpleName
    val taskAdapter by lazy {
        activity?.baseContext?.let { TaskAdapter(noteslist, it) }
    }

    val watchResponse = AtomicReference<Response<FindAllTasksQuery.Data>>()
    var apolloQueryWatcher: ApolloQueryWatcher<FindAllTasksQuery.Data>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.recycler_view_tasks.layoutManager = LinearLayoutManager(activity?.baseContext)
        view.recycler_view_tasks.adapter = taskAdapter

        getTasks()

        pull_to_refresh_tasks.setOnRefreshListener {
            doYourUpdate()
            pull_to_refresh_tasks.isRefreshing = false
        }

        //Used for creating a new task
        insertbutton_tasks.setOnClickListener {
            val inflatedView = LayoutInflater.from(activity?.baseContext).inflate(R.layout.alertfrag_createtasks, null, false)
            val customAlert: AlertDialog? = activity?.baseContext?.let { it1 ->
                android.support.v7.app.AlertDialog.Builder(it1)
                    .setView(inflatedView)
                    .setTitle("Create a new Note")
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { dialog, which ->
                        val title = inflatedView.etTitleTask.text.toString()
                        val desc = inflatedView.etDescTask.text.toString()
                        val version = inflatedView.etVerTask.text.toString()
                        createtask(title, desc, version.toInt())
                        dialog.dismiss()
                    }
                    .create()
            }
            customAlert?.show()
        }
    }

    fun doYourUpdate() {

        Log.e(TAG, " -*-*-*- doYourUpdate")
        noteslist.clear()

        activity?.baseContext?.let {
            Utils.getApolloClient(it)?.query(
                FindAllTasksQuery.builder().build()
            )?.watcher()
                ?.refetchResponseFetcher(ApolloResponseFetchers.CACHE_AND_NETWORK)
                ?.enqueueAndWatch(object : ApolloCall.Callback<FindAllTasksQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                        Log.e(TAG, " doYourUpdate onFailure----$e ")
                    }

                    override fun onResponse(response: Response<FindAllTasksQuery.Data>) {

                        watchResponse.set(response)

                        Log.e(TAG, "on Response doYourUpdate: Watcher ${response.data()}")

                        val result = watchResponse.get()?.data()?.findAllTasks()
                        result?.forEach {
                            val title = it.title()
                            val desc = it.description()
                            val id = it.id()
                            val version: Int? = it.version()
                            val task = Task(title, desc, id.toInt(), version!!)
                            activity?.runOnUiThread {
                                noteslist.add(task)
                                taskAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                })
        }

        pull_to_refresh_tasks.isRefreshing = false
    }

    fun getTasks() {
        Log.e(TAG, " ----- getTasks")

        noteslist.clear()

        FindAllTasksQuery.builder()?.build()?.let {
            activity?.baseContext?.let { it1 ->
                Utils.getApolloClient(it1)?.query(it)
                    ?.responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                    ?.enqueue(object : ApolloCall.Callback<FindAllTasksQuery.Data>() {

                        override fun onFailure(e: ApolloException) {
                            e.printStackTrace()
                            Log.e(TAG, "----$e ")
                        }

                        override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                            Log.e(TAG, "on Response : response.data ${response.data()}")
                            val result = response.data()?.findAllTasks()


                            result?.forEach {
                                val title = it.title()
                                val desc = it.description()
                                val id = it.id()
                                val version: Int? = it.version()
                                Log.e("${TAG}10", "$title")
                                Log.e("${TAG}11", "$desc")
                                Log.e("${TAG}12", "$id")
                                Log.e("${TAG}13", "$version")
        //
                                val task = Task(title, desc, id.toInt(), version!!)
                                noteslist.add(task)
                            }
                            activity?.runOnUiThread {
                                taskAdapter?.notifyDataSetChanged()
                            }
                        }
                    })
            }
        }
    }

    fun updateTask(id: String, title: String, version: Int, description: String) {
        Log.e(TAG, "inside update title in MainActivity")

        /*
        As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
        */
        val input = TaskInput.builder().title(title).version(version).description(description).status("test").build()

        var mutation = UpdateTaskMutation.builder().id(id).input(input).build()

        Log.e(TAG, " updateTask ********: - $mutation")

        val client = activity?.baseContext?.let {
            Utils.getApolloClient(it)?.mutate(
                mutation
            )?.refetchQueries(apolloQueryWatcher?.operation()?.name())
        }

        Log.e(TAG, " updateTask class name: - ${mutation.javaClass.simpleName}")
        Log.e(
            TAG,
            " updateTask 20: - ${client?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " updateTask 21: - ${client?.operation()?.queryDocument()}")
        Log.e(TAG, " updateTask 22: - ${client?.operation()?.variables()?.valueMap()}")
        Log.e(TAG, " updateTask 23: - ${client?.operation()?.name()}")

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

        activity?.baseContext?.let {
            Utils.getApolloClient(it)?.enqueue(
                mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
                customCallback
            )
        }
    }

    fun createtask(title: String, description: String, version: Int) {
        Log.e(TAG, "inside create title")

        /*
         As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
         */
        val input = TaskInput.builder().title(title).description(description).version(version).status("test").build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        val client = activity?.baseContext?.let {
            Utils.getApolloClient(it)?.mutate(
                mutation
            )?.refetchQueries(apolloQueryWatcher?.operation()?.name())
        }

        Log.e(TAG, " updateTask 22: - ${client?.operation()?.variables()?.valueMap()}")

        val customCallback = object : ResponseCallback {

            override fun onSuccess(response: Response<Any>) {
                Log.e("onSuccess() createTask", "${response.data()}")
            }

            override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
                e.printStackTrace()
                Log.e("onSchedule() createTask", "${mutation.variables().valueMap()}")
            }
        }

        activity?.baseContext?.let {
            Utils.getApolloClient(it)?.enqueue(
                mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
                customCallback
            )
        }

        Toast.makeText(activity, "Mutation with title $title created", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        super.onDestroy()
    }
}