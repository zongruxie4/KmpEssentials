package com.architect.kmpessentials.email

import com.architect.kmpessentials.aliases.DefaultAction
import com.architect.kmpessentials.email.delegates.EmailReceipientDelegate
import com.architect.kmpessentials.internal.ActionBoolParams
import com.architect.kmpessentials.logging.KmpLogging
import com.architect.kmpessentials.logging.constants.ErrorCodes
import com.architect.kmpessentials.mainThread.KmpMainThread
import com.architect.kmpessentials.toast.KmpToast
import platform.Foundation.NSURL
import platform.MessageUI.MFMailComposeViewController
import platform.UIKit.UIApplication

actual class KmpEmail {
    actual companion object {
        private val mailDelegate = EmailReceipientDelegate()
        actual fun isEmailSupported(action: ActionBoolParams) {
            KmpMainThread.runViaMainThread {
                action(MFMailComposeViewController.canSendMail())
            }
        }

        actual fun openEmailClientApp(noAppsFound: DefaultAction?){
            val mailtoUrl = NSURL.URLWithString("mailto:")

            if (mailtoUrl != null && UIApplication.sharedApplication.canOpenURL(mailtoUrl)) {
                UIApplication.sharedApplication.openURL(mailtoUrl)
            } else {
                KmpToast.showToastShort("No email app can be found or is available on this device.")
            }
        }

        actual fun sendEmailToAddress(address: String, emailSubject: String, emailMessage: String) {
            isEmailSupported {
                if (it) {
                    KmpMainThread.runViaMainThread {
                        try {
                            val mailController = MFMailComposeViewController().apply {
                                setToRecipients(arrayListOf(address))
                                setMailComposeDelegate(mailDelegate)
                                setSubject(emailSubject)
                                setMessageBody(emailMessage, false)
                            }

                            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                                mailController,
                                true
                            ) {

                            }
                        } catch (ex: Exception) {
                            KmpLogging.writeErrorWithCode(ErrorCodes.HARDWARE_SPECS_NOT_MET)
                        }
                    }
                } else {
                    KmpLogging.writeError(
                        "KMP_ESSENTIALS_EMAIL",
                        "Email is not supported on this device"
                    )
                }
            }
        }

        actual fun sendEmailsToCCAddress(
            address: String,
            ccAddresses: Array<String>?,
            emailSubject: String,
            emailMessage: String
        ) {
            isEmailSupported {
                if (it) {
                    KmpMainThread.runViaMainThread {
                        try {
                            val mailController = MFMailComposeViewController().apply {
                                setToRecipients(arrayListOf(address, ccAddresses))
                                setMailComposeDelegate(mailDelegate)
                                setSubject(emailSubject)
                                setMessageBody(emailMessage, false)
                            }

                            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                                mailController,
                                true
                            ) {

                            }
                        } catch (ex: Exception) {
                            KmpLogging.writeErrorWithCode(ErrorCodes.HARDWARE_SPECS_NOT_MET)
                        }
                    }
                } else {
                    KmpLogging.writeError(
                        "KMP_ESSENTIALS_EMAIL",
                        "Email is not supported on this device"
                    )
                }
            }
        }
    }
}