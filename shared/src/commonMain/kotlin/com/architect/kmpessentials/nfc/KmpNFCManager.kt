package com.architect.kmpessentials.nfc

import com.architect.kmpessentials.internal.ActionStringParams

/**
 * Used for handling communication with NFC devices
 * */
expect class KmpNFCManager {
    companion object {
        /**
         * Turns on NFC Listener
         * @param nfcScopeVal the value fetched from the NFC card/device
         * */
        fun startListening(
            nfcScopeVal: ActionStringParams
        )

        /**
         * Turns off the NFC listener
         * */
        fun stopListening()
    }
}