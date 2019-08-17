package org.aerogear.graphqlandroid.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/*
Class to inflate the fragments in the viewpager.
 */
class PagerAdapter(val fragments: ArrayList<Fragment>, val fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Tasks"
            1 -> "Users"
            else -> ""
        }
    }
}