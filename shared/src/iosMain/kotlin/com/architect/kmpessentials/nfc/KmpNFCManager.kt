@file:Suppress("CONFLICTING_OVERLOADS")

package com.architect.kmpessentials.nfc

import com.architect.kmpessentials.internal.ActionStringParams
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.CoreNFC.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject

actual class KmpNFCManager {
    actual companion object {
        private var session: NFCNDEFReaderSession? = null
        private var isListening = false
        private var onValue: ((String) -> Unit)? = null

        private var viewControllerProvider: (() -> UIViewController?)? = null
        fun bind(presenterProvider: () -> UIViewController?) { viewControllerProvider = presenterProvider }

        actual fun startListening(nfcScopeVal: ActionStringParams) {
            if (isListening) return
            if (!NFCNDEFReaderSession.readingAvailable()) return

            onValue = nfcScopeVal

            val delegate = NdefDelegate { value ->
                onValue?.invoke(value)
            }

            // Presenting VC is optional; CoreNFC shows its own sheet.
            @Suppress("UNUSED_VARIABLE")
            val vc = viewControllerProvider?.invoke() ?: topViewController()

            session = NFCNDEFReaderSession(
                delegate = delegate,
                queue = null,
                invalidateAfterFirstRead = false
            ).apply {
                alertMessage = "Hold your iPhone near the pump's NFC tag."
                beginSession()
            }

            isListening = true
        }

        /** Stop reading. */
        actual fun stopListening() {
            if (!isListening) return
            session?.invalidateSession()
            session = null
            isListening = false
            onValue = null
        }

        private fun topViewController(): UIViewController? {
            val keyWindow = UIApplication.sharedApplication.keyWindow
                ?: UIApplication.sharedApplication.windows.lastOrNull() as? UIWindow
            var top = keyWindow?.rootViewController
            while (top?.presentedViewController != null) top = top?.presentedViewController
            return top
        }

        private class NdefDelegate(
            private val onDecoded: (String) -> Unit
        ) : NSObject(), NFCNDEFReaderSessionDelegateProtocol {

            override fun readerSession(session: NFCNDEFReaderSession, didInvalidateWithError: NSError) {
                // Session ended/failed. Caller may choose to restart; no-op here.
            }

            override fun readerSession(session: NFCNDEFReaderSession, didDetectNDEFs: List<*>) {
                val messages = didDetectNDEFs.filterIsInstance<NFCNDEFMessage>()
                for (msg in messages) {
                    @Suppress("UNCHECKED_CAST")
                    val records = msg.records as List<NFCNDEFPayload>
                    for (rec in records) {
                        decodeRecord(rec)?.let {
                            onDecoded(it)
                            return
                        }
                    }
                }
            }

            @OptIn(ExperimentalForeignApi::class)
            private fun decodeRecord(rec: NFCNDEFPayload): String? = when (rec.typeNameFormat) {
                NFCTypeNameFormatNFCWellKnown -> when (rec.type?.utf8String()) {
                    "T" -> decodeText(rec.payload)
                    "U" -> decodeUri(rec.payload)
                    else -> null
                }
                NFCTypeNameFormatMedia -> rec.payload.utf8String()?.takeIf { it.isNotBlank() }
                else -> null
            }

            @OptIn(ExperimentalForeignApi::class)
            private fun decodeText(payload: NSData?): String? {
                if (payload == null || payload.length.toInt() == 0) return null
                val bytes = payload.bytes?.reinterpret<ByteVar>() ?: return null
                val len = payload.length.toInt()
                val status = bytes[0].toInt()
                val langLen = status and 0x3F
                val isUtf16 = (status and 0x80) != 0
                val start = 1 + langLen
                if (start >= len) return null

                val textData = payload.subdataWithRange(
                    NSMakeRange(start.toULong(), (len - start).toULong())
                )

                return if (isUtf16) textData.utf16String()?.trim()
                else textData.utf8String()?.trim()
            }

            @OptIn(ExperimentalForeignApi::class)
            private fun decodeUri(payload: NSData?): String? {
                if (payload == null || payload.length.toInt() == 0) return null
                val bytes = payload.bytes?.reinterpret<ByteVar>() ?: return null
                val len = payload.length.toInt()
                val prefixCode = bytes[0].toInt() and 0xFF
                val restData = payload.subdataWithRange(NSMakeRange(1uL, (len - 1).toULong()))
                val rest = restData.utf8String() ?: return null
                val prefix = URI_PREFIX_MAP[prefixCode] ?: ""
                return (prefix + rest).trim()
            }

            private val URI_PREFIX_MAP = mapOf(
                0x00 to "",
                0x01 to "http://www.",
                0x02 to "https://www.",
                0x03 to "http://",
                0x04 to "https://",
                0x05 to "tel:",
                0x06 to "mailto:",
                0x0D to "urn:nfc:"
            )
        }
    }
}

private fun NSData?.utf8String(): String? {
    this ?: return null
    return NSString.create(this, encoding = NSUTF8StringEncoding)?.toString()
}

private fun NSData?.utf16String(): String? {
    this ?: return null
    return NSString.create(this, encoding = NSUTF16StringEncoding)?.toString()
}