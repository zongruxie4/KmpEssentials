package com.architect.kmpessentials.email

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.architect.kmpessentials.KmpAndroid
import com.architect.kmpessentials.aliases.DefaultAction
import com.architect.kmpessentials.internal.ActionBoolParams
import com.architect.kmpessentials.mainThread.KmpMainThread
import com.architect.kmpessentials.toast.KmpToast
import java.net.URLEncoder

actual class KmpEmail {
    actual companion object {
        private val emailPrefix = "mailto:"
        actual fun isEmailSupported(action: ActionBoolParams) {
            KmpMainThread.runViaMainThread {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(emailPrefix) // Use mailto: scheme
                }
                val resolvedActivities =
                    KmpAndroid.applicationContext?.packageManager?.queryIntentActivities(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

                action(
                    !resolvedActivities.isNullOrEmpty()
                )
            }
        }

        actual fun openEmailClientApp(noAppsFound: DefaultAction?) {
            KmpMainThread.runViaMainThread {
                val context = KmpAndroid.getCurrentApplicationContext()
                val emailApps = listOf(
                    // Google
                    "com.google.android.gm",

                    // Microsoft
                    "com.microsoft.office.outlook",

                    // Yahoo
                    "com.yahoo.mobile.client.android.mail",

                    // Samsung
                    "com.samsung.android.email.provider",

                    // ProtonMail
                    "ch.protonmail.android",

                    // BlueMail
                    "me.bluemail.mail",

                    // Spark Email
                    "com.readdle.spark",

                    // Edison Mail
                    "com.edison.email",

                    // K-9 Mail
                    "com.fsck.k9",

                    // Aqua Mail
                    "org.kman.AquaMail",

                    // myMail
                    "com.my.mail",

                    // Zoho Mail
                    "com.zoho.mail",
                    "com.ninefolders.hd3",
                    "de.gmx.mobile.android.mail",
                    "ru.mail.mailapp"
                )

                val emailAppIntent = emailApps
                    .mapNotNull { context.packageManager.getLaunchIntentForPackage(it) }
                    .firstOrNull()

                if (emailAppIntent != null) {
                    context.startActivity(emailAppIntent)
                } else {
                    noAppsFound?.invoke()
                }
            }
        }

        actual fun sendEmailToAddress(
            address: String,
            emailSubject: String,
            emailMessage: String,
            promptInternal: Boolean
        ) {
            KmpMainThread.runViaMainThread {
                try {
                    var emailAddress = address
                    if (!address.startsWith(emailPrefix)) {
                        emailAddress = "$emailPrefix$address"
                    }

                    val emailIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data =
                            Uri.parse(emailAddress.plus("?subject=${emailSubject.encode()}&body=${emailMessage.encode()}"))
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailMessage);
                    }

                    KmpAndroid.applicationContext?.startActivity(emailIntent)
                } catch (_: Exception) {
                    KmpToast.showToastShort("Email is not supported on this device")
                }
            }
        }

        actual fun sendEmailsToCCAddress(
            address: String,
            ccAddresses: Array<String>?,
            emailSubject: String,
            emailMessage: String, promptInternal: Boolean
        ) {
            KmpMainThread.runViaMainThread {
                try {
                    var emailAddress = address
                    if (!address.startsWith(emailPrefix)) {
                        emailAddress = "$emailPrefix$address"
                    }

                    val emailIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data =
                            Uri.parse(emailAddress.plus("?subject=${emailSubject.encode()}&body=${emailMessage.encode()}"))
                        putExtra(Intent.EXTRA_CC, arrayOf(ccAddresses))
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailMessage);
                    }

                    KmpAndroid.applicationContext?.startActivity(emailIntent)
                } catch (_: Exception) {
                    KmpToast.showToastShort("Email is not supported on this device")
                }
            }
        }

        /**
         * '+' needs to be replaced with a whitespace code '%20'
         * @see https://stackoverflow.com/a/4737967/889278
         */
        private fun String.encode() = URLEncoder.encode(this, "UTF-8").replace("+", "%20")
    }
}
