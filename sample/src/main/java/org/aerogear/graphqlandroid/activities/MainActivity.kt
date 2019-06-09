package org.aerogear.graphqlandroid.activities

import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.api.OperationName
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.ApolloStore
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.model.Task

class MainActivity : AppCompatActivity() {


    val noteslist = arrayListOf<Task>()
    val TAG = javaClass.simpleName
    lateinit var apolloStore: ApolloStore
    val taskAdapter by lazy {
        TaskAdapter(noteslist, this)
    }
    val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val apolloSqlHelpersize by lazy {
        ApolloSqlHelper.TABLE_RECORDS.length
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork != null && activeNetwork.isConnected) {
            Log.e(TAG, " User is online ")
            Log.e(TAG, "  apolloSql size: $apolloSqlHelpersize")
            getTasks()

        }

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = taskAdapter


        /**  Hidden the fab action to insert a new note and
         *   just to display the tasks received from the query
         */

        fabAdd.setOnClickListener {

            val inflatedView = LayoutInflater.from(this).inflate(R.layout.alertfrag_create, null, false)

            val customAlert: AlertDialog = AlertDialog.Builder(this)
                .setView(inflatedView)
                .setTitle("Create a new Note")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    val title = inflatedView.etTitle.text.toString()
                    val desc = inflatedView.etDesc.text.toString()
                    createtask(title, desc)
                    Log.e(TAG, "---")
//                    taskAdapter.notifyItemInserted(noteslist.size - 1)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()

        }

    }

    fun getTasks() {

        Log.e(TAG, "inside getTasks")

        val client = Utils.getApolloClient(this)?.query(
            AllTasksQuery.builder().build()
        )

        //Utils.getApolloClient(this)?.clearNormalizedCache()

        Log.e(TAG, " getActiveCallsCount : ${Utils.getApolloClient(this)?.activeCallsCount()}")

        apolloStore = Utils.getApolloClient(this)?.apolloStore()!!

        Log.e(TAG, " apolloStore : ${Utils.getApolloClient(this)?.apolloStore()}")

        client?.enqueue(object : ApolloCall.Callback<AllTasksQuery.Data>() {

            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
                Log.e(TAG, e.toString())
            }

            override fun onResponse(response: Response<AllTasksQuery.Data>) {

                val result = response.data()?.allTasks()
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
                runOnUiThread {
                    Log.e(TAG, " Size ${noteslist.size}")
                    taskAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    fun updateTask(id: String, title: String, version: Int) {

        Log.e(TAG, "inside update task")

        val client: ApolloMutationCall<UpdateCurrentTask.Data>?

        val mutation = UpdateCurrentTask.builder().id(id).title(title).version(version).build()

        client = Utils.getApolloClient(this)?.mutate(mutation)?.refetchQueries(object : OperationName {
            override fun name(): String {
                return AllTasksQuery.OPERATION_DEFINITION
            }

        })
        Log.e(TAG, " getActiveCallsCount update Task : ${Utils.getApolloClient(this)?.activeCallsCount()}")

        client?.enqueue(
            object : ApolloCall.Callback<UpdateCurrentTask.Data>() {
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

                    runOnUiThread {
                        noteslist.clear()
                        getTasks()
                    }
                }
            })
    }

    private fun createtask(title: String, description: String) {

        Log.e(TAG, "inside create task")

        val client = Utils.getApolloClient(this)?.mutate(
            CreateTask.builder().title(title).description(description).build()
        )?.refetchQueries(object : OperationName {
            override fun name(): String {
                return AllTasksQuery.OPERATION_DEFINITION
            }
        })

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

                getTasks()

                runOnUiThread {
                    noteslist.clear()
                    getTasks()
                    Log.e(TAG, "createTask : ${noteslist.size}")
                }
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
}
