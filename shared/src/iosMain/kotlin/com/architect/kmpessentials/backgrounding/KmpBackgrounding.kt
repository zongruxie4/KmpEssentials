package com.architect.kmpessentials.backgrounding

import com.architect.kmpessentials.aliases.DefaultActionAsync
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGTaskScheduler
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskIdentifier
import platform.UIKit.UIBackgroundTaskInvalid
import platform.UIKit.UIImage
import kotlin.coroutines.cancellation.CancellationException

actual class KmpBackgrounding {
    actual companion object {
        private const val appleDefaultId = "com.kmpessentials.default.backgrounding"
        private val identifiers = mutableListOf<UIBackgroundTaskIdentifier>()
        private val backgroundJobs = mutableListOf<Job>()
        private val foregroundServices = mutableListOf<String>()

        var foregroundIcon: UIImage? = null
        fun setForegroundIcon(foregroundIcon: UIImage?) {
            this.foregroundIcon = foregroundIcon
        }

        actual fun createAndStartWorkerWithoutCancel(
            options: BackgroundOptions?,
            action: DefaultActionAsync
        ) {
            var backgroundId: UIBackgroundTaskIdentifier? = null
            backgroundId =
                UIApplication.sharedApplication.beginBackgroundTaskWithName(appleDefaultId) {

                }

            GlobalScope.launch {
                action()
            }

            UIApplication.sharedApplication.endBackgroundTask(backgroundId)
        }

        actual fun createAndStartWorker(options: BackgroundOptions?, action: DefaultActionAsync) {
            var backgroundId: UIBackgroundTaskIdentifier? = null
            var job : Job? = null

            backgroundId = UIApplication.sharedApplication.beginBackgroundTaskWithName(
                appleDefaultId
            ) {
                if (backgroundId != null && backgroundId != UIBackgroundTaskInvalid) {
                    UIApplication.sharedApplication.endBackgroundTask(backgroundId!!)
                }
            }

            identifiers.add(backgroundId)
            job = GlobalScope.launch {
                try {
                    action()
                } finally {
                    if (backgroundId != UIBackgroundTaskInvalid) {
                        UIApplication.sharedApplication.endBackgroundTask(backgroundId)
                    }
                }
            }
            backgroundJobs.add(job)
        }

        actual fun cancelAllRunningWorkers() {
            if (identifiers.isNotEmpty()) {
                identifiers.forEach {
                    UIApplication.sharedApplication.endBackgroundTask(it)
                }
            }

            if (backgroundJobs.isNotEmpty()) {
                backgroundJobs.forEach {
                    if(!it.isCancelled) {
                        it.cancel()
                    }
                }
            }

            if (foregroundServices.isNotEmpty()) {
                foregroundServices.forEach {
                    BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(it)
                }
            }

            identifiers.clear()
            backgroundJobs.clear()
            foregroundServices.clear()
            KmpForegroundService.stopNotificationService()
        }

        actual fun createAndStartForegroundWorker(
            title: String,
            message: String,
            action: DefaultActionAsync,
        ) {
            KmpForegroundService.startNotificationService(title, message, action)
        }
    }
}