package com.architect.kmpessentials

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Bundle
import com.architect.kmpessentials.backgrounding.KmpBackgrounding
import com.architect.kmpessentials.lifecycle.KmpLifecycle
import com.architect.kmpessentials.logging.KmpLogging

open class KmpCoreApplication : Application() {
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        val runtime = Runtime.getRuntime()
        val usedHeap = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) // MB
        val maxHeap = runtime.maxMemory() / (1024 * 1024) // MB

        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMem = memoryInfo.availMem / (1024 * 1024) // MB
        val thresholdMem = memoryInfo.threshold / (1024 * 1024) // MB

        KmpLogging.writeInfo(
            "KMP_ESSENTIALS_MEMORY",
            "Trim level: $level | Used Heap: ${usedHeap}MB / ${maxHeap}MB | Available: ${availableMem}MB | Threshold: ${thresholdMem}MB"
        )

        // Detect if the app is being fully terminated
        if (usedHeap == 0L || availableMem <= thresholdMem) {
            KmpLogging.writeInfo("KMP_ESSENTIALS_APP_STATE", "App is being terminated")
            KmpLifecycle.isInForeground = false
            if (!KmpAndroid.allowWorkersToRunBeyondApp) {
                KmpBackgrounding.cancelAllRunningWorkers()
            }
            KmpLifecycle.destroyAction?.invoke()
        }
    }
}