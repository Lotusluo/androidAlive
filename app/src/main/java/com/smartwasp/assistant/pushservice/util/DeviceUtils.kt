package com.smartwasp.assistant.pushservice.util

import android.annotation.SuppressLint
import android.os.Build

// Created by luotao on 2021/4/13
object DeviceUtils {
    /**
     * 获取设备ID
     */
    fun getDeviceId(): String {
        return try {
            @SuppressLint("MissingPermission")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1)
                Build.SERIAL
            else {
                Build.getSerial()
            }
        } catch (e: SecurityException) { ""
        }
    }
}