package org.aerogear.offix

// Imports
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4


import org.junit.Test
import org.junit.runner.RunWith

import  org.junit.Assert.*

import org.aerogear.offix.persistence.Mutation
import org.json.JSONObject
import com.apollographql.apollo.api.OperationName

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // Test Mutation
    private val testOperation = OperationName { "testOperation" }
    private val testJson = JSONObject("{'value':'test'}")

    private val testMutation = Mutation(operationId = "1",queryDoc = "test",operationName = testOperation,
                                        valueMap = testJson,responseClassName = "testClass")

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("org.aerogear.offix.test", appContext.getPackageName())
    }

    // Check Delete All Mutations
    @Test
    fun CheckDeleteAllMutation(){
        getDao()?.deleteAllMutations()
        assertEquals(0,getDao()?.getAllMutations()?.size)
    }

    // Check Mutation Insert and Get A Mutation Method
    @Test
    fun CheckInsertMutation(){

        getDao()?.deleteAllMutations()

        // Insert the mutation and get the snNo And Convert it into Non-nullable Int
        val snNo = getDao()?.insertMutation(testMutation)?.toInt()!!
        val testMutation = getDao()?.getAMutation(snNo)

        assertEquals("1",testMutation?.operationId)
    }


    // Check Delete Mutation by snNo
    @Test
    fun CheckDeleteCurrentMutation(){

        getDao()?.deleteAllMutations()
        val snNo = getDao()?.insertMutation(testMutation)?.toInt()!!

        assertEquals(1,getDao()?.getAllMutations()?.size)

        getDao()?.deleteCurrentMutation(snNo)

        assertEquals(0,getDao()?.getAllMutations()?.size)
    }

    // Check Delete Mutation
    @Test
    fun CheckDeleteMutation(){

        getDao()?.deleteAllMutations()

        val snNo = getDao()?.insertMutation(testMutation)?.toInt()!!

        assertEquals(1,getDao()?.getAllMutations()?.size)

        val testMutation = getDao()?.getAMutation(snNo)!!
        getDao()?.deleteMutation(testMutation)

        assertEquals(0,getDao()?.getAllMutations()?.size)
    }

}
