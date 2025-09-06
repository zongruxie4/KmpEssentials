package com.architect.kmpessentials.permissions.delegates

import com.architect.kmpessentials.internal.ActionNoParams
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

class LocationPermissionsDelegate(
    val runAction: ActionNoParams,
    val onDenied: ActionNoParams? = null
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus()) {
            0 -> {// Not Determined
                onDenied?.invoke()
            }

            2 -> // denied
            {
                onDenied?.invoke()
            }

            3, 4 -> { // granted
                runAction()
            }
        }
    }
}