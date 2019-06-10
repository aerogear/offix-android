package org.aerogear.graphqlandroid.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Context
import org.aerogear.graphqlandroid.model.Task

class ViewModel(application: Application) : AndroidViewModel(application) {

    val apcontext = application
    lateinit var tasksList: LiveData<List<Task>>

    fun getAll(): LiveData<List<Task>> {

        return UserData().getTasks(apcontext) as LiveData<List<Task>>

    }


    fun update(id: String, title: String, version: Int, context: Context) {

         UserData().updateTask(id, title, version, context)

    }


    fun create(title: String, description: String, context: Context) {

         UserData().createtask(title, description, context)

    }


}