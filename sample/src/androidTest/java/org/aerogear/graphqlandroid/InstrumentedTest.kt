package org.aerogear.graphqlandroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserInput
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    val appContext = InstrumentationRegistry.getTargetContext()

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("org.aerogear.graphqlandroid", appContext.packageName)
    }

    @Test
    fun findAllTaskQuery() {
        FindAllTasksQuery.builder()?.build()?.let {
            Utils.getApolloClient(appContext)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindAllTasksQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        assertTrue(false)
                    }

                    override fun onResponse(response: Response<FindAllTasksQuery.Data>) {
                        assertTrue(true)
                    }
                })
        }
    }

    @Test
    fun findAllUsersQuery(){
        FindAllUsersQuery.builder()?.build()?.let {
            Utils.getApolloClient(appContext)?.query(it)
                ?.responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
                ?.enqueue(object : ApolloCall.Callback<FindAllUsersQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        assertTrue(false)
                    }

                    override fun onResponse(response: Response<FindAllUsersQuery.Data>) {
                        assertTrue(true)
                    }

                })
        }
    }

    @Test
    fun createUser(){
        val input = UserInput.builder()
                .title("TestUser")
                .lastName("L-N")
                .firstName("F-N")
                .email("tester015875@protonmail.com")
                .taskId("0")
                .creationmetadataId("0")
                .build()

        val mutation = CreateUserMutation.builder().input(input).build()

        val apolloMutation : ApolloQueryWatcher<CreateUserMutation.Data>? = null

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloMutation?.operation()?.name())
            ?.enqueue(object : ApolloCall.Callback<CreateUserMutation.Data>() {

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CreateUserMutation.Data>) {
                    response.data()?.createUser()?.let {
                        assertTrue(true)
                    }
                }
            })
    }

    @Test
    fun createTask(){
        val input = TaskInput.builder()
            .title("test")
            .description("This is a test task")
            .version(1)
            .status("testing")
            .build()

        val mutation = CreateTaskMutation.builder().input(input).build()

        val apolloMutation : ApolloQueryWatcher<CreateTaskMutation.Data>? = null

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloMutation?.operation()?.name())
            ?.enqueue(object: ApolloCall.Callback<CreateTaskMutation.Data>() {

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CreateTaskMutation.Data>) {
                    assertTrue(true)
                }

            })

    }

    @Test
    fun updateTask(){

        val input = TaskInput.builder()
            .title("test")
            .description("This is a test task")
            .version(1)
            .status("testing")
            .build()

        val mutation = UpdateTaskMutation.builder().id("0").input(input).build()

        val apolloMutation : ApolloQueryWatcher<UpdateTaskMutation.Data>? = null

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloMutation?.operation()?.name())
            ?.enqueue(object : ApolloCall.Callback<UpdateTaskMutation.Data>() {

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<UpdateTaskMutation.Data>) {
                    assertTrue(true)
                }

            })

    }

    @Test
    fun checkAndUpdateTask(){

        val mutation = CheckAndUpdateTaskMutation.builder()
            .id("0")
            .title("test")
            .description("This is a test task")
            .version(1)
            .status("testing")
            .build()

        val apolloMutation : ApolloQueryWatcher<CheckAndUpdateTaskMutation.Data>? = null

        Utils.getApolloClient(appContext)
            ?.mutate(mutation)
            ?.refetchQueries(apolloMutation?.operation()?.name())
            ?.enqueue(object : ApolloCall.Callback<CheckAndUpdateTaskMutation.Data>() {

                override fun onFailure(e: ApolloException) {
                    assertTrue(false)
                }

                override fun onResponse(response: Response<CheckAndUpdateTaskMutation.Data>) {
                    assertTrue(true)
                }

            })

    }

}