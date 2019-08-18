package org.aerogear.graphqlandroid.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.aerogear.graphqlandroid.*
import org.aerogear.graphqlandroid.adapter.PagerAdapter
import org.aerogear.graphqlandroid.fragments.FragmentTasks
import org.aerogear.graphqlandroid.fragments.FragmentUsers

class MainActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"MainActivity Started")

        val fragmentslist = arrayListOf<Fragment>()

        fragmentslist.add(FragmentTasks())
        fragmentslist.add(FragmentUsers())

        val pagerAdapeter = PagerAdapter(fragmentslist, supportFragmentManager)
        viewPager.adapter = pagerAdapeter
        tablayout.setupWithViewPager(viewPager)
    }
}

