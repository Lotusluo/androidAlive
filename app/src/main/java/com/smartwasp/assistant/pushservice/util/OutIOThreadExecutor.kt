package com.smartwasp.assistant.pushservice.util

import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @name :      IExecutor
 * @author :    luotao
 * @date :      2020/12/26 15:58
 * @description :
 */
class OutIOThreadExecutor: Executor {
    private var ___IO:ExecutorService? = null
    init {
        ___IO = Executors.newSingleThreadExecutor()
    }


    /**
     * 执行
     */
    override fun execute(p0: Runnable?) {
        p0?.let {
            ___IO?.execute(p0)
        }
    }
}