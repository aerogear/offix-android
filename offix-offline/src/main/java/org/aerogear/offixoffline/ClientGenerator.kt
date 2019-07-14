package org.aerogear.offixoffline

import android.annotation.SuppressLint
import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport

/*  ClientGenerator helps in creating an apollo client when the app is in background
    and a reference to the apollo client made by the user cannot be used.
*/
class ClientGenerator{

    /*
    The library follows a singleton pattern while making apollo client.
     */
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val context: Context? = Offline.getContextOffline()
        val SQL_CACHE_NAME = "oclientDB"
        private var apClient: ApolloClient? = null

        private val sharedPreferences = context?.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)
        private val serverUrl = sharedPreferences?.getString("url", " ")

        /*
         To get the apolloClient whenever a call is to be made to the server when client is in background.
          */
        fun getApolloClient(): ApolloClient? {
            val apolloSqlHelper = ApolloSqlHelper(context, SQL_CACHE_NAME)

            /*Used Normalized Disk Cache: Per node caching of responses in SQL.
              Persists normalized responses on disk so that they can used after process death.
            */
            val cacheFactory = LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
                .chain(SqlNormalizedCacheFactory(apolloSqlHelper))

            /*If Apollo Client is not null, return it else make a new Apollo Client.
              Helps in singleton pattern.
            */
            apClient?.let {
                return it
            } ?: kotlin.run {
                apClient = ApolloClient.builder()
                    .okHttpClient(context?.let { org.aerogear.offixoffline.OkHttpClient(it).getOkhttpClient() }!!)
                    .normalizedCache(cacheFactory, CacheResolver().cacheResolver())
                    .subscriptionTransportFactory(
                        WebSocketSubscriptionTransport.Factory(
                            serverUrl!!,
                            context.let { org.aerogear.offixoffline.OkHttpClient(it).getOkhttpClient() }!!
                        )
                    )
                    .serverUrl(serverUrl)
                    .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
                    .build()
            }
            return apClient
        }
    }
}