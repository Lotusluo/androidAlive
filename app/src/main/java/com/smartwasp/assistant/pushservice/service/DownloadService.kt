package com.smartwasp.assistant.pushservice.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.smartwasp.assistant.pushservice.util.Etag
import com.smartwasp.assistant.pushservice.util.RetrofitManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val ACTION_FOO = "com.smartwasp.assistant.app.service.action.FOO"

private const val EXTRA_PARAM1 = "com.smartwasp.assistant.app.service.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.smartwasp.assistant.app.service.extra.PARAM2"
/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class DownloadService : IntentService("DownService"){

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_FOO -> {
                //下载地址
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                //MD5文件校验值
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFoo(param1!!,param2!!)
            }
        }
    }

    /**
     * 更新下载文件
     * @param path 下载的地址
     * @param md5 md5文件校验值
     */
    private fun handleActionFoo(path:String,md5:String){
        val result = RetrofitManager.get().retrofitApiService?.download(path)
        result?.let {call->
            val responseBody = call.execute()
            if(responseBody.isSuccessful){
                responseBody?.body()?.let {
                    try{
                        val saveFile = File(applicationContext.externalCacheDir,"update.bat")
                        if(saveFile.exists() || saveFile.createNewFile()){
                            //开始下载
                            val mTotal = it.contentLength()
                            val inputStream = BufferedInputStream(it.byteStream())
                            val outputStream = FileOutputStream(saveFile)
                            var buffer = ByteArray(1024)
                            var len:Int
                            var mLength:Long = 0
                            while (((inputStream.read(buffer)).also {rd-> len = rd }) != -1) {
                                outputStream.write(buffer, 0, len)
                                mLength+=len
                            }
                            try {
                                inputStream.close()
                                outputStream.close()
                            }catch (e:IOException){}
                            finally {
                                val apk = File(saveFile.parent, "update.apk")
                                if(mTotal == mLength && saveFile.renameTo(apk)){
                                    if(Etag.file(apk).trim() == md5.trim()) {
                                        try{
                                            val installResult = installSilent(apk.absolutePath)
                                            Log.d("DownloadService","静默安装:$installResult")
                                        }catch (e:Exception){ }
                                    }else
                                        showErrNotification(1)
                                }
                                else showErrNotification(2)
                            }
                        }else{
                            showErrNotification(3)
                        }
                    }catch(e:IOException) {
                        showErrNotification(4)
                    }
                }?: kotlin.run {
                    showErrNotification(5)
                }
            }else{
                showErrNotification(6)
            }
        }?: kotlin.run {
            showErrNotification(7)
        }
    }

    /**
     * 显示下载失败提示
     */
    private fun showErrNotification(err:Int){
        Log.e("DownloadService","showErrNotification:$err")
    }

    /**
     * 静默安装
     * @param
     */
    @Throws(Exception::class)
    private fun installSilent(filePath:String){
        val apk = File(filePath)
        val packageManager = packageManager
        val pmClz = packageManager.javaClass
        val aClass = Class.forName("android.app.PackageInstallObserver")
        val constructor = aClass.getDeclaredConstructor()
        constructor.isAccessible = true
        val installObserver = constructor.newInstance()
        val method = pmClz.getDeclaredMethod("installPackage",Uri::class.java,aClass,Int::class.java,String::class.java)
        method.isAccessible = true
        method.invoke(packageManager,Uri.fromFile(apk),installObserver,2,null)
    }

    companion object {
        @JvmStatic
        fun startActionFoo(context: Context, param1: String, param2:String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_FOO
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}