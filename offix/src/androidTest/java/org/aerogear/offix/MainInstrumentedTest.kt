package org.aerogear.offix

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.aerogear.offix.Offline.Companion.getDb
import org.aerogear.offix.persistence.Mutation
import org.aerogear.offix.type.TaskInput
import org.json.JSONObject

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val input = TaskInput(Input.optional(0), "title", "desc", "test")
    val mutation = CreateTaskMutation(input)
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("org.aerogear.offixsdk.test", appContext.packageName)
    }

    @Test
    fun addMutationCheck(){
        getDb()?.mutationDao()?.deleteAllMutations()
        var a = getDb()?.mutationDao()?.getAllMutations()?.size
        assertEquals(0, a)
        val m = Mutation(mutation.operationId(), mutation.queryDocument(), mutation.name(), JSONObject(mutation.variables().valueMap()), mutation.responseFieldMapper().javaClass.simpleName)
        getDb()!!.mutationDao().insertMutation(m)
        a = getDb()?.mutationDao()?.getAllMutations()?.size
        assertEquals(1, a)
    }

    @Test
    fun discreteObjectCheck(){
        val m1 = Mutation(mutation.operationId(), mutation.queryDocument(), mutation.name(), JSONObject(mutation.variables().valueMap()), mutation.responseFieldMapper().javaClass.simpleName)
        val m2 = getDb()!!.mutationDao().getAMutation(0)
        assertTrue(!m1.equals(m2))
    }

    @Test
    fun deleteMutationCheck(){
        var a = getDb()?.mutationDao()?.getAllMutations()?.size
        assertEquals(1, a)
        getDb()?.mutationDao()?.deleteAllMutations()
        a = getDb()?.mutationDao()?.getAllMutations()?.size
        assertEquals(0, a)
    }

    @Test
    fun getAllTasksWorking(){
        val callback = object : ApolloCall.Callback<FindAllTasksQuery.Data>() {

            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
                Log.e("TAG", "getTasks ----$e ")
                assert(false)
            }

            override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                Log.e("TAG", "on Response getTasks : Data ${response.data()}")
                assert(true)
            }
        }
        Offline.apClient?.query(FindAllTasksQuery())?.watcher()
            ?.enqueueAndWatch(callback)
    }

    @Test
    fun getAllUsersWorking(){
        val callback = object : ApolloCall.Callback<FindAllUsersQuery.Data>() {

            override fun onFailure(e: ApolloException) {
                e.printStackTrace()
                Log.e("TAG", "getTasks ----$e ")
                assert(false)
            }

            override fun onResponse(response: Response<FindAllUsersQuery.Data>) {
                Log.e("TAG", "on Response getTasks : Data ${response.data()}")
                assert(true)
            }
        }
        Offline.apClient?.query(FindAllUsersQuery())?.watcher()
            ?.enqueueAndWatch(callback)
    }
}
