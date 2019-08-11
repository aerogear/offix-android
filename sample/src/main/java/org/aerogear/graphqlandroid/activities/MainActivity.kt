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
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.worker.SampleWorker
import org.aerogear.offix.enqueue
import org.aerogear.offix.interfaces.ResponseCallback
import org.aerogear.offix.scheduleWorker
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
    val watchResponse = AtomicReference<Response<AllTasksQuery.Data>>()
    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null
    lateinit var apolloStore: ApolloStore
    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        getTasks()
        subscribeUpdatedTaskAdded()
        subscribeNewTaskAdded()

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
                    createtask(title, desc)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }

        //Used for deleting task
        deletetask.setOnClickListener {
            val inflated = LayoutInflater.from(this).inflate(R.layout.alertdelete, null, false)
            val customAlert: android.support.v7.app.AlertDialog = android.support.v7.app.AlertDialog.Builder(this)
                .setView(inflated)
                .setTitle("Delete a  task")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    val id = inflated.etId.text.toString()
                    deleteTask(id)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }
    }

    private fun doYourUpdate() {

        Log.e(TAG, " ----- doYourUpdate")
        noteslist.clear()

        Utils.getApolloClient(this)?.query(
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

                    Log.e(TAG, "on Response doYourUpdate: Watcher ${response.data()}")


                    val result = watchResponse.get()?.data()?.allTasks()
//                    Log.e(
//                        TAG,
//                        "onResponse-getTasks : ${result?.get(result.size - 1)?.title()} ${result?.get(result.size - 1)?.version()}"
//              )
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
        Log.e(TAG, "inside getTasks")

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
                        runOnUiThread {
                            taskAdapter.notifyDataSetChanged()
                        }
                    }
                })
        }
    }

    fun updateTask(id: String, title: String, version: Int) {
        Log.e(TAG, "inside update title in MainActivity")
        var mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()
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

        Toast.makeText(this, "Mutation with title $title created", Toast.LENGTH_SHORT).show()
    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete task")
        Log.e(TAG, "inside delete title")
        val mutation = DeleteTaskMutation.builder().id(id).build()

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

    private fun subscribeUpdatedTaskAdded() {

        val subscription = SubscribeTasksSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(Rx2Apollo.from<SubscribeTasksSubscription.Data>(subscriptionCall!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSubscriber<Response<SubscribeTasksSubscription.Data>>() {
                    override fun onNext(response: Response<SubscribeTasksSubscription.Data>) {

                        val res = response.data()?.taskUpdated()
                        res?.let {

                            Log.e(TAG, " inside subscription1 ${it.title()} mutated upon updating")
                            Log.e(TAG, " inside subscription1 ${res.title()}")
//
//                            runOnUiThread {
//                                Log.e(TAG, " inside subscription1  *** ${it.title()} mutated upon updating")
//                                val tasktoberemoved =
//                                Task(it.title(), it.description(), it.id().toInt(), it.version()!! - 1)
//                                noteslist.remove(tasktoberemoved)
//                                noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
//                                taskAdapter.notifyDataSetChanged()
//                            }
                        }

                        Toast.makeText(
                            this@MainActivity,
                            "Subscription1 response received",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.message, e)
                        Toast.makeText(this@MainActivity, "Subscription1 failure", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onComplete() {
                        Log.d(TAG, "Subscription1 exhausted")
                        Toast.makeText(this@MainActivity, "Subscription1 complete", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
        )
    }

    private fun subscribeNewTaskAdded() {

        val subscription = SubscribeAddTaskSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(Rx2Apollo.from<SubscribeAddTaskSubscription.Data>(subscriptionCall!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSubscriber<Response<SubscribeAddTaskSubscription.Data>>() {
                    override fun onNext(response: Response<SubscribeAddTaskSubscription.Data>) {

                        val res = response.data()?.taskAdded()
                        res?.let {

                            Log.e(TAG, " inside subscription2 ${it.title()} mutated upon new title")
//
//                            runOnUiThread {
//                                noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
//                                taskAdapter.notifyDataSetChanged()
//                            }
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "Subscription2 response received",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.message, e)
                        Toast.makeText(this@MainActivity, "Subscription2 failure", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onComplete() {
                        Log.d(TAG, "Subscription2 exhausted")
                        Toast.makeText(this@MainActivity, "Subscription2 complete", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
        )
    }

    /*
     In the activity's onStop(), schedule a worker that would try to replicate the mutations done when offline (stored in database )
     to the server whenever network connection is regained.
     */
    override fun onStop() {
        /* Schedule a worker by calling scheduleWorker() of the library.
           @param Worker class which extend OffixWorker class of the library.
        */
        scheduleWorker(SampleWorker::class.java)
        super.onStop()
    }

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        disposables.dispose()
        super.onDestroy()
    }
}

