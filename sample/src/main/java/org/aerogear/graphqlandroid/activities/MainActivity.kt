package org.aerogear.graphqlandroid.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertfrag_create_tasks.view.*
import org.aerogear.graphqlandroid.FindAllTasksQuery
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.Utils

import org.aerogear.graphqlandroid.managers.MutationManager.createTask;
import org.aerogear.graphqlandroid.managers.SubscriptionManager.subscriptionNewTask
import org.aerogear.graphqlandroid.managers.SubscriptionManager.subscriptionNewUser
import org.aerogear.graphqlandroid.managers.SubscriptionManager.subscriptionUpdateTask
import org.aerogear.graphqlandroid.managers.SubscriptionManager.subscriptionUpdateUser
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.UserOutput
import org.aerogear.offix.Offline

class MainActivity : AppCompatActivity() {
    var tasksList = arrayListOf<UserOutput>()
    private val TAG = javaClass.simpleName
    val taskAdapter by lazy {
        TaskAdapter(tasksList, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAdd.setOnClickListener {
            //Used for creating a new task
            val inflatedView =
                LayoutInflater.from(this).inflate(R.layout.alertfrag_create_tasks, null, false)
            val ilTitleTask = inflatedView.ilTitleTask
            val etTitleTask = inflatedView.etTitleTask
            etTitleTask.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if(s.toString().isNotEmpty()){
                        ilTitleTask.error = null
                    }
                }

            })
            val customAlert: AlertDialog = AlertDialog.Builder(this)
                .setView(inflatedView)
                .setTitle(R.string.new_task)
                .setNegativeButton(android.R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(android.R.string.yes, null)
                .create()
            customAlert.setOnShowListener {
                val positiveButton = customAlert.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val title = etTitleTask.text.toString()
                    if(title.isEmpty()){
                        ilTitleTask.error = resources.getString(R.string.task_title_error)
                    } else {
                        val desc = inflatedView.etDescTask.text.toString()
                        createTask(title, desc, this)
                        customAlert.dismiss()
                    }
                }
            }
            customAlert.show()
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter

        getTasks()

        pull_to_refresh.setOnRefreshListener {
            doSampleUpdate()
            pull_to_refresh.isRefreshing = false
        }
    }

    private fun doSampleUpdate() {
        Log.e(TAG, " --------------- doSampleUpdate")
        tasksList.clear()
        getTasks()
    }
    private fun getTasks() {
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

                        result?.forEach { allTasks ->
                            val title = allTasks.title()
                            val desc = allTasks.description()
                            val id = allTasks.id()
                            var firstName = ""
                            var lastName = ""
                            var email = ""
                            var userId = ""
                            allTasks.assignedTo()?.let { query ->
                                firstName = query.firstName()
                                lastName = query.lastName()
                                email = query.email()
                                userId = query.id()
                            } ?: kotlin.run {
                                firstName = ""
                                lastName = ""
                                email = ""
                                userId = ""
                            }
                            val taskOutput = UserOutput(
                                title,
                                desc,
                                id.toInt(),
                                firstName,
                                lastName,
                                userId,
                                email
                            )
                            runOnUiThread {
                                tasksList.add(taskOutput)
                                taskAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                })
        }
    }

    override fun onStart() {
        if (Offline.isNetwork()) {
            subscriptionNewTask(this)
            subscriptionNewUser(this)
            subscriptionUpdateTask(this)
            subscriptionUpdateUser(this)
        }
        super.onStart()
    }
}

