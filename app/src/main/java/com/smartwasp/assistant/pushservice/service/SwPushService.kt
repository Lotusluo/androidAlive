package com.smartwasp.assistant.pushservice.service


import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartwasp.assistant.pushservice.App
import com.smartwasp.assistant.pushservice.ISwInterface
import com.smartwasp.assistant.pushservice.R
import com.smartwasp.assistant.pushservice.bean.CmdBean
import com.smartwasp.assistant.pushservice.model.PushModel
import com.smartwasp.assistant.pushservice.util.AppExecutors
import com.smartwasp.assistant.pushservice.util.TerminalUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * MainActivity
 * @author luotao
 * @date 2021/4/12
 */
class SwPushService : Service(),LifecycleOwner {
    companion object{
        const val TAG = "SwPushService"
        private const val PREFIX = "com.smartwasp.assistant.pushservice"
        const val BROADCAST_CMD = "$PREFIX.CMD"
        const val BROADCAST_CONNECTED = "$PREFIX.CONNECTED"
    }

    private val mLifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): Lifecycle{
        return mLifecycleRegistry
    }

    private lateinit var model: PushModel

    /**
     * 推送服务启动
     * 启动前台进程
     * 启动极光推送
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "主进程拉起")
        bindDaemonService()
        model = PushModel()
        lifecycle.addObserver(model)
        mLifecycleRegistry.currentState = Lifecycle.State.CREATED
        registerReceiver(customReceiver, IntentFilter().apply {
            addAction(BROADCAST_CMD)
            addAction(BROADCAST_CONNECTED)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    //自定义广播接收器
    private val customReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    BROADCAST_CMD -> {
                        val pushCMD = it.getStringExtra("data")
                        Log.d(TAG, "pushCMD:${pushCMD}")
                        pushCMD?.let {
                            try {
                                val cmdBean = Gson().fromJson<CmdBean>(pushCMD, object : TypeToken<CmdBean>() {}.type)
                                Log.d(TAG, "cmd:${cmdBean.cmd}")
                                when (cmdBean.type) {
                                    "adb" -> {
                                        when (cmdBean.cmd.trim()) {
                                            "reboot" -> {
                                                val pm = App._thiz.getSystemService(Context.POWER_SERVICE) as PowerManager
                                                pm.reboot("")
                                            }
                                            "reboot -p" -> {
                                                val intent = Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN")
                                                intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                            }
                                            else -> {
                                                TerminalUtils.execute(cmdBean.cmd)
                                            }
                                        }
                                    }
                                    "fota" -> {
                                        showFotaNotification(cmdBean.cmd)
                                    }
                                    else -> {

                                    }
                                }
                            } catch (e: Throwable) {
                                Log.e(TAG, e.toString())
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    /**
     * 绑定守护服务
     */
    private fun bindDaemonService(){
        //启动绑定的服务
        if(!applicationContext.bindService(Intent().apply {
                    component = ComponentName(
                            "com.smartwasp.assistant.daemonservice",
                            "com.smartwasp.assistant.daemonservice.service.SwDaemonService"
                    )
                    setPackage(packageName)
                    type = packageName
                }, mServiceConnection, Context.BIND_AUTO_CREATE)){
            Log.e(TAG, "绑定失败")
        }
        startService(Intent().apply {
            action = "com.smartwasp.assistant.daemonservice"
            component = ComponentName(
                    "com.smartwasp.assistant.daemonservice",
                    "com.smartwasp.assistant.daemonservice.service.SwDaemonService"
            )
            setPackage(packageName)
        })
    }

    /**
     * 弹出授权错误通知栏
     */
    fun showFotaNotification(cmd:String){
        dismissNotification()
        AppExecutors.get().mainThread().execute {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val view = View.inflate(this, R.layout.layout_fota, null)
            val layoutParams = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            layoutParams.format = PixelFormat.TRANSPARENT
            layoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = 76
            layoutParams.gravity = Gravity.TOP
            windowManager.addView(view, layoutParams)
            fotaNotificationView = view
            view.findViewById<AppCompatTextView>(R.id.notification_message).text = String.format(getString(R.string.fota_update_top),cmd)
            view.findViewById<View>(R.id.notification_positive_action).setOnClickListener {
                launcherFota()
            }
            view.findViewById<View>(R.id.notification_negative_action).setOnClickListener {
                dismissNotification()
            }
        }
    }private var fotaNotificationView: View? = null//悬浮层视图

    private fun launcherFota() {
        try {
            val intent = Intent()
            intent.setClassName("com.smartwasp.fota", "com.smartwasp.fota.MainActivity")
            intent.action = "android.settings.SYSTEM_UPDATE_SETTINGS"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }catch (e:Exception){
            Log.e(TAG,e.toString())
        }
    }


    /**
     * 取消通知栏
     */
    fun dismissNotification() {
        AppExecutors.get().mainThread().execute{
            fotaNotificationView?.let {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeViewImmediate(fotaNotificationView)
                fotaNotificationView = null
            }
        }
    }

    /**
     * 守护服务销毁
     */
    override fun onDestroy() {
        try {
            unbindService(mServiceConnection)
        }catch (e: Throwable){}
        try {
            unregisterReceiver(customReceiver)
        }catch (e: Throwable){}
        mLifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        Log.d(TAG, "主进程关闭")
        super.onDestroy()
    }

    //跨进程binder
    private val binder = SwPushServiceBinder()
    /**
     * 返回绑定器
     */
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class SwPushServiceBinder: ISwInterface.Stub()
    /**
     * 服务连接
     */
    private val mServiceConnection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
            GlobalScope.launch {
                delay(2000)
                bindDaemonService()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
        }
    }
}