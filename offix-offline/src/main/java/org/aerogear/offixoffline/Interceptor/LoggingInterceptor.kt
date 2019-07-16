package org.aerogear.offixoffline.Interceptor

import android.content.Context
import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.aerogear.offixoffline.Offline
import java.io.IOException

/* With the help of this interceptor, we can get access to the requests and responses.
 */
class LoggingInterceptor : Interceptor {

    val TAG = javaClass.simpleName
    val ctx = Offline.getContextOffline()

    lateinit var headers: Headers
    val sharedPreferences = ctx?.getSharedPreferences("libSharedPref", Context.MODE_PRIVATE)
    val isUrlPresent = sharedPreferences?.getString("url", " ")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        headers = request.headers()
        val stringUrl = request.url().toString()

        /* When the client is offline, we save the request url in a shared pref which we can access
           later while making a custom call to the server when the network connection is regained and the app is in background.
         */

        if (isUrlPresent.equals(stringUrl)) {
            //nothing
        } else {
            Offline.editSharedPref(stringUrl)
        }

        /*
         For logging the request sent to the server.
         */
        val t1 = System.nanoTime()
        Log.d("$TAG 1", " ${request.url()}  ---   ${bodyToString(request)}   ---  ${request.headers()}")

        /*
         For logging the response received from the server.
         */
        val response = chain.proceed(request)
        val t2 = System.nanoTime()

        Log.d("$TAG 2", " ${response.request().url()}  $t2  ${request.headers()}")
        return response
    }

    private fun bodyToString(request: Request): String {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body()?.writeTo(buffer)
            return buffer.readUtf8()

        } catch (e: IOException) {
            return "bodyToString did not work"
        }
    }
}