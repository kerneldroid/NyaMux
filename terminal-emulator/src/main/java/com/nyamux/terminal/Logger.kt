package com.nyamux.terminal

import android.util.Log
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

object Logger {

    @JvmStatic
    fun logError(client: TerminalSessionClient?, logTag: String, message: String) {
        if (client != null)
            client.logError(logTag, message)
        else
            Log.e(logTag, message)
    }

    @JvmStatic
    fun logWarn(client: TerminalSessionClient?, logTag: String, message: String) {
        if (client != null)
            client.logWarn(logTag, message)
        else
            Log.w(logTag, message)
    }

    @JvmStatic
    fun logInfo(client: TerminalSessionClient?, logTag: String, message: String) {
        if (client != null)
            client.logInfo(logTag, message)
        else
            Log.i(logTag, message)
    }

    @JvmStatic
    fun logDebug(client: TerminalSessionClient?, logTag: String, message: String) {
        if (client != null)
            client.logDebug(logTag, message)
        else
            Log.d(logTag, message)
    }

    @JvmStatic
    fun logVerbose(client: TerminalSessionClient?, logTag: String, message: String) {
        if (client != null)
            client.logVerbose(logTag, message)
        else
            Log.v(logTag, message)
    }

    @JvmStatic
    fun logStackTraceWithMessage(client: TerminalSessionClient?, tag: String, message: String, throwable: Throwable) {
        getMessageAndStackTraceString(message, throwable)?.let { logError(client, tag, it) }
    }

    @JvmStatic
    fun getMessageAndStackTraceString(message: String?, throwable: Throwable?): String? = when {
        message == null && throwable == null -> null
        message != null && throwable != null -> message + ":\n" + getStackTraceString(throwable)
        throwable == null -> message
        else -> getStackTraceString(throwable)
    }

    @JvmStatic
    fun getStackTraceString(throwable: Throwable?): String? {
        if (throwable == null) return null
        return try {
            StringWriter().use { sw ->
                PrintWriter(sw).use { pw ->
                    throwable.printStackTrace(pw)
                    sw.toString()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
