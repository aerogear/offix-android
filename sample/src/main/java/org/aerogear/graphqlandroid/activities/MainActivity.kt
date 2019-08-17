package org.aerogear.graphqlandroid.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.etDesc
import kotlinx.android.synthetic.main.alertdialog_task.view.etTitle
import kotlinx.android.synthetic.main.alertfrag_create.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.offix.enqueue
import org.aerogear.offix.interfaces.ResponseCallback
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {

    var noteslist = arrayListOf<Task>()
    val TAG = javaClass.simpleName
    val taskAdapter by lazy {
        TaskAdapter(noteslist, this)
    }
    val constraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    }
    private val disposables = CompositeDisposable()
    val watchResponse = AtomicReference<Response<FindAllTasksQuery.Data>>()
    var apolloQueryWatcher: ApolloQueryWatcher<FindAllTasksQuery.Data>? = null
    lateinit var apolloStore: ApolloStore
    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        getTasks()

        pull_to_refresh.setOnRefreshListener {
            doYourUpdate()
            pull_to_refresh.isRefreshing = false
        }

        //Used for creating a new task
        insertbutton.setOnClickListener {
            val inflatedView = LayoutInflater.from(this).inflate(R.layout.alertfrag_create, null, false)
            val customAlert: android.support.v7.app.AlertDialog = android.support.v7.app.AlertDialog.Builder(this)
                .setView(inflatedView)
                .setTitle("Create a new Note")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    val title = inflatedView.etTitle.text.toString()
                    val desc = inflatedView.etDesc.text.toString()
                    val version = inflatedView.etVer.text.toString()
                    createtask(title, desc, version.toInt())
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }
    }

    private fun doYourUpdate() {

        Log.e(TAG, " -*-*-*- doYourUpdate")
        noteslist.clear()

        Utils.getApolloClient(this)?.query(
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
                        runOnUiThread {
                            noteslist.add(task)
                            taskAdapter.notifyDataSetChanged()
                        }
                    }
                }
            })

        pull_to_refresh.isRefreshing = false
    }

    fun getTasks() {
        Log.e(TAG, " ----- getTasks")

        noteslist.clear()

        apolloStore = Utils.getApolloClient(context)?.apolloStore()!!
        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(context)?.apolloStore()}")

        FindAllTasksQuery.builder()?.build()?.let {
            Utils.getApolloClient(context)?.query(it)
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
                        runOnUiThread {
                            taskAdapter.notifyDataSetChanged()
                        }
                    }
                })
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

        val client = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

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

        Utils.getApolloClient(context)?.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
        )
    }

    fun createtask(title: String, description: String, version: Int) {
        Log.e(TAG, "inside create title")

        /*
         As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
         */
        val input = TaskInput.builder().title(title).description(description).version(version).status("test").build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        val client = Utils.getApolloClient(context)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

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

        Utils.getApolloClient(context)?.enqueue(
            mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
            customCallback
        )

        Toast.makeText(this, "Mutation with title $title created", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        disposables.dispose()
        super.onDestroy()
    }
}

