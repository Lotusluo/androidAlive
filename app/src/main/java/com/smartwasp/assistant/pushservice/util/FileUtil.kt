package com.smartwasp.assistant.pushservice.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.smartwasp.assistant.pushservice.BuildConfig
import java.io.File

/**
 * Created by luotao on 2021/2/25 14:49
 * E-Mail Address：gtkrockets@163.com
 */
object FileUtil {
    /**
     * 根据文件形成URI
     * @param context
     * @param file
     * @return
     */
    fun createUriFromFile(context: Context,file:File): Uri {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        }
    }
}