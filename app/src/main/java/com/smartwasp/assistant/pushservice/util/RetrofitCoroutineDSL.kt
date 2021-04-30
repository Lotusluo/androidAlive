package com.smartwasp.assistant.pushservice.util

import retrofit2.Call

class RetrofitCoroutineDSL<T> {

    var api: (Call<T>)? = null
    internal var onSuccess: ((T) -> Unit)? = null
        private set
    internal var onFail: ((errorCode: Int,msg: String?) -> Unit)? = null
        private set

    /**
     * 获取数据成功
     * @param block (T) -> Unit
     */
    fun onSuccess(block: (T) -> Unit) {
        this.onSuccess = block
    }

    /**
     * 获取数据失败
     * @param block (errorCode: Int,msg: String) -> Unit
     */
    fun onFail(block: (errorCode: Int,msg: String?) -> Unit) {
        this.onFail = block
    }

    internal fun clean() {
        onSuccess = null
        onFail = null
    }
}