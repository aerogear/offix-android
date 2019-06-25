package org.aerogear.graphqlandroid.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.android.synthetic.main.activity_main.*
import org.aerogear.graphqlandroid.AllTasksQuery
import org.aerogear.graphqlandroid.Utils
import org.aerogear.graphqlandroid.model.Task
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList

class ViewModel(application: Application) :
    AndroidViewModel(application) {

    val apcontext = application

    lateinit var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>

    lateinit var tasksList: LiveData<List<Task>>

    val TAG = javaClass.simpleName

    fun getAll(): ArrayList<Task> {

        Log.e(TAG, "${UserData(apcontext).getTasks().size}")
        return UserData(apcontext).getTasks()

    }


    fun update(id: String, title: String, version: Int) {

        UserData(apcontext).updateTask(id, title, version)

    }


    fun create(title: String, description: String) {

        UserData(apcontext).createtask(title, description)

    }

    fun getOfflineList() : ArrayList<com.apollographql.apollo.api.Mutation<Operation.Data,Void, Operation.Variables>> = UserData(apcontext).OfflineArraylist()

    fun doYourUpdate(): ArrayList<Task> {
        return UserData(apcontext).doYourUpdate()
    }

}