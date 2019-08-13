package org.aerogear.graphqlandroid

import android.content.Context
import android.util.Log
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
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.aerogear.offix.interceptor.ConflictInterceptor
import java.nio.charset.Charset

object Utils {

    //To run on emulator use http://10.0.2.2.100:4000/graphql
    const val BASE_URL = "http://192.168.0.105:4000/graphql"
    private const val SQL_CACHE_NAME = "tasks4Db"
    private var apClient: ApolloClient? = null
    private var httpClient: OkHttpClient? = null

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
                /*While making an instance of apollo client, user will have to add the interceptor provided
                  by the library.
                */
                .addApplicationInterceptor(ConflictInterceptor(UserConflictResolutionHandler(context)))
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
        httpClient?.let {
            return it
        } ?: kotlin.run {
            httpClient = OkHttpClient.Builder()
                //Adding HttpLoggingInterceptor() to see the response body and the results.
                .addInterceptor(LoggingInterceptor())
                .build()
        }
        return httpClient
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

