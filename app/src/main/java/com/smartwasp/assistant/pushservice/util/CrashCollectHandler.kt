package com.smartwasp.assistant.pushservice.util

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import kotlin.system.exitProcess

/**
 * 闪退
 */
class CrashCollectHandler : Thread.UncaughtExceptionHandler {
    var mContext: Context? = null
    var mDefaultHandler:Thread.UncaughtExceptionHandler ?=null
    
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { CrashCollectHandler() }
        var isCrashOccur = false
    }
    
    fun init(pContext: Context) {
        this.mContext = pContext
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
    
    //当UncaughtException发生时会转入该函数来处理
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if (!handleException(e) && mDefaultHandler!=null){
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(t,e)
        }else{
            isCrashOccur = true
            exitProcess(1)
            SystemClock.sleep(3000)
        }
    }

    private fun handleException(ex: Throwable?):Boolean {
        ex ?: return false
        Thread{
            Looper.prepare()
            //启动服务
            mContext?.startService(Intent("com.smartwasp.assistant.action.CALL").setPackage(mContext?.packageName))
            Looper.loop()
        }.start()
        return true
    }
}