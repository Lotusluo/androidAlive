package com.smartwasp.assistant.pushservice.util

import android.content.pm.PackageManager
import android.util.Log
import com.smartwasp.assistant.pushservice.App
import com.smartwasp.assistant.pushservice.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.GzipSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by luotao on 2021/1/8 15:14
 * E-Mail Address：gtkrockets@163.com
 */
class RetrofitManager private constructor():Interceptor{
    companion object{
        private var instance:RetrofitManager? = null
        fun get(): RetrofitManager{
            instance?.let {
                return it
            }?: kotlin.run {
                synchronized(this){
                    return RetrofitManager()
                }
            }
        }
    }

    //HTTP服务
    var retrofitApiService:RetrofitApiService? = null
        private set

    private var accessToken:String

    init {
        initHttpRequest()
        accessToken = App._thiz.packageManager.
        getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA).metaData["access_token"].toString()
    }

    //初始化Http
    private fun initHttpRequest() {
        var retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(NetWorkUtil.getOkHttpsSSLOkHttpClientForRetrofit(this))
                .build()
        retrofitApiService = retrofit.create(RetrofitApiService::class.java)
    }


    /**
     * http请求拦截
     * @param chain
     * @return 响应
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .addHeader("access_token",accessToken)
                .addHeader("Cache-Control","no-cache, max-age=0")
                .build()
        try{
            return chain.proceed(request)
        }catch (e:IOException){
            throw e
        }
    }
}