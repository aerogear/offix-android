package org.aerogear.offix

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test;
import org.junit.runner.RunWith;

import  org.junit.Assert.*

import org.aerogear.offix.persistence.MutationDao
import org.aerogear.offix.getDao
import org.aerogear.offix.persistence.Mutation

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("org.aerogear.offixsdk.test", appContext.getPackageName());
    }

    @Test
    fun AddMutation(){

        getDao()?.deleteAllMutations()
        val size = getDao()?.getAllMutations()?.size

        
    }
}
