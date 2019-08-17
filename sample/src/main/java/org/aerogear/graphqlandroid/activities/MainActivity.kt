package org.aerogear.graphqlandroid.activities

import android.os.Bundle
import android.support.v4.app.Fragment
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
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_task.view.etDescTask
import kotlinx.android.synthetic.main.alertdialog_task.view.etTitleTask
import kotlinx.android.synthetic.main.alertfrag_createtasks.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.PagerAdapter
import org.aerogear.graphqlandroid.adapter.TaskAdapter
import org.aerogear.graphqlandroid.fragments.Fragment_Tasks
import org.aerogear.graphqlandroid.fragments.Fragment_Users
import org.aerogear.graphqlandroid.model.Task
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.offix.enqueue
import org.aerogear.offix.interfaces.ResponseCallback
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"MainActivity Started")

        val fragmentslist = arrayListOf<Fragment>()

        fragmentslist.add(Fragment_Tasks())
        fragmentslist.add(Fragment_Users())

        val pagerAdapeter = PagerAdapter(fragmentslist, supportFragmentManager)
        viewPager.adapter = pagerAdapeter
        tablayout.setupWithViewPager(viewPager)
    }
}

