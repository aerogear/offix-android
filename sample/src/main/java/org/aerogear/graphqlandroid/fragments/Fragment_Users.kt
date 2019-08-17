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
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.android.synthetic.main.alertfrag_createuser.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.fragment_users.view.*
import org.aerogear.graphqlandroid.CreateUserMutation
import org.aerogear.graphqlandroid.FindAllUsersQuery
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.Utils
import org.aerogear.graphqlandroid.adapter.UserAdapter
import org.aerogear.graphqlandroid.model.User
import org.aerogear.graphqlandroid.type.UserInput
import org.aerogear.offix.enqueue
import org.aerogear.offix.interfaces.ResponseCallback
import java.util.concurrent.atomic.AtomicReference

class Fragment_Users : Fragment() {

    var userList = arrayListOf<User>()
    val TAG = javaClass.simpleName
    val userAdapter by lazy {
        activity?.baseContext?.let { UserAdapter(userList, it) }
    }

    val watchResponse = AtomicReference<Response<FindAllUsersQuery.Data>>()
    var apolloQueryWatcher: ApolloQueryWatcher<FindAllUsersQuery.Data>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.recycler_view_users.layoutManager = LinearLayoutManager(activity)
        view.recycler_view_users.adapter = userAdapter

        pull_to_refresh_users.setOnRefreshListener {
            doUserUpdate()
            pull_to_refresh_tasks.isRefreshing = false
        }

        //Used for creating a new task
        insertbutton_users.setOnClickListener {
            val inflatedView = LayoutInflater.from(activity).inflate(R.layout.alertfrag_createuser, null, false)
            val customAlert: AlertDialog? = activity?.baseContext?.let { it1 ->
                android.support.v7.app.AlertDialog.Builder(it1)
                    .setView(inflatedView)
                    .setTitle("Create a User!")
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { dialog, which ->
                        val taskId = inflatedView.etTaskIdUser.text.toString()
                        val title=inflatedView.etTitleUser.text.toString()
                        val firstName=inflatedView.etFirstName.text.toString()
                        val lastName=inflatedView.etLastName.text.toString()
                        val email=inflatedView.etEmail.text.toString()
                        createUser(title,firstName,lastName,email,taskId)
                        dialog.dismiss()
                    }
                    .create()
            }
            customAlert?.show()
        }
    }

    fun doUserUpdate() {

        Log.e(TAG, " -*-*-*- doYourUpdate")
        userList.clear()

        activity?.baseContext?.let {
            Utils.getApolloClient(it)?.query(
                FindAllUsersQuery.builder().build()
            )?.watcher()
                ?.refetchResponseFetcher(ApolloResponseFetchers.CACHE_AND_NETWORK)
                ?.enqueueAndWatch(object : ApolloCall.Callback<FindAllUsersQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                        Log.e(TAG, " doYourUpdate onFailure----$e ")
                    }

                    override fun onResponse(response: Response<FindAllUsersQuery.Data>) {

                        watchResponse.set(response)

                        Log.e(TAG, "on Response doYourUpdate: Watcher ${response.data()}")

                        val result = watchResponse.get()?.data()?.findAllUsers()
                        result?.forEach {
                            val title = it.title()
                            val id = it.taskId()
                            val email = it.email()
                            val firstName = it.firstName()
                            val lastName = it.lastName()
                            val user = User(id.toInt(),firstName,lastName,email,title)
                            activity?.runOnUiThread {
                                userList.add(user)
                                userAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                })
        }

        pull_to_refresh_tasks.isRefreshing = false
    }

    fun createUser(title: String, firstName: String, lastName: String, email: String, taskId: String) {
        Log.e(TAG, "inside create user")

        /*
         As version is assumed to be auto incremented ( //TODO Have to make changes in sqlite db)
         */
        val input = UserInput.builder().taskId(taskId).email(email).firstName(firstName).lastName(lastName).title(title)
            .creationmetadataId(taskId).build()

        val mutation = CreateUserMutation.builder().input(input).build()

        val client = activity?.baseContext?.let {
            Utils.getApolloClient(it)?.mutate(
                mutation
            )?.refetchQueries(apolloQueryWatcher?.operation()?.name())
        }

        Log.e(TAG, " createUser 22: - ${client?.operation()?.variables()?.valueMap()}")

        val customCallback = object : ResponseCallback {

            override fun onSuccess(response: Response<Any>) {
                Log.e("onSuccess() createTask", "${response.data()}")
            }

            override fun onSchedule(e: ApolloException, mutation: Mutation<Operation.Data, Any, Operation.Variables>) {
                e.printStackTrace()
                Log.e("onSchedule() createUser", "${mutation.variables().valueMap()}")
            }
        }

        activity?.baseContext?.let {
            Utils.getApolloClient(it)?.enqueue(
                mutation as com.apollographql.apollo.api.Mutation<Operation.Data, Any, Operation.Variables>,
                customCallback
            )
        }

        Toast.makeText(activity, "Task with id $taskId is assigned to $firstName $lastName", Toast.LENGTH_SHORT).show()
    }


}