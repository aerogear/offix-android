package org.aerogear.offixsdk.Interceptor

import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.aerogear.offixsdk.application
import org.aerogear.offixsdk.persistence.Url
import org.aerogear.offixsdk.worker.OfflineMutationsWorker
import java.io.IOException
import java.util.concurrent.TimeUnit


/* With the help of this interceptor, we can get access to the requests and responses.
 */
class LoggingInterceptor : Interceptor {

    val TAG = javaClass.simpleName

    lateinit var headers: Headers

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        headers = request.headers()
        val stringUrl = request.url()

        /* When the client is offline, we save the request url in a database which we can access
           later while making a custom call to the server when the network connection is regained.
         */
        val urlobject = Url(stringUrl.toString())
        application.database.urlDao().insertUrl(urlobject)

        /* Schedule a work manager to process that request in future.
           1. Set the constraints.
           2. Execute a periodic work Request
        */
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorker = PeriodicWorkRequestBuilder<OfflineMutationsWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueue(periodicWorker)


        /*
         For logging the request sent to the server.
         */
        val t1 = System.nanoTime()
        Log.e(
            TAG + "1",
            " ${request.url()}  ---   ${bodyToString(request)}   ---  ${request.headers()}"
        )

        /*
         For logging the response received from the server.
         */
        val response = chain.proceed(request)
        val t2 = System.nanoTime()

        Log.e(TAG + "2 ", " ${response.request().url()}  ${t2 - t1}  ${request.headers()}")
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