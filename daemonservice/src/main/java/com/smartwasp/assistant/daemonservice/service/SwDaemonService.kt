package com.smartwasp.assistant.daemonservice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smartwasp.assistant.daemonservice.receiver.SwUpdateReceiver
import com.smartwasp.assistant.pushservice.ISwInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SwDaemonService : Service() {
    companion object{
        const val TAG = "SwDaemonService"
    }

    /**
     * 守护线程产生
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"守护进程拉起")
        bindPushService()
        setForeground()
        registerReceiver(updateReceiver, IntentFilter().apply {
            addAction("android.intent.action.PACKAGE_INSTALL")
            addAction("android.intent.action.PACKAGE_ADDED")
            addAction("android.intent.action.PACKAGE_REPLACED")
        })
    }
    /**
     * 启动前台服务，保活处理
     */
    private fun setForeground(){
        val notificationManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "channel_id")
        with(builder){
            setSmallIcon(android.R.drawable.presence_online)
            setAutoCancel(false)
            setSound(null)
            setOnlyAlertOnce(true)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                setChannelId("notification_id")
                val channel = NotificationChannel(
                    "notification_id",
                    "support_service",
                    NotificationManager.IMPORTANCE_MIN
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
        startForeground(100, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private val updateReceiver = SwUpdateReceiver()

    /**
     * 绑定守护服务
     */
    private fun bindPushService(){
        if(!applicationContext.bindService(Intent().apply {
                    component = ComponentName(
                            "com.smartwasp.assistant.pushservice",
                            "com.smartwasp.assistant.pushservice.service.SwPushService"
                    )
                    setPackage(packageName)
                    type = packageName
                }, mServiceConnection, Context.BIND_AUTO_CREATE)){
            Log.e(TAG,"绑定失败")
        }
        startService(Intent().apply {
            action = "com.smartwasp.assistant.daemonservice"
            component = ComponentName(
                    "com.smartwasp.assistant.pushservice",
                    "com.smartwasp.assistant.pushservice.service.SwPushService"
            )
            setPackage(packageName)
        })
    }

    /**
     * 服务销毁
     */
    override fun onDestroy() {
        val notificationManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(100)
        try {
            unbindService(mServiceConnection)
        }catch (e:Throwable){}
        try {
            unregisterReceiver(updateReceiver)
        }catch (e:Throwable){}
        Log.d(TAG,"守护进程关闭")
        super.onDestroy()
    }

    //跨进程binder
    private val binder = SwDaemonServiceBinder()

    /**
     * 返回绑定器
     */
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class SwDaemonServiceBinder: ISwInterface.Stub()
    /**
     * 服务连接
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            stopSelf()
            GlobalScope.launch {
                delay(2 * 1000)
                bindPushService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG,"onServiceConnected")
        }
    }
}