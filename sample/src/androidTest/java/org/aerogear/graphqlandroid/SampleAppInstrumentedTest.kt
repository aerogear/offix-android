package org.aerogear.graphqlandroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import org.aerogear.graphqlandroid.type.TaskInput
import org.aerogear.graphqlandroid.type.UserInput

@RunWith(AndroidJUnit4::class)
class SampleAppInstrumentedTest {
	val targetContext = InstrumentationRegistry.getTargetContext()

	@Test
	fun getAppContext() {
		assertEquals("org.aerogear.graphqlandroid", this.targetContext.packageName)
	}

	@Test
	fun createTestTask() {
		val testData = TaskInput.builder().title("Test Task 9").version(1)
			.description("Test Task 9 Description").status("testtask").build()

		val testMutation = CreateTaskMutation.builder().input(testData).build()

		val apolloQuery: ApolloQueryWatcher<CreateTaskMutation.Data>? = null

		Utils.getApolloClient(this.targetContext)?.mutate(testMutation)
			?.refetchQueries(apolloQuery?.operation()?.name())
			?.enqueue(object : ApolloCall.Callback<CreateTaskMutation.Data>() {
				override fun onFailure(e: ApolloException) {
					assertTrue(false)
				}

				override fun onResponse(response: Response<CreateTaskMutation.Data>) {
					assertTrue(true)
				}
			})
	}

	@Test
	fun createTestUser() {
		val testData = UserInput.builder().title("Test 2 User").firstName("Test First Name")
			.lastName("Test Last Name").email("test2user@test.com").taskId("2")
			.creationmetadataId("2").build()

		val testMutation = CreateUserMutation.builder().input(testData).build()

		val apolloQuery: ApolloQueryWatcher<CreateTaskMutation.Data>? = null

		Utils.getApolloClient(this.targetContext)?.mutate(testMutation)
			?.refetchQueries(apolloQuery?.operation()?.name())
			?.enqueue(object : ApolloCall.Callback<CreateUserMutation.Data>() {
				override fun onFailure(e: ApolloException) {
					assertTrue(false)
				}

				override fun onResponse(response: Response<CreateUserMutation.Data>) {
					assertTrue(true)
				}
			})
	}

	@Test
	fun updateTestTask() {
		val testData = TaskInput.builder().title("change test task 9").version(1).description("new test description").status("testtask").build()

		val testMutation = UpdateTaskMutation.builder().input(testData).id("2").build()

		val apolloQuery: ApolloQueryWatcher<UpdateTaskMutation.Data>? = null

		Utils.getApolloClient(this.targetContext)?.mutate(testMutation)
			?.refetchQueries(apolloQuery?.operation()?.name())
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
	fun searchAndUpdateTestTask() {

		val testMutation = CheckAndUpdateTaskMutation.builder().title("changed test task").version(1).description("new test description").id("2").status("testtask").build()

		val apolloQuery: ApolloQueryWatcher<CheckAndUpdateTaskMutation.Data>? = null

		Utils.getApolloClient(this.targetContext)?.mutate(testMutation)
			?.refetchQueries(apolloQuery?.operation()?.name())
			?.enqueue(object : ApolloCall.Callback<CheckAndUpdateTaskMutation.Data>() {
				override fun onFailure(e: ApolloException) {
					assertTrue(false)
				}

				override fun onResponse(response: Response<CheckAndUpdateTaskMutation.Data>) {
					assertTrue(true)
				}
			})
	}

	@Test
	fun searchAllTestTasks() {
		FindAllTasksQuery.builder()?.build()?.let {
			Utils.getApolloClient(this.targetContext)?.query(it)
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
	fun searchAllTestUsers() {
		FindAllUsersQuery.builder()?.build()?.let {
			Utils.getApolloClient(this.targetContext)?.query(it)
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
}