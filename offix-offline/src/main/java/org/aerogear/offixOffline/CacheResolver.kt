package org.aerogear.offixOffline

import android.util.Log
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver

class CacheResolver {

    private val TAG = javaClass.simpleName

    fun cacheResolver(): CacheKeyResolver {
        return object : CacheKeyResolver() {
            override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {
                Log.d(TAG, "fromFieldRecordSet ${(recordSet["id"] as String)}")
                if (recordSet.containsKey("id")) {
                    val typeNameAndIDKey = recordSet["__typename"].toString() + "." + recordSet["id"]
                    return CacheKey.from(typeNameAndIDKey)
                }
                return CacheKey.NO_KEY

            }

            /*
            Use this resolver to customize the key for fields with variables: eg entry(repoFullName: $repoFullName).
            This is useful if you want to make query to be able to resolved, even if it has never been run before.
            */
            override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                Log.d(TAG, "fromFieldArguments $variables")
                return CacheKey.NO_KEY
            }
        }
    }
}