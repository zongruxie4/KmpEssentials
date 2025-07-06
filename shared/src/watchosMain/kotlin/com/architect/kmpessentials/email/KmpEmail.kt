package com.architect.kmpessentials.email

import com.architect.kmpessentials.aliases.DefaultAction
import com.architect.kmpessentials.internal.ActionBoolParams
import com.architect.kmpessentials.toast.KmpToast
import platform.Foundation.NSURL
import platform.WatchKit.WKApplication

actual class KmpEmail {
    actual companion object {
        actual fun isEmailSupported(action: ActionBoolParams) {

        }

        actual fun openEmailClientApp(noAppsFound: DefaultAction?) {
            val mailtoUrl = NSURL.URLWithString("mailto:")
            if (mailtoUrl != null) {
                WKApplication.sharedApplication().openSystemURL(mailtoUrl)
            }
        }

        actual fun sendEmailToAddress(address: String, emailSubject: String, emailMessage: String) {

        }

        actual fun sendEmailsToCCAddress(
            address: String,
            ccAddresses: Array<String>?,
            emailSubject: String,
            emailMessage: String
        ) {

        }
    }
}