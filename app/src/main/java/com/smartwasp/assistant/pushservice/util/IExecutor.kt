package com.smartwasp.assistant.pushservice.util

import androidx.annotation.NonNull
import java.util.concurrent.Executor

/**
 * @name :      IExecutor
 * @author :    luotao
 * @date :      2020/12/26 14:58
 * @description :
 */
interface IExecutor:Executor {
    /**
     * 延迟执行一个runnable
     */
    fun executeDelay(@NonNull command: Runnable, delay: Long)

    /**
     * 移除一个将要执行的runnable
     */
    fun removeCallbacks(r: Runnable)
}