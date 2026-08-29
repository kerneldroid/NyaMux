package com.nyamux.app

import android.app.Activity
import android.os.Process
import android.view.View
import android.view.WindowManager

object GpuAccelHelper {
    fun applyIfEnabled(activity: Activity, enabled: Boolean) {
        if (!enabled) return
        try {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY)
            activity.window?.decorView?.let { v: View ->
                if (v.isHardwareAccelerated) {
                    v.setLayerType(View.LAYER_TYPE_NONE, null)
                }
            }
        } catch (_: Exception) { }
    }

    fun isHardwareAccelerated(activity: Activity): Boolean {
        return try {
            activity.window?.decorView?.isHardwareAccelerated == true
        } catch (_: Exception) { false }
    }
}
