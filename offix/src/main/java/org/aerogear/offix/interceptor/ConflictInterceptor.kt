package org.aerogear.offix.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.aerogear.offix.detectedConflict
import java.nio.charset.Charset

/*
 ConflictInterceptor should be used by the user while making an instance of apollo client
 which will help in detecting conflicts.
 */
class ConflictInterceptor  : Interceptor{

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        //https://stackoverflow.com/a/33862068/10189663
        val responseBody = response.body()
        val bufferedSource = responseBody?.source()
        bufferedSource?.request(Long.MAX_VALUE)
        val buffer = bufferedSource?.buffer()
        val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""

        Log.d("OffixClass", " Interceptor ** : $responseBodyString")

        if (responseBodyString.contains("conflictInfo")) {
            detectedConflict = responseBodyString
            Log.d("ConflictInterceptor", " ******")
            Log.d("ConflictInterceptor **", detectedConflict)
        }
        return response
    }
}
