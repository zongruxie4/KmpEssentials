package com.architect.testclient

import com.architect.kmpessentials.backgrounding.KmpBackgrounding
import com.architect.kmpessentials.launcher.KmpLauncher
import com.architect.kmpessentials.lifecycle.KmpLifecycle
import com.architect.kmpessentials.localNotifications.KmpLocalNotifications
import com.architect.kmpessentials.logging.KmpLogging
import com.architect.kmpessentials.permissions.KmpPermissionsManager
import com.architect.kmpessentials.permissions.Permission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class SharedComponent {
    actual companion object {
        fun runProcess() {
            KmpLifecycle.setAppLifecycleBackground {
                KmpBackgrounding.createAndStartWorker {
                    var item = 1000
                    while (item >= 0) {
                        if (!KmpLifecycle.isCurrentlyInForeground()) {
                            KmpLogging.writeInfo("BACKGROUND_STATE", "APP IS IN BACKGROUND")
                        } else {
                            KmpLogging.writeInfo("FOREGROUND_STATE", "APP IS IN FOREGROUND")
                        }

                        item--
                        KmpLogging.writeInfo("TESTING", "RUNNING_LOG_TEST")
                        delay(1000)

                        KmpLocalNotifications.sendNotification(
                            "Sample Test $item",
                            "Testing again"
                        )
                    }
                }
            }

            KmpLifecycle.setAppLifecycleForeground {
                KmpLauncher.cancelAllTimers()
                KmpBackgrounding.cancelAllRunningWorkers()
            }
        }

        var isRunningTimer = true
        fun runProcessForTimer() {
            KmpLifecycle.setAppLifecycleBackground {
                KmpBackgrounding.createAndStartWorker {
                    KmpLifecycle.waitForAppToReturnToForegroundWithTimeout(5000) {
                        KmpBackgrounding.createAndStartWorker {
                            var item = 1000
                            KmpLauncher.startTimerRepeating(1.0) {
                                item--

                                KmpLogging.writeInfo("TESTING", "RUNNING_LOG_TEST_FOR_TIMER")
                                KmpLocalNotifications.sendNotification(
                                    "Sample Test $item",
                                    "Testing again TIMER"
                                )
                                isRunningTimer
                            }
                        }
                    }
                }
            }

            KmpLifecycle.setAppLifecycleForeground {
                KmpLauncher.cancelAllTimers()
                KmpBackgrounding.cancelAllRunningWorkers()
            }
        }

        actual fun runNativeHandler() {
            KmpPermissionsManager.isPermissionGranted(Permission.PushNotifications) {
                if (it) {
                    runProcessForTimer()
                } else {
                    KmpPermissionsManager.requestPermission(Permission.PushNotifications) {
                        runProcessForTimer()
                    }
                }
            }


//
//            KmpBackgrounding.createAndStartForegroundWorker("Sample", "Test") {
//                var item = 1000
//                while (item >= 0) {
//                    if (!KmpLifecycle.isCurrentlyInForeground()) {
//                        KmpLogging.writeInfo("BACKGROUND_STATE", "APP IS IN BACKGROUND")
//                    } else {
//                        KmpLogging.writeInfo("FOREGROUND_STATE", "APP IS IN FOREGROUND")
//                    }
//
//                    item--
//                    KmpLogging.writeInfo("TESTING", "RUNNING_LOG_TEST")
//                    delay(1000)
//
//                    KmpLocalNotifications.sendNotification("Sample Test $item", "Testing again")
//                }
//            }
        }
    }

}