package com.smartwasp.assistant.pushservice.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by luotao on 2021/2/1 11:32
 * E-Mail Address：gtkrockets@163.com
 */
data class BaseBean<T>(
                       @SerializedName(value = "errCode",alternate = ["code"])
                       var errCode:Int,
                       @SerializedName(value = "errMsg",alternate = ["msg"])
                       var errMsg:String,
                       var data: T?):Serializable