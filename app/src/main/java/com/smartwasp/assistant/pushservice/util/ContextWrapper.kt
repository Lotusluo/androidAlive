package com.smartwasp.assistant.pushservice.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.UserHandle

/**
 * 上下文包装器
 */
object ContextWrapper {
    fun startServiceAsUser(context: Context, intent: Intent, userHandle: String) {
        try {
            val clz = context::class.java
            val method =
                clz.getMethod("startServiceAsUser", Intent::class.java, UserHandle::class.java)
            method.isAccessible = true
            method.invoke(context, intent, newUserHandle(userHandle))
        } catch (t: Throwable) {
        }
    }

    fun startActivityAsUser(activity: Activity, intent: Intent, userHandle: String) {
        try {
            val clz = activity::class.java
            val method =
                clz.getMethod("startActivityAsUser", Intent::class.java, UserHandle::class.java)
            method.isAccessible = true
            method.invoke(activity, intent, newUserHandle(userHandle))
        } catch (t: Throwable) {
        }
    }

    fun getBroadcastAsUser(
        context: Context?,
        requestCode: Int,
        intent: Intent,
        flags: Int,
        userHandle: String
    ): PendingIntent? {
        try {
            val clz = PendingIntent::class.java
            val method =
                clz.getMethod(
                    "getBroadcastAsUser",
                    Context::class.java,
                    Int::class.java,
                    Intent::class.java,
                    Int::class.java,
                    UserHandle::class.java
                )
            method.isAccessible = true
            return method.invoke(
                null,
                context,
                requestCode,
                intent,
                flags,
                    newUserHandle(userHandle)
            ) as? PendingIntent
        } catch (t: Throwable) {
        }
        return null
    }

    fun startForegroundServiceAsUser(context: Context, intent: Intent, userHandle: String) {
        try {
            val clz = context::class.java
            val method =
                clz.getMethod(
                    "startForegroundServiceAsUser",
                    Intent::class.java,
                    UserHandle::class.java
                )
            method.isAccessible = true
            method.invoke(context, intent, newUserHandle(userHandle))
        } catch (t: Throwable) {
        }
    }

    fun sendBroadcastAsUser(context: Context,intent: Intent,userHandle:String){
        context.sendBroadcastAsUser(intent, newUserHandle(userHandle))
    }

    fun sendBroadcastAsUser(context: Context,intent: Intent,userHandle:String,permission:String){
        context.sendBroadcastAsUser(intent, newUserHandle(userHandle),permission)
    }

    private fun newUserHandle(userHandle: String): UserHandle? {
        try {
            val clz = UserHandle::class.java
            val all = clz.getDeclaredField(userHandle)
            all.isAccessible = true
            return all.get(null) as UserHandle
        } catch (t: Throwable) {
        }
        return null
    }
}