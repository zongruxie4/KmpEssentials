package com.architect.kmpessentials.email

import com.architect.kmpessentials.aliases.DefaultAction
import com.architect.kmpessentials.internal.ActionBoolParams
import com.architect.kmpessentials.toast.KmpToast
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

internal object URIEncoder {
    fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        } catch (e: Exception) {
            value
        }
    }
}

actual class KmpEmail {
    actual companion object {
        actual fun isEmailSupported(action: ActionBoolParams) {
            action(
                Desktop.isDesktopSupported() && Desktop.getDesktop()
                    .isSupported(Desktop.Action.MAIL)
            )
        }

        actual fun openEmailClientApp(noAppsFound: DefaultAction?){

        }

        actual fun sendEmailToAddress(address: String, emailSubject: String, emailMessage: String, promptInternal: Boolean) {
            // Construct mailto URI
            val uriStr = java.lang.String.format(
                "mailto:%s?subject=%s&body=%s",
                address,
                URIEncoder.encode(emailSubject),
                URIEncoder.encode(emailMessage)
            )

            // Launch email client
            Desktop.getDesktop().mail(URI(uriStr))
        }

        actual fun sendEmailsToCCAddress(
            address: String,
            ccAddresses: Array<String>?,
            emailSubject: String,
            emailMessage: String, promptInternal: Boolean
        ) {
            // Email URI
            val recipient =
                if (!ccAddresses.isNullOrEmpty()) address + "," + ccAddresses.joinToString() else address

            // Construct mailto URI
            val uriStr = java.lang.String.format(
                "mailto:%s?subject=%s&body=%s",
                recipient,
                URIEncoder.encode(emailSubject),
                URIEncoder.encode(emailMessage)
            )

            // Launch email client
            Desktop.getDesktop().mail(URI(uriStr))
        }
    }
}