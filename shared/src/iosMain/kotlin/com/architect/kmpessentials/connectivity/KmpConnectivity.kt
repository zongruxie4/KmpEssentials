package com.architect.kmpessentials.connectivity

import com.architect.kmpessentials.internal.ActionBoolParams
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class KmpConnectivity {
    actual companion object {
        private val konnection = Konnection.instance
        private var hasConnection = false
        init {
            GlobalScope.launch {
                konnection.observeHasConnection().collect{
                    hasConnection = it
                }
            }
        }

        actual fun isConnected(): Boolean {
            return hasConnection
        }

        actual fun getCurrentNetworkName(): String? {
            return konnection.getCurrentNetworkConnection()?.name
        }

        actual suspend fun listenToConnectionChange(connectionState: ActionBoolParams) {
            konnection.observeHasConnection().collect { hasConnection ->
                connectionState(hasConnection)
            }
        }

        actual suspend  fun getCurrentNetworkIPv4(): String?{
            return konnection.getInfo()?.ipv4
        }

        actual suspend  fun getCurrentNetworkIPv6(): String?{
            return konnection.getInfo()?.ipv6
        }
    }
}