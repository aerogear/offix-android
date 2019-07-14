package org.aerogear.offixoffline

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import java.nio.charset.Charset

/*
Class to get the response after query/mutation is performed, can get conflict messages and so on.
 */
class ResponseInterceptor(context: Context) {

    private val TAG = javaClass.simpleName
    private val ctx = context
    private var conflictInterceptor: Interceptor? = null

    fun getResponseInterceptor(): Interceptor? {

        conflictInterceptor?.let {
            return it
        } ?: kotlin.run {
            conflictInterceptor = Interceptor {
                val request = it.request()

                //all the HTTP work happens, producing a response to satisfy the request.
                val response = it.proceed(request)

                val responseBody = response.body()
                val bufferedSource = responseBody?.source()
                bufferedSource?.request(Long.MAX_VALUE)
                val buffer = bufferedSource?.buffer()
                val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""

                Log.d(TAG, " ResponseInterceptor: $responseBodyString")

                //To see for conflict, "VoyagerConflict" which comes in the message is searched for.
                if (responseBodyString.contains("VoyagerConflict")) {
                    Log.d(TAG, " Conflcit in offixClient")
                }
                return@Interceptor response
            }
        }
        return conflictInterceptor
    }
}