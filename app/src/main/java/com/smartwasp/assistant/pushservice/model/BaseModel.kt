package com.smartwasp.assistant.pushservice.model

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.smartwasp.assistant.pushservice.util.RetrofitCoroutineDSL
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.RuntimeException
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

/**
 * Created by luotao on 2021/4/13
 * E-Mail Address：gtkrockets@163.com
 */
abstract class BaseModel: LifecycleObserver,CoroutineScope {
    companion object{
        fun getLogTag():String{
            return this.javaClass.simpleName
        }
    }

    //协程的工作
    private var allJob: Job = Job()

    var job:Job? = null

    //协程上下文环境
    override val coroutineContext: CoroutineContext
        get() =  Dispatchers.Main + allJob

    fun clearJob(){
        if(job?.isCancelled == false){
            job!!.cancel()
        }
        job = null
    }

    protected fun <T> retrofit(dsl: RetrofitCoroutineDSL<T>.() -> Unit) {
        val coroutine = RetrofitCoroutineDSL<T>().apply(dsl)
        clearJob()
        job = launch(Dispatchers.IO) {
            coroutine.api?.let { call ->
                //async 并发执行 在IO线程中
                val deferred = async {
                    try {
                        var result = call.execute() //已经在io线程中了，所以调用Retrofit的同步方法.
                        result
                    } catch (e: ConnectException) {
                        Log.e(getLogTag(),e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    } catch (e: IOException) {
                        Log.e(getLogTag(),e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    } catch (e: RuntimeException){
                        Log.e(getLogTag(),e.toString())
                        coroutine.onFail?.invoke(-1,e.toString())
                        null
                    }
                }
                //当协程取消的时候，取消网络请求
                deferred.invokeOnCompletion {
                    if (deferred.isCancelled) {
                        call.cancel()
                        coroutine.clean()
                        Log.e(getLogTag(),"clear request")
                    }
                }
                //await 等待异步执行的结果
                val response = deferred.await()
                response?.let {
                    if(it.isSuccessful){
                        it.body()?.let { body ->
                            coroutine.onSuccess?.invoke(body)
                        }?: kotlin.run {
                            Log.e(getLogTag(),it.message())
                            coroutine.onFail?.invoke(it.code(),it.message())
                        }
                    }else{
                        Log.e(getLogTag(),it.errorBody()?.string()!!)
                        coroutine.onFail?.invoke(-1,"error_empty_data")
                    }
                }?: kotlin.run {
                    coroutine.onFail?.invoke(-2,"error_empty_data")
                }
            }?: kotlin.run {
                coroutine.onFail?.invoke(-3,"error_empty_request")
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        allJob?.cancel()
    }
}
