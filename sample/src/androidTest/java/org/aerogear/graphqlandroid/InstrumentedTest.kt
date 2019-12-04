package org.aerogear.graphqlandroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.aerogear.offix.getDao

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.runners.JUnit4

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(JUnit4::class)
class InstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val args = InstrumentationRegistry.getArguments()
        assertEquals("com.lavanya.graphqlandroid", appContext.packageName)
        assertEquals(0.1010001, args.getDouble("0.1010001"))
        assertEquals(true, "test".contains("s"))
        assertEquals("TasksApp", appContext.resources.getString(R.string.app_name)) //TODO: change it if you ever change the app name
        assertEquals(0, getDao()?.getAllMutations()?.size);
    }
}
