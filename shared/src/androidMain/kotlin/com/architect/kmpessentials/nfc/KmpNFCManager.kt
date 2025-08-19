package com.architect.kmpessentials.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.architect.kmpessentials.KmpAndroid
import com.architect.kmpessentials.internal.ActionStringParams

actual class KmpNFCManager {
    actual companion object {
        private val adapter: NfcAdapter?
            get() = KmpAndroid.clientAppContext?.let { NfcAdapter.getDefaultAdapter(it) }

        private var isListening = false
        private var onValue: ActionStringParams? = null

        // Foreground Dispatch state
        private var foregroundEnabled = false
        private var pendingIntent: PendingIntent? = null
        private var intentFilters: Array<IntentFilter>? = null
        private var techLists: Array<Array<String>>? = null

        fun handleOnNewIntent(intent: Intent?) {
            if (!foregroundEnabled || intent == null) return
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) handleTag(tag)
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        actual fun startListening(nfcScopeVal: ActionStringParams) {
            if (isListening) return
            val activity = KmpAndroid.clientAppContext ?: return
            val nfc = adapter ?: return

            onValue = nfcScopeVal
            val readerEnabled = tryEnableReaderMode(nfc, activity)
            if (!readerEnabled) {
                // Fallback for devices/firmwares where ReaderMode is unsupported (e.g., some Samsung watches)
                enableForegroundDispatch(nfc, activity)
            }

            isListening = true
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        actual fun stopListening() {
            if (!isListening) return
            val activity = KmpAndroid.clientAppContext
            val nfc = adapter

            // Disable Reader Mode (safe to call even if it wasn't active)
            if (activity != null && nfc != null) {
                runCatching { nfc.disableReaderMode(activity) }
            }

            // Disable Foreground Dispatch if enabled
            if (foregroundEnabled && activity != null && nfc != null) {
                runCatching { nfc.disableForegroundDispatch(activity) }
                foregroundEnabled = false
            }

            pendingIntent = null
            intentFilters = null
            techLists = null

            isListening = false
            onValue = null
        }

        private fun tryEnableReaderMode(nfc: NfcAdapter, activity: FragmentActivity): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false
            return try {
                val flags =
                    NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_NFC_B or
                            NfcAdapter.FLAG_READER_NFC_F or
                            NfcAdapter.FLAG_READER_NFC_V or
                            NfcAdapter.FLAG_READER_NFC_BARCODE or
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

                val extras = Bundle().apply {
                    putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 100)
                }

                nfc.enableReaderMode(
                    activity,
                    ReaderCallback { tag -> handleTag(tag) },
                    flags,
                    extras
                )
                true
            } catch (_: Throwable) {
                false
            }
        }

        private fun enableForegroundDispatch(nfc: NfcAdapter, activity: FragmentActivity) {
            val selfIntent = Intent(activity, activity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                selfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                runCatching { addDataType("*/*") }
            }
            val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            val tag = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            intentFilters = arrayOf(ndef, tech, tag)

            techLists = arrayOf(
                arrayOf(android.nfc.tech.Ndef::class.java.name),
                arrayOf(android.nfc.tech.NdefFormatable::class.java.name),
                arrayOf(android.nfc.tech.NfcA::class.java.name),
                arrayOf(android.nfc.tech.NfcB::class.java.name),
                arrayOf(android.nfc.tech.NfcF::class.java.name),
                arrayOf(android.nfc.tech.NfcV::class.java.name),
                arrayOf(android.nfc.tech.MifareClassic::class.java.name),
                arrayOf(android.nfc.tech.MifareUltralight::class.java.name)
            )

            nfc.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists)
            foregroundEnabled = true
        }

        private fun handleTag(tag: Tag) {
            val value = readFirstNdefTextOrUri(tag)
                ?: tag.id?.joinToString("") { "%02X".format(it) }
                ?: "UNKNOWN_TAG"
            onValue?.invoke(value)
        }

        private fun readFirstNdefTextOrUri(tag: Tag): String? {
            val ndef = android.nfc.tech.Ndef.get(tag) ?: return null
            ndef.connect()
            return try {
                val msg: NdefMessage = ndef.ndefMessage ?: return null
                msg.records.asSequence().mapNotNull { decodeRecord(it) }.firstOrNull()
            } finally {
                runCatching { ndef.close() }
            }
        }

        private fun decodeRecord(record: NdefRecord): String? = when {
            record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    record.type.contentEquals(NdefRecord.RTD_TEXT) ->
                decodeText(record.payload)

            record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    record.type.contentEquals(NdefRecord.RTD_URI) ->
                decodeUri(record.payload)

            record.tnf == NdefRecord.TNF_MIME_MEDIA ->
                record.payload?.toString(Charsets.UTF_8)?.takeIf { it.isNotBlank() }

            else -> null
        }

        private fun decodeText(payload: ByteArray?): String? {
            if(payload == null) return null
            if (payload.isEmpty()) return null
            val status = payload[0].toInt()
            val langLen = status and 0x3F
            val isUtf16 = (status and 0x80) != 0
            val charset = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8
            val start = 1 + langLen
            if (start > payload.size) return null
            return payload.copyOfRange(start, payload.size).toString(charset).trim()
        }

        private fun decodeUri(payload: ByteArray?): String? {
            if(payload == null) return null
            if (payload.isEmpty()) return null
            val prefixCode = payload[0].toInt() and 0xFF
            val rest = payload.copyOfRange(1, payload.size).toString(Charsets.UTF_8)
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