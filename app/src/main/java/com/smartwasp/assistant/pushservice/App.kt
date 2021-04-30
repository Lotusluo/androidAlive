package com.smartwasp.assistant.pushservice

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import cn.jpush.android.api.JPushInterface
import com.smartwasp.assistant.pushservice.util.CrashCollectHandler
import com.smartwasp.assistant.pushservice.util.DeviceAdminUtil


// Created by luotao on 2021/4/13
class App:Application() {
    companion object{
        lateinit var _thiz:Application
    }

    override fun onCreate() {
        super.onCreate()
        _thiz = this
        DeviceAdminUtil.getInstance().init(this)
        CrashCollectHandler.instance.init(this)
        //极光推送
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        //初始化 JPush
        JPushInterface.init(this)
//        adb shell am startservice -n com.smartwasp.assistant.pushservice/.service.SwPushService
//        adb shell am startservice -n com.smartwasp.assistant.daemonservice/.service.SwDaemonService
//        installSilent("/mnt/sdcard/update.apk")
    }
//
//    private fun installSilent(filePath:String){
//        val apk = File(filePath)
//        val packageManager = packageManager
//        val pmClz = packageManager.javaClass
//        val aClass = Class.forName("android.app.PackageInstallObserver")
//        val constructor = aClass.getDeclaredConstructor()
//        constructor.isAccessible = true
//        val installObserver = constructor.newInstance()
//        val method = pmClz.getDeclaredMethod("installPackage",
//            Uri::class.java,aClass,Int::class.java,String::class.java)
//        method.isAccessible = true
//        method.invoke(packageManager, Uri.fromFile(apk),installObserver,2,null)
//    }
}