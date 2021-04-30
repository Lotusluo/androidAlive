package com.smartwasp.assistant.pushservice


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.smartwasp.assistant.pushservice.service.SwPushService
import com.smartwasp.assistant.pushservice.util.AppExecutors
import com.smartwasp.assistant.pushservice.util.ContextWrapper
import com.smartwasp.assistant.pushservice.util.TerminalUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        AppExecutors.get().diskIO().execute {
            try {
                Runtime.getRuntime().exec("chmod 4755 /system/xbin/su")
                Runtime.getRuntime().exec("/system/xbin/su")
            }catch (e:Throwable){
                Log.e("MainActivity",e.toString())
            }
        }
    }
}