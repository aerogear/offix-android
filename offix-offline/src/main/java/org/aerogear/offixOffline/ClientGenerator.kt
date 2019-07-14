package org.aerogear.offixOffline

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport

/*  ClientGenerator class will help in creating an apollo client when the app is in background
    and a refernce to the apolloclient mad eby the user can't be used.
*/
class ClientGenerator(val serverUrl: String) {

    private val TAG = javaClass.simpleName
    private val ctx: Context? = Offline.getContextOffline()
    val SQL_CACHE_NAME = "oclientDB"
    private var apClient: ApolloClient? = null

    fun getApolloClient(): ApolloClient? {

        val apolloSqlHelper = ApolloSqlHelper(ctx, SQL_CACHE_NAME)

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
                .okHttpClient(ctx?.let { org.aerogear.offixOffline.OkHttpClient(it).getOkhttpClient() }!!)
                .normalizedCache(cacheFactory, CacheResolver().cacheResolver())
                .subscriptionTransportFactory(
                    WebSocketSubscriptionTransport.Factory(
                        serverUrl,
                        ctx.let { org.aerogear.offixOffline.OkHttpClient(it).getOkhttpClient() }!!
                    )
                )
                .serverUrl(serverUrl)
                .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
                .build()
        }
        return apClient
    }
}