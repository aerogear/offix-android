package org.aerogear.graphqlandroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

// Imports
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserInput
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    val appContext = InstrumentationRegistry.getTargetContext()
    private var apolloQueryWatcher: ApolloQueryWatcher<FindAllTasksQuery.Data>? = null

    @Test
    fun useAppContext() {
        // Context of the app under tests
        assertEquals("org.aerogear.graphqlandroid", appContext.packageName)
    }

    // Test - Find All Tasks
    @Test
    fun findAllTasks(){

        FindAllTasksQuery.builder()?.build()?.let{
            Utils.getApolloClient(this.appContext)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindAllTasksQuery.Data>(){

                    override fun onFailure(e: ApolloException) {
                        assertTrue(false)
                    }

                    override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                        assertTrue(true)
                    }
                })
        }
    }

    // Test - Find All Users
    @Test
    fun findAllUsers(){

        FindAllUsersQuery.builder()?.build()?.let{
            Utils.getApolloClient(appContext)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object: ApolloCall.Callback<FindAllUsersQuery.Data>(){

                    override fun onFailure(e: ApolloException) {
                        assertTrue(false)
                    }

                    override fun onResponse(response: Response<FindAllUsersQuery.Data>) {
                        assertTrue(true)
                    }
                })
        }
    }

    // Test - Create A Task
    @Test
    fun createTask(){

        val input = TaskInput.builder().title("Test Task 2").version(1).description("Test Description")
            .status("test")
            .build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloQueryWatcher?.operation()?.name())
            ?.enqueue(object: ApolloCall.Callback<CreateTaskMutation.Data>(){

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                    assertTrue(true)
                }
            })
    }

    // Test - Update A Task
    @Test
    fun updateTask(){

        val input = TaskInput.builder().title("Test Task Changed").version(1).description("Test Desc")
            .status("test")
            .build()

        val mutation = UpdateTaskMutation.builder().id("6").input(input).build()

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloQueryWatcher?.operation()?.name())
            ?.enqueue(object: ApolloCall.Callback<UpdateTaskMutation.Data>(){

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                    assertTrue(true)
                }
            })
    }

    // Test - Create A User
    @Test
    fun createUser(){
        val input = UserInput.builder().taskId("6").email("sample@example.com").firstName("ezio")
            .lastName("auditore")
            .title("User Test")
            .creationmetadataId("6").build()

        val mutation = CreateUserMutation.builder().input(input).build()

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloQueryWatcher?.operation()?.name())
            ?.enqueue(object: ApolloCall.Callback<CreateUserMutation.Data>(){

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CreateUserMutation.Data>) {
                    assertTrue(true)
                }
            })
    }

    // Test - Check And Update A Task
    @Test
    fun checkAndUpdateTask(){

        val mutation = CheckAndUpdateTaskMutation.builder().version(1).id("6").title("Test Title returns")
            .description("Test Description Returns")
            .status("Still Testing")
            .build()

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloQueryWatcher?.operation()?.name())
            ?.enqueue(object: ApolloCall.Callback<CheckAndUpdateTaskMutation.Data>(){

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CheckAndUpdateTaskMutation.Data>) {
                    assertTrue(true)
                }
            })
    }

}
