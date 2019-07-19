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
import androidx.work.Constraints
import androidx.work.NetworkType
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.pull_to_refresh
import kotlinx.android.synthetic.main.activity_main.recycler_view
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.data.ViewModel
import org.aerogear.graphqlandroid.model.Task
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
    val dbDao = MyApplication.database.mutationDao()
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

        //Used for persistent mutations
        persistence.setOnClickListener {

            Toast.makeText(this, "FromDB button clicked", Toast.LENGTH_SHORT).show()


            Log.e(TAG, "in persistence")
            val listOfMutations = dbDao.getAllMutations()
            Log.e(TAG, "in persistence size of list : ${listOfMutations.size}")

            listOfMutations.forEach { storedmutation ->

                //Get the class name of the mutation to which it has to be mapped
                val responseClassName = storedmutation.responseClassName

                val classReflected: Class<*> = Class.forName(responseClassName)

                //Get the constructor of the class, and as apollo generated classes have only one constructor, so take the first one.
                val constructor = classReflected.constructors.first()

                //Get the parameterTypes
                val parameters = constructor.parameterTypes

                val jsonValues = arrayListOf<Any>()

                //Get the json object i.e the variables map given as input by the user
                val jsonObj = storedmutation.valuemap

                //Put all the json values into a list
                val iter = jsonObj.keys()
                iter.forEach { key ->
                    jsonValues.add(jsonObj.get(key))
                }

                Log.e("jsonValuesList ", " ${jsonValues.size}")

                jsonValues.forEach {
                    Log.e("jsonValuesList : ", " $it")
                }

                //Check if the input parameter is of type Input<*>, if yes typecast it to be of the type Input<*>
                parameters.forEachIndexed { index, clazz ->
                    //                println(clazz.name)
                    Log.e("parameters : ", " ${clazz.name}")
                    if (clazz.name.equals("com.apollographql.apollo.api.Input")) {
                        jsonValues[index] = Input.optional(jsonValues[index])
                        Log.e("parameters **: ", " ${jsonValues[index].javaClass.name}")

                    }
                }

                //Make an object of mutation (done by reflection)
                val obj = constructor.newInstance(*jsonValues.toArray())

                //Make an apollo client which takes in mutation object and makes a call to server.
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
            })?.refetch()

        pull_to_refresh.isRefreshing = false
    }

    fun getTasks() {
        Log.e(TAG, " ----- getTasks")
        noteslist = myModel.getAll()
        taskAdapter.notifyDataSetChanged()
    }

    fun updateTask(id: String, title: String, version: Int) {
        Log.e(TAG, "inside update title in MainActivity")
        myModel.update(id, title, version)
    }

    fun createtask(title: String, description: String) {
        Toast.makeText(this, "Mutation with title $title created", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "inside create title")
        myModel.create(title, description)
    }

    fun deleteTask(id: String) {
        Log.e(TAG, "inside delete task")
        myModel.delete(id)
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

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        disposables.dispose()
        super.onDestroy()
    }
}

