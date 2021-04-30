package com.smartwasp.assistant.pushservice.util

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object TerminalUtils {
    fun execute(command: String): String? {
        try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val respBuff = StringBuffer()
            val buff = CharArray(1024)
            var count = reader.read(buff)
            while (count != -1) {
                respBuff.append(buff, 0, count)
                count = reader.read(buff)
            }
            reader.close()
            val result = respBuff.toString().trim()
            Log.d(TerminalUtils::class.java.simpleName, "COMMAND $command result: $result")
            return result
        } catch (t: Throwable) {
            Log.e("TerminalUtils",t.toString())
            t.printStackTrace()
        }
        return null
    }
}