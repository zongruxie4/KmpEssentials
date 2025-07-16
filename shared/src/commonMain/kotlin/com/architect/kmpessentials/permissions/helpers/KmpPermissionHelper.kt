package com.architect.kmpessentials.permissions.helpers

import com.architect.kmpessentials.permissions.KmpPermissionsManager
import com.architect.kmpessentials.permissions.Permission
import com.architect.kmpessentials.permissions.PermissionStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object KmpPermissionHelper {
    suspend fun requestAppPermission(permission: Permission): PermissionStatus {
        return suspendCoroutine { continuation ->
            KmpPermissionsManager.isPermissionGranted(permission) { isGranted ->
                if (isGranted) {
                    continuation.resume(PermissionStatus.Granted)
                } else {
                    KmpPermissionsManager.canShowPromptDialog(permission) { canPrompt ->
                        if (canPrompt) {
                            KmpPermissionsManager.requestPermission(permission) {
                                KmpPermissionsManager.isPermissionGranted(permission) { granted ->
                                    GlobalScope.launch {
                                        if (granted) {
                                            continuation.resume(PermissionStatus.Granted)
                                        } else {
                                            continuation.resume(PermissionStatus.Denied)
                                        }
                                    }
                                }
                            }
                        } else {
                            continuation.resume(PermissionStatus.DeniedAlways)
                        }
                    }
                }
            }
        }
    }
}

