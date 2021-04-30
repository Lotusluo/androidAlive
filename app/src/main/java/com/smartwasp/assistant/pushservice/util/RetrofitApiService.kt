package com.smartwasp.assistant.pushservice.util

import com.smartwasp.assistant.pushservice.bean.BaseBean
import com.smartwasp.assistant.pushservice.bean.ConfigBean
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


/**
 * Created by luotao on 2021/1/8 15:23
 * E-Mail Address：gtkrockets@163.com
 */
interface RetrofitApiService {
    /**
     * 获取配置信息
     */
    @FormUrlEncoded
    @POST("api/client/device/getUpdateConfig")
    fun loadConfig(@Field("projectId") projectId:String = "69"): Call<BaseBean<ConfigBean>>

    /**
     * 获取配置信息
     */
    @FormUrlEncoded
    @POST("api/client/device/reportrDeviceInfo")
    fun report(@Field("projectId") projectId:String = "69",@Field("sn") sn:String = DeviceUtils.getDeviceId()): Call<BaseBean<String>>

    /**
     * 断点下载
     * RANGE bytes=0-1000
     * @Header("Range") range:String? = null,
     */
    @Streaming
    @GET
    fun download(@Url path:String):Call<ResponseBody>
}