package org.aerogear.graphqlandroid.activities

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.data.ViewModel
import org.aerogear.graphqlandroid.model.Task
import java.util.concurrent.atomic.AtomicReference
import org.aerogear.graphqlandroid.SubscribeTasks as SubscribeTasks1

class MainActivity : AppCompatActivity() {


    val noteslist = arrayListOf<Task>()
    val TAG = javaClass.simpleName
    val taskAdapter by lazy {
        TaskAdapter(noteslist, this)
    }


    lateinit var apolloStore: ApolloStore

    private val disposables = CompositeDisposable()

    val watchResponse = AtomicReference<Response<AllTasksQuery.Data>>()

    val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val apolloSqlHelpersize by lazy {
        ApolloSqlHelper.TABLE_RECORDS.length
    }

    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myModel = ViewModelProviders.of(this).get(ViewModel::class.java)

        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null && activeNetwork.isConnected) {
            Log.e(TAG, " User is online ")
            Log.e(TAG, "  apolloSql size: $apolloSqlHelpersize")
            getTasks()

        }

        pull_to_refresh.setOnRefreshListener {
            doYourUpdate()
        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        fabAdd.setOnClickListener {

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
                    createtask(title, desc)
                    Log.e(TAG, "jhjj")
//                    taskAdapter.notifyItemInserted(noteslist.size - 1)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()

        }

    }

    private fun doYourUpdate() {
        apolloQueryWatcher?.refetch()
        Log.e(TAG, " on Refersh : doYourUpdate ")
        taskAdapter.notifyDataSetChanged()
        pull_to_refresh.isRefreshing = false
    }

    fun getTasks() {

        noteslist.clear()

        Log.e(TAG, "inside getTasks")
        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(this)?.activeCallsCount()}")
        apolloStore = Utils.getApolloClient(this)?.apolloStore()!!
        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(this)?.apolloStore()}")

        val client: ApolloCall<AllTasksQuery.Data> = Utils.getApolloClient(this)?.query(
            AllTasksQuery.builder().build()
        )?.responseFetcher(ApolloResponseFetchers.CACHE_FIRST) as ApolloCall<AllTasksQuery.Data>

        Rx2Apollo.from(client).subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeWith(object : DisposableObserver<Response<AllTasksQuery.Data>>() {
                override fun onComplete() {

                }

                override fun onNext(result: Response<AllTasksQuery.Data>) {
                    Log.e(TAG, " onNext : ${result.data()?.allTasks()?.size}")

                    result?.data()?.allTasks()?.forEach {
                        val title = it.title()
                        val desc = it.description()
                        val id = it.id()
                        val version: Int? = it.version()
                        val task = Task(title, desc, id.toInt(), version!!)
                        noteslist.add(task)
                    }
                    runOnUiThread {
                        Log.e(TAG, " Size ${noteslist.size}")
                        taskAdapter.notifyDataSetChanged()
                    }

                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, " onError : ${e.message}")
                }

            }

            )?.let {
                disposables.add(
                    it
                )
            }


    }

    fun updateTask(id: String, title: String, version: Int) {

        Log.e(TAG, "inside update task")

        val mutation = UpdateCurrentTask.builder().id(id).title(title).version(version).build()

        val client = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Utils.getApolloClient(this)?.defaultCacheHeaders()

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

                noteslist.clear()
//                runOnUiThread {
//                    noteslist.clear()
//                    getTasks()
//                }
            }
        })
    }

    fun createtask(title: String, description: String) {

        Log.e(TAG, "inside create task")

        val client = Utils.getApolloClient(this)?.mutate(
            CreateTask.builder().title(title).description(description).build()
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

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

                noteslist.clear()
            }
        })
    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete task")

        val client = Utils.getApolloClient(this)?.mutate(
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

                runOnUiThread {
                    getTasks()
                }
            }
        })
    }

    fun onSuccess() {

        Log.e(TAG, "onSuccess in MainActivity")

        noteslist.clear()
        getTasks()
        taskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        super.onDestroy()
    }


}

