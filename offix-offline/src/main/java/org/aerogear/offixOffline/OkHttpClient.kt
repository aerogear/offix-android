package org.aerogear.offixOffline

import android.content.Context
import okhttp3.OkHttpClient
import org.aerogear.offixOffline.Interceptor.LoggingInterceptor

class OkHttpClient(context: Context) {

    private val ctx = context
    private var httpClient: OkHttpClient? = null

    fun getOkhttpClient(): OkHttpClient? {

        //Adding HttpLoggingInterceptor() to see the response body and the results.
        httpClient?.let {
            return it
        } ?: kotlin.run {
            httpClient = OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor())
                .addInterceptor(ctx.let { ResponseInterceptor(it).getResponseInterceptor() }!!)
                .build()
        }
        return httpClient
    }
}