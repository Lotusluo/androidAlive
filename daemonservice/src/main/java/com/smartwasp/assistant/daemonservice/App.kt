package com.smartwasp.assistant.daemonservice

import android.app.Application
import com.smartwasp.assistant.daemonservice.util.DeviceAdminUtil

// Created by luotao on 2021/4/13
class App:Application() {
    companion object{
        lateinit var _thiz:Application
    }

    override fun onCreate() {
        super.onCreate()
        _thiz = this
        DeviceAdminUtil.getInstance().init(this)
    }
}