package org.aerogear.graphqlandroid

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.aerogear.graphqlandroid.activities.MainActivity
import java.nio.charset.Charset
import java.util.concurrent.Executor

object Utils {

    //To run on emulator
    const val BASE_URL = "http://192.168.0.102:4000/graphql"
    private const val SQL_CACHE_NAME = "tasks3Db"

    private var apClient: ApolloClient? = null
    private var httpClient: OkHttpClient? = null
    private var conflictInterceptor: Interceptor? = null
    private lateinit var apolloInterceptor: ApolloInterceptor
    private val gson = Gson()
    lateinit var parsedObject: ConflictPojo

    @JvmStatic
    fun getApolloClient(context: Context): ApolloClient? {

        val apolloSqlHelper = ApolloSqlHelper(context, SQL_CACHE_NAME)

        //Used Normalized Disk Cache: Per node caching of responses in SQL.
        //Persists normalized responses on disk so that they can used after process death.
        val cacheFactory = LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
            .chain(SqlNormalizedCacheFactory(apolloSqlHelper))

        //If Apollo Client is not null, return it else make a new Apollo Client.
        //Helps in singleton pattern.
        apClient?.let {
            return it
        } ?: kotlin.run {
            apClient = ApolloClient.builder()
                .okHttpClient(getOkhttpClient(context)!!)
                .normalizedCache(cacheFactory, cacheResolver())
//              .addApplicationInterceptor(getApolloInterceptor())
                .subscriptionTransportFactory(
                    WebSocketSubscriptionTransport.Factory(
                        BASE_URL,
                        getOkhttpClient(context)!!
                    )
                )
                .serverUrl(BASE_URL)
                .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
                .build()
        }

        return apClient
    }

    private fun getOkhttpClient(context: Context): OkHttpClient? {

        //Adding HttpLoggingInterceptor() to see the response body and the results.
        httpClient?.let {
            return it
        } ?: kotlin.run {
            httpClient = OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor())
                .addInterceptor(getResponseInterceptor(context)!!)
                .build()
        }
        return httpClient
    }

    private fun getApolloInterceptor(): ApolloInterceptor {
        val apolloInterceptor = object : ApolloInterceptor {
            override fun interceptAsync(
                request: ApolloInterceptor.InterceptorRequest,
                chain: ApolloInterceptorChain,
                dispatcher: Executor,
                callBack: ApolloInterceptor.CallBack
            ) {
                Log.e("ApolloInterceptor----", "$request")
                Log.e("ApolloInterceptor----", "${request.operation.queryDocument()}")
                Log.e("ApolloInterceptor----", "${request.operation.variables().valueMap()}")
                Log.e("ApolloInterceptor----", "${request.operation.operationId()}")
                Log.e("ApolloInterceptor----", "${request.requestHeaders}")
//                Log.e("ApolloInterceptor----", "${chain.}")
            }

            override fun dispose() {
                Log.e("UtilsClass", " *** ondispose")
            }
        }

        return apolloInterceptor
    }

    //function to get the response after query/mutation is performed, can get conflict messages and so on.
    private fun getResponseInterceptor(context: Context): Interceptor? {

        conflictInterceptor?.let {
            return it
        } ?: kotlin.run {
            conflictInterceptor = Interceptor {
                val request = it.request()

                //all the HTTP work happens, producing a response to satisfy the request.
                val response = it.proceed(request)

                //https://stackoverflow.com/a/33862068/10189663

                val responseBody = response.body()
                val bufferedSource = responseBody?.source()
                bufferedSource?.request(Long.MAX_VALUE)
                val buffer = bufferedSource?.buffer()
                val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""

                Log.e("Util Class in sample", " Interceptor : $responseBodyString")

                //To see for conflict, "VoyagerConflict" which comes in the message is searched for.
                if (responseBodyString.contains("VoyagerConflict")) {
                    Log.e("Util Class in sample : ", "VoyagerConflict")

                    /*
                    Map the json responseBody of conflict to the Conflict Pojo.
                     */
                    makePojoOfResponse(responseBodyString)
                    //TODO problem in getting activtiy Context to show Conflict Detected message to the user.
//                    showToast(context)
                }
//                if (responseBodyString.contains("\"msg\":\"\"") &&
//                    responseBodyString.contains("\"operationType\":\"mutation\"") &&
//                    responseBodyString.contains("\"success\":true")
//                ) {
//                    Log.e("UtilsClass", "mutation operation successfully performed")
//                    (context as MainActivity).onSuccess()
//                }

//                if (responseBodyString.contains("\"msg\":\"\",\"operationType\":\"query\"") &&
//                    responseBodyString.contains("\"success\":true")
//                ) {
//                    Log.e("UtilsClass", "query operation successfully performed")
//                    showToast2(context)
//                }

                return@Interceptor response
            }

        }
        return conflictInterceptor
    }

    /*
     This function parses the response body of the conflict detected and makes an object of ConflictPojo class.
     */
    fun makePojoOfResponse(responseBodyString: String) {
        parsedObject = gson.fromJson(responseBodyString, ConflictPojo::class.java)
        Log.e("Util makePojoOfResponse", " ${parsedObject.errors[0].extensions.exception.conflictInfo.serverState}")
    }

    //Toast shown to the user displaying conflict detected.
    private fun showToast(context: Context) {
        (context as MainActivity).runOnUiThread {
            Toast.makeText(context, "Conflict Detected", Toast.LENGTH_SHORT).show()
        }
    }

    //Toast shown to the user displaying conflict detected.
    private fun showToast2(context: Context) {
        (context as AppCompatActivity).runOnUiThread {
            Toast.makeText(context, "Query run successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cacheResolver(): CacheKeyResolver {
        return object : CacheKeyResolver() {
            override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {
                Log.e("UtilClass", "fromFieldRecordSet ${(recordSet["id"] as String)}")
                if (recordSet.containsKey("id")) {
                    val typeNameAndIDKey = recordSet["__typename"].toString() + "." + recordSet["id"]
                    return CacheKey.from(typeNameAndIDKey)
                }
                return CacheKey.NO_KEY
            }

            // Use this resolver to customize the key for fields with variables: eg entry(repoFullName: $repoFullName).
            // This is useful if you want to make query to be able to resolved, even if it has never been run before.
            override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                Log.e("UtilClass", "fromFieldArguments $variables")
                return CacheKey.NO_KEY
            }
        }


    }
}

