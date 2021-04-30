package com.smartwasp.assistant.pushservice.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Created by luotao on 2020/12/26 15:05
 * E-Mail Address：gtkrockets@163.com
 */
class AppExecutors private constructor(
        private val netIO: Executor,
        private val diskIO: Executor,
        private val mainThread: IExecutor
){

    companion object {
        @Volatile
        private var instance: AppExecutors? = null
            get() {
                if (field == null) {
                    field = AppExecutors(
                            OutIOThreadExecutor(),
                            OutIOThreadExecutor(),
                            MainThreadExecutor()
                    )
                }
                return field
            }
        @Synchronized
        fun get(): AppExecutors {
            return instance!!
        }
    }

    /**
     * 获取磁盘读写线程池
     */
    fun netIO(): Executor {
        return netIO
    }

    /**
     * 获取磁盘读写线程池
     */
    fun diskIO(): Executor {
        return diskIO
    }


    /**
     * 获取主线程执行线程池
     */
    fun mainThread(): IExecutor {
        return mainThread
    }

    /**
     * 主线程线程执行器
     * 匿名静态内部类
     */
    class MainThreadExecutor: IExecutor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun executeDelay(command: Runnable, delay: Long) {
            mainThreadHandler.postDelayed(command, delay)
        }

        override fun removeCallbacks(r: Runnable) {
            mainThreadHandler.removeCallbacks(r)
        }

        override fun execute(p0: Runnable) {
            mainThreadHandler.post(p0)
        }

    }
}