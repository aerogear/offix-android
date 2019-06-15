package org.aerogear.graphqlandroid.activities

import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.data.ViewModel
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.persistence.Database
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class MainActivity : AppCompatActivity() {

    var noteslist = arrayListOf<Task>()
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

    val myModel: ViewModel by lazy {
        ViewModelProviders.of(this).get(ViewModel::class.java)
    }

    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null


    val updateDao by lazy {
        MyApplciation.database.updatDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null && activeNetwork.isConnected) {
            Log.e(TAG, " User is online ")
            Log.e(TAG, "  apolloSql size: $apolloSqlHelpersize")
            getTasks()

//            noteslist = myModel.getAll()

            subscribeUpdatedTaskAdded()
            subscribeNewTaskAdded()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Swipe down to refersh. No network there, so items fethced from db.",
                Toast.LENGTH_SHORT
            )
                .show()
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

//        noteslist.clear()
//
//        noteslist= myModel.doYourUpdate()
//
//        taskAdapter.notifyDataSetChanged()
//

        Utils.getApolloClient(this)?.query(
            AllTasksQuery.builder().build()
        )?.watcher()
            ?.refetchResponseFetcher(ApolloResponseFetchers.CACHE_FIRST)
            ?.enqueueAndWatch(object : ApolloCall.Callback<AllTasksQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    e.printStackTrace()
                    Log.e(TAG, "----$e ")
                }

                override fun onResponse(response: Response<AllTasksQuery.Data>) {

                    watchResponse.set(response)
                    noteslist.clear()

                    Log.e(TAG, "on Response : Watcher ${response.data()}")


                    val result = watchResponse.get()?.data()?.allTasks()
//                    Log.e(
//                        TAG,
//                        "onResponse-getTasks : ${result?.get(result.size - 1)?.title()} ${result?.get(result.size - 1)?.version()}"
//                    )

                    result?.forEach {
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
            })



        Log.e(TAG, " on Refersh : doneYourUpdate ")

        pull_to_refresh.isRefreshing = false
    }

    fun getTasks() {

        noteslist.clear()

        Log.e(TAG, "inside getTasks")

        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(this)?.activeCallsCount()}")

        apolloStore = Utils.getApolloClient(this)?.apolloStore()!!
        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(this)?.apolloStore()}")

        AllTasksQuery.builder()?.build()?.let {
            Utils?.getApolloClient(this)?.query(it)
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
                        runOnUiThread {
                            Log.e(TAG, " Size ${noteslist.size}")
                            taskAdapter.notifyDataSetChanged()
                        }
                    }
                })
        }

//         Log.e(TAG, "watched operation ${client?.operation()}")
    }

    fun updateTask(id: String, title: String, version: Int) {

        Log.e(TAG, "inside update title")

        val mutation = UpdateCurrentTaskMutation.builder().id(id).title(title).version(version).build()

        val client = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

//        Utils.getApolloClient(this)?.defaultCacheHeaders()

        client?.enqueue(object : ApolloCall.Callback<UpdateCurrentTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure--" + "updateTask", e.toString())

                //Save to db when offline
                doUpdateOffline(title, id, version)


            }

            override fun onResponse(response: Response<UpdateCurrentTaskMutation.Data>) {
                val result = response.data()?.updateTask()

                Log.e(TAG, "onResponse-UpdateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")

                runOnUiThread {
                    noteslist.clear()
                    Toast.makeText(this@MainActivity, " Swipe down to refresh", Toast.LENGTH_SHORT).show()
//                    getTasks()
                }
            }
        })
    }

    private fun doUpdateOffline(title: String, id: String, version: Int) {

        val task = Task(title, " ", id.toInt(), version)
        val dbId = updateDao.insertTask(task)

        Log.e(TAG, " Id in database $dbId ")
        Log.e(TAG, " Id of note fetched from db ${updateDao.getTaskById(dbId).id} ")
        Log.e(TAG, " Title of note fetched from db ${updateDao.getTaskById(dbId).title} ")

        noteslist.removeAt(id.toInt() - 1)
        noteslist.add(id.toInt() - 1, task)
        taskAdapter.notifyItemChanged(id.toInt() - 1)

        Log.e(TAG, "size of noteslist after update")

    }

    fun createtask(title: String, description: String) {

        Log.e(TAG, "inside create title")

        val client = Utils.getApolloClient(this)?.mutate(
            CreateTaskMutation.builder().title(title).description(description).build()
        )
            //?.refetchQueries(CreateTaskMutation.builder().title(title).description(description).build()?.name())
            ?.refetchQueries(apolloQueryWatcher?.operation()?.name())


        Utils?.getApolloClient(this)?.apolloStore()?.writeOptimisticUpdates(
            AllTasksQuery(), AllTasksQuery.Data(
                mutableListOf()
            ), UUID.randomUUID()

        )?.execute()


        client?.enqueue(object : ApolloCall.Callback<CreateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "createTask", e.toString())
            }

            override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                val result = response.data()?.createTask()

                Log.e(TAG, "onResponse-CreateTask")

                Log.e(TAG, "${result?.id()}")
                Log.e(TAG, "${result?.title()}")
                Log.e(TAG, "${result?.description()}")
                Log.e(TAG, "${result?.version()}")

                runOnUiThread {
                    if (!noteslist.isEmpty())
                        noteslist.clear()
//                    Thread.sleep(5000)
//                    Log.e(TAG," Sleep over")
                }
            }
        })


    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete title")

        val client = Utils.getApolloClient(this)?.mutate(
            DeleteTaskMutation.builder().id(id).build()
        )
        client?.enqueue(object : ApolloCall.Callback<DeleteTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure" + "deleteTask", e.toString())
            }

            override fun onResponse(response: Response<DeleteTaskMutation.Data>) {
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


    private fun subscribeUpdatedTaskAdded() {

        val subscription = SubscribeTasksSubscription()
        val subscriptionCall = Utils?.getApolloClient(this)
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
                            noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
                        }

                        runOnUiThread {
                            taskAdapter.notifyDataSetChanged()
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
        val subscriptionCall = Utils?.getApolloClient(this)
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
                            noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
                        }

                        runOnUiThread {
                            taskAdapter.notifyDataSetChanged()
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


    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        disposables.dispose()
        super.onDestroy()
    }

}

