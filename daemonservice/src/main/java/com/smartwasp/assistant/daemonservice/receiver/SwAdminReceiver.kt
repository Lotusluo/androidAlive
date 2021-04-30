package com.smartwasp.assistant.daemonservice.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SwAdminReceiver : DeviceAdminReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}