package org.aerogear.graphqlandroid.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_update_task.view.*
import kotlinx.android.synthetic.main.alert_update_user.view.*
import kotlinx.android.synthetic.main.alertfrag_create_tasks.view.*
import kotlinx.android.synthetic.main.alertfrag_create_user.view.*
import kotlinx.android.synthetic.main.alertfrag_create_user.view.etTitleUser
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.NamePair
import org.aerogear.graphqlandroid.model.UserOutput
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserFilter
import org.aerogear.graphqlandroid.type.UserInput
import org.aerogear.offix.Offline
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {

    var tasksList = arrayListOf<UserOutput>()
    val TAG = javaClass.simpleName
    val taskAdapter by lazy {
        TaskAdapter(tasksList, this)
    }

    private val disposables = CompositeDisposable()
    var apolloQueryWatcher: ApolloQueryWatcher<FindAllTasksQuery.Data>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        getTasks()

        pull_to_refresh.setOnRefreshListener {
            doSampleUpdate()
            pull_to_refresh.isRefreshing = false
        }
    }

    fun doSampleUpdate() {
        Log.e(TAG, " --------------- doSampleUpdate")
        tasksList.clear()
        getTasks()
    }

    fun getTasks() {
        Log.e(TAG, " ----- getTasks")

        tasksList.clear()

        FindAllTasksQuery.builder()?.build()?.let {
            Utils.getApolloClient(this)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindAllTasksQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                        Log.e(TAG, "getTasks ----$e ")
                    }

                    override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                        Log.e(TAG, "on Response getTasks : Data ${response.data()}")
                        val result = response.data()?.findAllTasks()

                        result?.forEach {
                            val title = it.title()
                            val desc = it.description()
                            val id = it.id()
                            var firstName = ""
                            var lastName = ""
                            var email = ""
                            var userId = ""
                            it.assignedTo()?.let {
                                firstName = it.firstName()
                                lastName = it.lastName()
                                email = it.email()
                                userId = it.id().toString()
                            } ?: kotlin.run {
                                firstName = "User Not assigned"
                                lastName = ""
                                email = ""
                                userId = ""
                            }

                            Log.e(TAG, "$firstName $email $title $desc")
                            val taskOutput = UserOutput(title, desc, id.toInt(), firstName, lastName, userId, email)
                            runOnUiThread {
                                tasksList.add(taskOutput)
                                taskAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                })
        }
    }

    fun getUser(id: Int): NamePair {
        Log.e("${TAG} Inside getUser", "TaskId: $id")
        var namePair = NamePair("", "")
        val userFilter = UserFilter.builder().id(id.toString()).build()
        FindUsersQuery.builder().fields(userFilter).build()?.let {
            Utils.getApolloClient(this)?.query(it)?.responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindUsersQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(response: Response<FindUsersQuery.Data>) {
                        Log.e(TAG, "on Response : response.data ${response.data()}")
                        val result = response.data()?.findUsers()

                        result?.forEach {
                            var firstName = it.firstName()
                            var lastName = it.lastName()
                            namePair.fName = firstName
                            namePair.lName = lastName
                        }
                    }
                })
        }
        return namePair
    }

    fun updateTask(id: String, title: String, description: String) {
        Log.e(TAG, "inside update title in MainActivity")

        /*
        As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
        */
        val input = TaskInput.builder().title(title).version(1).description(description).status("test").build()

        var mutation = UpdateTaskMutation.builder().id(id).input(input).build()

        Log.e(TAG, " updateTask ********: - $mutation")

        val mutationCall = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " updateTask class name: - ${mutation.javaClass.simpleName}")
        Log.e(
            TAG,
            " updateTask 20: - ${mutationCall?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " updateTask 21: - ${mutationCall?.operation()?.queryDocument()}")
        Log.e(TAG, " updateTask 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        Log.e(TAG, " updateTask 23: - ${mutationCall?.operation()?.name()}")

        val callback = object : ApolloCall.Callback<UpdateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                val result = response.data()?.updateTask()

                //In case of conflicts data returned from the server taskId null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(this@MainActivity, "Task with $id is updated", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Task with $id is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    fun updateUser(idUser: String, taskId: String, title: String, firstName: String, lastName: String, email: String) {
        Log.e(TAG, "inside updateUser in MainActivity")

        val input = UserInput.builder().title(title).lastName(lastName).firstName(firstName).email(email).taskId(taskId)
            .creationmetadataId(taskId).build()

        var mutation = UpdateUserMutation.builder().id(idUser).input(input).build()

        Log.e(TAG, " updateUser ********: - $mutation")

        val mutationCall = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " updateUser class name: - ${mutation.javaClass.simpleName}")
        Log.e(
            TAG,
            " updateUser 20: - ${mutationCall?.requestHeaders(com.apollographql.apollo.request.RequestHeaders.builder().build())}"
        )
        Log.e(TAG, " updateUser 21: - ${mutationCall?.operation()?.queryDocument()}")
        Log.e(TAG, " updateUser 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        Log.e(TAG, " updateUser 23: - ${mutationCall?.operation()?.name()}")

        val callback = object : ApolloCall.Callback<UpdateUserMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<UpdateUserMutation.Data>) {
                val result = response.data()?.updateUser()

                //In case of conflicts data returned from the server taskId null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(this, "User with $idUser is updated", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Updated in User with $idUser is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    fun createtask(title: String, description: String) {
        Log.e(TAG, "inside create title")

        /*
         As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
         */
        val input = TaskInput.builder().title(title).description(description).version(1).status("test").build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        val mutationCall = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " updateTask 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")


        val callback = object : ApolloCall.Callback<CreateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                val result = response.data()?.createTask()

                //In case of conflicts data returned from the server taskId null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)

        if (Offline.isNetwork()) {
            Toast.makeText(this, "Mutation with title $title is created", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Create mutation with title $title is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun createUser(title: String, firstName: String, lastName: String, email: String, taskId: String) {
        Log.e(TAG, "inside create user")

        val input = UserInput.builder().taskId(taskId).email(email).firstName(firstName).lastName(lastName).title(title)
            .creationmetadataId(taskId).build()

        val mutation = CreateUserMutation.builder().input(input).build()

        val mutationCall = Utils.getApolloClient(this)?.mutate(
            mutation
        )?.refetchQueries(apolloQueryWatcher?.operation()?.name())

        Log.e(TAG, " createUser 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        val callback = object : ApolloCall.Callback<CreateUserMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
            }

            override fun onResponse(response: Response<CreateUserMutation.Data>) {
                val result = response.data()?.createUser()

                //In case of conflicts data returned from the server taskId null.
                result?.let {
                    Log.e(TAG, "onResponse-UpdateTask- $it")
                }
            }
        }
        mutationCall?.enqueue(callback)
        if (Offline.isNetwork()) {
            Toast.makeText(this, "Task with taskId $taskId is assigned to $firstName $lastName", Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(
                this,
                "Update in user where Task with taskId $taskId is assigned to $firstName $lastName is stored offline. Changes will be synced to the server when app comes online.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun subscriptionNewTask() {
        val subscription = NewTaskSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<NewTaskSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<NewTaskSubscription.Data>>() {
                        override fun onNext(response: Response<NewTaskSubscription.Data>) {
                            val res = response.data()?.newTask()
                            res?.let {
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "Subscription new task added exhausted")
                        }
                    }
                )
        )
    }

    fun subscriptionUpdateTask() {

        val subscription = UpdatedTaskSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(Rx2Apollo.from<UpdatedTaskSubscription.Data>(subscriptionCall!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSubscriber<Response<UpdatedTaskSubscription.Data>>() {
                    override fun onNext(response: Response<UpdatedTaskSubscription.Data>) {
                        val res = response.data()?.updatedTask()
                        res?.let {
                            Log.e(TAG, " inside subscriptionUpdateTask ${it.title()} mutated upon updating")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.message, e)
                    }

                    override fun onComplete() {
                        Log.e(TAG, "subscriptionUpdateTask exhausted")
                    }
                }
            )
        )
    }

    fun subscriptionNewUser() {
        val subscription = NewUserSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(
            Rx2Apollo.from<NewUserSubscription.Data>(subscriptionCall!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                    object : DisposableSubscriber<Response<NewUserSubscription.Data>>() {
                        override fun onNext(response: Response<NewUserSubscription.Data>) {
                            val res = response.data()?.newUser()
                            res?.let {
                                Log.e(TAG, " inside subscriptionNewUser ${it.title()} mutated upon new title")
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, e.message, e)
                        }

                        override fun onComplete() {
                            Log.e(TAG, "Subscription new user added exhausted")
                        }
                    }
                )
        )
    }

    fun subscriptionUpdateUser() {

        val subscription = UpdatedUserSubscription()
        val subscriptionCall = Utils.getApolloClient(this)
            ?.subscribe(subscription)

        disposables.add(Rx2Apollo.from<UpdatedUserSubscription.Data>(subscriptionCall!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSubscriber<Response<UpdatedUserSubscription.Data>>() {
                    override fun onNext(response: Response<UpdatedUserSubscription.Data>) {

                        val res = response.data()?.updatedUser()
                        res?.let {
                            Log.e(TAG, " inside subscriptionUpdateUser ${it.title()} mutated upon updating")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.message, e)
                    }

                    override fun onComplete() {
                        Log.e(TAG, "subscriptionUpdateUser exhausted")
                    }
                }
            )
        )
    }

    override fun onStart() {
        if (Offline.isNetwork()) {
            subscriptionNewTask()
            subscriptionNewUser()
            subscriptionUpdateTask()
            subscriptionUpdateUser()
        }
        super.onStart()
    }

    override fun onDestroy() {
        apolloQueryWatcher?.cancel()
        disposables.dispose()
        super.onDestroy()
    }
}

