package com.smartwasp.assistant.pushservice.bean

import android.text.TextUtils
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.smartwasp.assistant.pushservice.App
import com.smartwasp.assistant.pushservice.BuildConfig

data class ConfigBean(val updateInterval:String,val versionCode:Int,val url:String,var md5:String){
    fun getUpdateInterval():Long{
        if(TextUtils.isDigitsOnly(updateInterval)){
            return updateInterval.toLong()
        }
        return 60L
    }

    fun isNewVersion():Boolean{
        return App._thiz?.packageManager?.let {
            val currentVersionCode =  PackageInfoCompat.getLongVersionCode(it.getPackageInfo(BuildConfig.APPLICATION_ID,0))
            versionCode > currentVersionCode
        } ?: kotlin.run {
            false
        }
    }
}