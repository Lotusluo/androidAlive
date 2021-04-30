package com.smartwasp.assistant.pushservice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class SwBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //启动服务
        context.startService(Intent("com.smartwasp.assistant.action.CALL").setPackage(context.packageName))
    }
}