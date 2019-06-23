package org.aerogear.graphqlandroid

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Workmanager that replicates the mutations to the server that are stored in the database.
 * Post firing the mutation, it is deleted from the db
 */

class OfflineMutationsWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {


    private val getDao = MyApplciation.database.mutationDao()
    val TAG = javaClass.simpleName

    var apolloQueryWatcher: ApolloQueryWatcher<AllTasksQuery.Data>? = null


    override fun doWork(): Result {
        Log.e("TAG", "In Worker doWork()")

        //get the list of mutations
        val list = getDao.getAllMutations()

        //get the mutation one by one from database
        for (currmutation in list) {
            val opId = currmutation.operationID
            val opNmae = currmutation.operationName
            val queryDoc = currmutation.queryDoc
            val json = currmutation.valuemap


            //val result: HashMap<String, Object> = Gson().fromJson(json, TypeToken<HashMap<String, Object>> {}.type)

            val valmap = toMap(json)

            //Make an object of GenericMutation class
            val genericMutation = GenericMutation(opId, opNmae, queryDoc, valmap as MutableMap<String, Any>)

            // make a client call

           val client = Utils.getApolloClient(applicationContext)
                ?.mutate(genericMutation)?.refetchQueries(apolloQueryWatcher?.operation()?.name())


            client?.enqueue(object : ApolloCall.Callback<GenericMutation.Data>() {
                override fun onFailure(e: ApolloException) {

                    Log.e("onFailure" + "Worker Class: ", e.toString())
                     Result.retry()

                }

                override fun onResponse(response: Response<GenericMutation.Data>) {

                    val result = response.data()
                    Log.e(TAG, "Worker Class: ${response.data()}")
                    Log.e(TAG, "onResponse-UpdateTask")

                    Log.e(TAG, "${result?.Mapper()}")
                    Log.e(TAG, "${result?.mapBuilder()}")
                }
            })

        }


        return Result.success()
    }


    @Throws(JSONException::class)
    fun toMap(jsonobj: JSONObject): Map<String, Any> {
        val map = HashMap<String, Any>()
        val keys = jsonobj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value = jsonobj.get(key)
            if (value is JSONArray) {
                value = toList(value as JsonArray)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            map[key] = value
        }
        return map
    }


    @Throws(JSONException::class)
    fun toList(array: JsonArray): List<Object> {
        val list = arrayListOf<Object>()

        for (i in array) {
            var value = i
            if (value is JsonArray) {
                value = toList(value) as JsonElement
            } else if (value is JSONObject) {
                value = toMap(value) as JsonElement
            }

            list.add(value as Object)

        }
        return list
    }
}
