package com.architect.kmpessentials.lifecycle.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.architect.kmpessentials.KmpAndroid
import com.architect.kmpessentials.backgrounding.KmpBackgrounding
import com.architect.kmpessentials.lifecycle.KmpLifecycle
import com.architect.kmpessentials.logging.KmpLogging

class AppCloseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            KmpLogging.writeInfo("KMP_ESSENTIALS_APP_STATE", "User swiped away the app from Recents")
            KmpLifecycle.isInForeground = false
            if (!KmpAndroid.allowWorkersToRunBeyondApp) {
                KmpBackgrounding.cancelAllRunningWorkers()
            }
            KmpLifecycle.destroyAction?.invoke()
        }
    }
}