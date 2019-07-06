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
import androidx.work.*
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.pull_to_refresh
import kotlinx.android.synthetic.main.activity_main.recycler_view
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import okhttp3.*
import okhttp3.internal.Util
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.data.UserData
import org.aerogear.graphqlandroid.data.ViewModel
import org.aerogear.graphqlandroid.model.Task
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList

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

    val dbDao = MyApplciation.database.mutationDao()

    private val disposables = CompositeDisposable()

    val watchResponse = AtomicReference<Response<AllTasksQuery.Data>>()

    val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    val myModel: ViewModel by lazy {
        ViewModelProviders.of(this).get(ViewModel::class.java)
    }

    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null && activeNetwork.isConnected) {
            Log.e(TAG, " User is online ")

            getTasks()

            subscribeUpdatedTaskAdded()
            subscribeNewTaskAdded()
        } else {

            getTasks()
            Toast.makeText(
                this@MainActivity,
                "Swipe down to refersh. No network there, so items fethced from db.",
                Toast.LENGTH_SHORT
            ).show()
        }

        pull_to_refresh.setOnRefreshListener {
            doYourUpdate()
            pull_to_refresh.isRefreshing = false
        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        //Used for in-memory mutations
        buttonOffline.setOnClickListener {

            Log.e(TAG, "buttonOffline clicked")
            OfflineMutationSender()
        }

        //Used for persistent mutations
        persistence.setOnClickListener {


            Log.e(TAG, "in persistence")
            val listOfMutations = dbDao.getAllMutations()
            Log.e(TAG, "in persistence size of list : ${listOfMutations.size}")

            listOfMutations.forEach { storedmutation ->

                val responseClassName = storedmutation.responseClassName

                val classReflected: Class<*> = Class.forName(responseClassName)

                val constructor = classReflected.constructors.first()

                val parameters = constructor.parameterTypes

                val jsonValues = arrayListOf<Any>()

                //Put all the json values into a list
                val jsonObj = storedmutation.valuemap

                val iter = jsonObj.keys()
                iter.forEach { key ->
                    jsonValues.add(jsonObj.get(key))
                }

                Log.e("jsonValuesList ", " ${jsonValues.size}")

                jsonValues.forEach {
                    Log.e("jsonValuesList : ", " $it")
                }


                parameters.forEachIndexed { index, clazz ->
                    //                println(clazz.name)
                    Log.e("parameters : ", " ${clazz.name}")
                    if (clazz.name.equals("com.apollographql.apollo.api.Input")) {
                        jsonValues[index] = Input.optional(jsonValues[index])
                        Log.e("parameters **: ", " ${jsonValues[index].javaClass.name}")

                    }
                }

                val obj = constructor.newInstance(*jsonValues.toArray())

                val client = Utils.getApolloClient(this)?.mutate(
                    obj as Mutation<Operation.Data, Operation.Data, Operation.Variables>
                )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

                client?.enqueue(object : ApolloCall.Callback<Operation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(response: Response<Operation.Data>) {
                        Log.e(TAG, response.hasErrors().toString())
                        Log.e(TAG, response.data().toString())
                        Log.e(TAG, response.errors().toString())

                        dbDao.deleteCurrentMutation(storedmutation.SNo)
                    }

                })
            }


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
                    Log.e(TAG, "jhjj")
//                    taskAdapter.notifyItemInserted(noteslist.size - 1)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()

        }
    }

    //When app is in foreground, in-memory approach used and mutations fetched from arraylist ( no resposne is received )
    fun OfflineMutationSender() {

        Log.e(TAG, "OfflineMutationSender inside")

        val arrayList = myModel.getOfflineList()
        Log.e(TAG, "OfflineMutationSender  :  ${arrayList.size} ")

        arrayList.forEach {

            Log.e(TAG, "OfflineMutationSender 1 : $it ")
            val client = Utils.getApolloClient(this)?.mutate(
                it
            )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

            client?.enqueue(object : CustomApolloCall.CustomCallback() {
                override fun onResponse(response: Response<Void>) {
                    arrayList.remove(it)
                }

                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, "OfflineMutationSender 3: ${e.message} ")
                }
            })
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
//                    )

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

        runOnUiThread {
            taskAdapter.notifyDataSetChanged()
        }
        pull_to_refresh.isRefreshing = false
    }

    fun getTasks() {

        Log.e(TAG, " ----- getTasks")
        noteslist.clear()
        noteslist = myModel.getAll()
        taskAdapter.notifyDataSetChanged()
    }

    fun updateTask(id: String, title: String, version: Int) {

        Log.e(TAG, "inside update title in MainActivity")
        myModel.update(id, title, version)
//        noteslist.clear()
    }

    fun createtask(title: String, description: String) {

        Log.e(TAG, "inside create title")
        myModel.create(title, description)
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
                            //  noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
//                            noteslist.add(
//                                it.id().toInt() - 1,
//                                Task(it.title(), it.description(), it.id().toInt(), it.version()!!)
//                            )
                            //taskAdapter.notifyDataSetChanged()
                        }
//
//                        runOnUiThread {
//                            taskAdapter.notifyDataSetChanged()
//                        }
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
                            noteslist.add(Task(it.title(), it.description(), it.id().toInt(), it.version()!!))
                        }
//
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

