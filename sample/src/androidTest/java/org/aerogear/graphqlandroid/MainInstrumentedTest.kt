package org.aerogear.graphqlandroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import org.aerogear.graphqlandroid.model.UserOutput
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserInput

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.lang.AssertionError
import java.lang.Exception

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainInstrumentedTest {


    private var apolloQueryWatcher: ApolloQueryWatcher<FindAllUsersQuery.Data>? = null
    private var apolloCreateTaskMutation: ApolloQueryWatcher<CreateTaskMutation.Data>? = null
    private var apolloCreateUserMutation: ApolloQueryWatcher<CreateUserMutation.Data>? = null
    private var apolloUpdateTaskMutation: ApolloQueryWatcher<UpdateTaskMutation.Data>? = null
    private var apolloUpdateUserMutation: ApolloQueryWatcher<UpdateUserMutation.Data>? = null
    val appContext = InstrumentationRegistry.getTargetContext()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("org.aerogear.graphqlandroid", appContext.packageName)

    }

    @Test
    fun getAllTasksTest(){
        var i=0
        try {
            FindAllTasksQuery.builder()?.build()?.let {
                Utils.getApolloClient(appContext)?.query(it)
                    ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                    ?.enqueue(object : ApolloCall.Callback<FindAllTasksQuery.Data>() {

                        override fun onFailure(e: ApolloException) {
                            e.printStackTrace()
                            Log.e("TAG", "getTasks ----$e ")
                            i = 1
                            System.out.println("not working")
                        }

                        override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                        Log.e("TAG", "on Response getTasks : Data ${response.data()}")
                        i = 0
                    }
                })
        }
    }catch (e: Exception){
        e.printStackTrace()
    }finally {
        if(i==1){
            assertTrue(false)
        }else{
            assertTrue(true)
        }
    }
}

@Test
fun getAllUsersTest(){
    var i=0
    try {
        FindAllUsersQuery.builder()?.build()?.let {
            Utils.getApolloClient(appContext)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindAllUsersQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        e.printStackTrace()
                        Log.e("TAG", "getTasks ----$e ")
                        i = 1
                        System.out.println("not working")
                    }

                    override fun onResponse(response: Response<FindAllUsersQuery.Data>) {
                        Log.e("TAG", "on Response getTasks : Data ${response.data()}")
                        i = 0
                    }
                })
        }
    }catch (e: Exception){
        e.printStackTrace()
    }finally {
        if(i==1){
            assertTrue(false)
        }else{
            assertTrue(true)
        }
    }
    }

    @Test
    fun createTaskTest(){
        var i=9
        try {
            val input =
                TaskInput.builder().title("tas1").description("desc").version(1).status("test")
                    .build()

            val mutation = CreateTaskMutation.builder().input(input).build()

            Utils.getApolloClient(appContext)?.mutate(
                mutation
            )?.refetchQueries(apolloCreateTaskMutation?.operation()?.name())?.enqueue(
                object: ApolloCall.Callback<CreateTaskMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                        e.printStackTrace()
                        i = 1
                    }

                    override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                        val result = response.data()?.createTask()

                        //In case of conflicts data returned from the server id null.
                        result?.let {
                            Log.e("TAG", "onResponse-CreateTask- $it")
                            i = 0
                        }
                    }
                })
        }catch (s: Exception){
            s.printStackTrace()
        }finally {
            if(i==0){
                assertTrue(true)
            }else if(i==1){
                assertTrue(false)
            }
        }
    }

    @Test
    fun createUserTest(){
        var i=9
        try{
        val input =
            UserInput.builder().taskId("0").email("example@gmail.com").firstName("test").lastName("user")
                .title("tester")
                .creationmetadataId("0").build()

        val mutation = CreateUserMutation.builder().input(input).build()

        val mutationCall = Utils.getApolloClient(appContext)?.mutate(
            mutation
        )?.refetchQueries(apolloCreateUserMutation
            ?.operation()?.name())

        Log.e("TAG", " createUser 22: - ${mutationCall?.operation()?.variables()?.valueMap()}")
        val callback = object : ApolloCall.Callback<CreateUserMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
                i=0
            }

            override fun onResponse(response: Response<CreateUserMutation.Data>) {
                val result = response.data()?.createUser()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e("TAG", "onResponse-UpdateTask- $it")
                    i=1
                }
            }
        }
        mutationCall?.enqueue(callback)
        }catch (s: Exception){
            s.printStackTrace()
        }finally {
            if(i==0){
                assertTrue(false)
            }else if(i==1){
                assertTrue(true)
            }
        }
    }

    @Test
    fun updateTaskTest(){
        var i=9
        try{
        val input =
            TaskInput.builder().title("tas1").description("desc").version(1).status("test")
                .build()

        val mutation = UpdateTaskMutation.builder().id("0").input(input).build()

        Log.e("TAG", " updateTask ********: - $mutation")

        val mutationCall = Utils.getApolloClient(appContext)?.mutate(
            mutation
        )?.refetchQueries(apolloUpdateTaskMutation?.operation()?.name())

        val callback = object : ApolloCall.Callback<UpdateTaskMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Log.e("onFailure() updateTask", "${mutation.variables().valueMap()}")
                e.printStackTrace()
                i=0
            }

            override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                val result = response.data()?.updateTask()

                //In case of conflicts data returned from the server id null.
                result?.let {
                    Log.e("TAG", "onResponse-UpdateTask- $it")
                    i=1
                }
            }
        }
        mutationCall?.enqueue(callback)
        }catch (s: Exception){
            s.printStackTrace()
        }finally {
            if(i==0){
                assertTrue(false)
            }else if(1==1){
                assertTrue(true)
            }
        }
    }
}
