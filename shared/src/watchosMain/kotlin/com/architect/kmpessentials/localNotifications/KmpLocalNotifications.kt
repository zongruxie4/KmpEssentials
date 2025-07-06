package com.architect.kmpessentials.localNotifications

import com.architect.kmpessentials.mainThread.KmpMainThread
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

actual class KmpLocalNotifications {
    actual companion object {
        actual fun sendNotificationWithLowPriority(title: String, message: String){

        }

        actual fun sendNotification(title: String, message: String) {
            KmpMainThread.runViaMainThread {
                val notificationContent = UNMutableNotificationContent()
                notificationContent.setTitle(title)
                notificationContent.setBody(message)

                val notificationRequest =
                    UNNotificationRequest.requestWithIdentifier("", notificationContent, null)

                UNUserNotificationCenter.currentNotificationCenter()
                    .addNotificationRequest(notificationRequest) {
                        if (it != null) {
                            // failed to push local notification
                        }
                    }
            }
        }

        actual fun scheduleAlarmNotification(
            durationMS: Long,
            title: String,
            message: String
        ): String {
            return ""
        }

        actual fun scheduleAlarmNotificationRepeating(
            durationMS: Long,
            intervalMs: Long,
            title: String,
            message: String
        ): String {
            return ""
        }

        actual fun cancelAllAlarms() {
        }

        actual fun cancelAlarmWithId(alarmId: String) {

        }

        actual fun isSchedulingAlarmWithId(alarmId: String): Boolean {
            TODO("Not yet implemented")
        }
    }
}