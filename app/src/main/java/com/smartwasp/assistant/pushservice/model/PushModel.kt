package com.smartwasp.assistant.pushservice.model

import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.smartwasp.assistant.pushservice.App
import com.smartwasp.assistant.pushservice.BuildConfig
import com.smartwasp.assistant.pushservice.bean.BaseBean
import com.smartwasp.assistant.pushservice.bean.ConfigBean
import com.smartwasp.assistant.pushservice.service.DownloadService
import com.smartwasp.assistant.pushservice.service.SwPushService
import com.smartwasp.assistant.pushservice.util.AppExecutors
import com.smartwasp.assistant.pushservice.util.DeviceUtils
import com.smartwasp.assistant.pushservice.util.NetWorkUtil
import com.smartwasp.assistant.pushservice.util.RetrofitManager
import java.lang.RuntimeException


class PushModel: BaseModel() {
    companion object{
        const val TAG = "PushModel"
    }

    private var configBean:ConfigBean? = null
    //加载配置
    private fun loadConfig(){
        //开始加载配置
        Log.d(TAG,"加载配置")
        AppExecutors.get().mainThread().removeCallbacks(delayToLoadConfig)
        AppExecutors.get().mainThread().removeCallbacks(delayToReport)
        retrofit<BaseBean<ConfigBean>> {
            api = RetrofitManager.get().retrofitApiService?.loadConfig()
            onSuccess {
                configBean = it.data
                Log.d(TAG,"成功加载配置:$configBean")
                JPushInterface.setTags(App._thiz,101,setOf("69"))
                JPushInterface.setAlias(App._thiz,102, DeviceUtils.getDeviceId())
                if(configBean?.isNewVersion() == true){
                    Log.d(TAG,"准备更新")
                    DownloadService.startActionFoo(App._thiz,configBean!!.url,configBean!!.md5)
                }
                report()
            }
            onFail { _, _ ->
                AppExecutors.get().mainThread().executeDelay(delayToLoadConfig,5 * 1000)
            }
        }
    }


    //上报
    private fun report(){
        AppExecutors.get().mainThread().removeCallbacks(delayToReport)
        AppExecutors.get().mainThread().executeDelay(delayToReport,configBean!!.getUpdateInterval() * 1000)
        retrofit<BaseBean<String>> {
            api = RetrofitManager.get().retrofitApiService?.report()
            onSuccess {}
            onFail { _, _ -> }
        }
    }

    //延迟请求加载配置文件
    private val delayToLoadConfig = Runnable {
        loadConfig()
    }

    //延迟上报
    private val delayToReport = Runnable {
        report()
    }

    override fun onCreate() {
        super.onCreate()
        delayToLoadConfig.run()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppExecutors.get().mainThread().removeCallbacks(delayToLoadConfig)
        AppExecutors.get().mainThread().removeCallbacks(delayToReport)
    }
}
