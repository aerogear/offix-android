package org.aerogear.offixsdk

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.aerogear.offixsdk.Interceptor.LoggingInterceptor
import java.nio.charset.Charset

/*  ClientGenerator class will help the user in creating an apollo client.
    The user passes in the server url to which the request has to be sent.
*/

class ClientGenerator(val serverUrl: String) {

    private val TAG = javaClass.simpleName
    private val applicationContext: Context = getContext()
    val SQL_CACHE_NAME = "oclientDB"
    private var apClient: ApolloClient? = null
    private var httpClient: OkHttpClient? = null
    private var conflictInterceptor: Interceptor? = null

    fun getApolloClient(): ApolloClient? {

        val apolloSqlHelper = ApolloSqlHelper(applicationContext, SQL_CACHE_NAME)

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
                .okHttpClient(getOkhttpClient()!!)
                .normalizedCache(cacheFactory, cacheResolver())
                .subscriptionTransportFactory(
                    WebSocketSubscriptionTransport.Factory(
                        serverUrl,
                        getOkhttpClient()!!
                    )
                )
                .serverUrl(serverUrl)
//              .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
                .build()
        }

        return apClient
    }

    private fun getOkhttpClient(): OkHttpClient? {

        //Adding HttpLoggingInterceptor() to see the response body and the results.
        httpClient?.let {
            return it
        } ?: kotlin.run {
            httpClient = OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor())
                .addInterceptor(getResponseInterceptor(applicationContext)!!)
                .build()
        }
        return httpClient
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

                Log.e("UtillClass", " Interceptor : $responseBodyString")

                //To see for conflict, "VoyagerConflict" which comes in the message is searched for.
                if (responseBodyString.contains("VoyagerConflict")) {
                    Log.e(TAG, " Conflcit in offixClient")
                }
                return@Interceptor response
            }
        }
        return conflictInterceptor
    }

    private fun cacheResolver(): CacheKeyResolver {
        return object : CacheKeyResolver() {
            override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {
                Log.e(TAG, "fromFieldRecordSet ${(recordSet["id"] as String)}")
//                if (recordSet.containsKey("id")) {
//                    val typeNameAndIDKey = recordSet["__typename"].toString() + "." + recordSet["id"]
//                    return CacheKey.from(typeNameAndIDKey)
//                }
//                return CacheKey.NO_KEY
                return formatCacheKey(recordSet["id"] as String?)
            }

            // Use this resolver to customize the key for fields with variables: eg entry(repoFullName: $repoFullName).
            // This is useful if you want to make query to be able to resolved, even if it has never been run before.
            override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                Log.e(TAG, "fromFieldArguments $variables")
//                return CacheKey.NO_KEY
                return formatCacheKey(field.resolveArgument("id", variables) as String?)
            }

            private fun formatCacheKey(id: String?): CacheKey {
                return if (id == null || id.isEmpty()) {
                    CacheKey.NO_KEY
                } else {
                    CacheKey.from(id)
                }
            }
        }
    }
}