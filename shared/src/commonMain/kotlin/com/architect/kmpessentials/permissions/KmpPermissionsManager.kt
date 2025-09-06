package com.architect.kmpessentials.permissions

import com.architect.kmpessentials.internal.ActionBoolParams
import com.architect.kmpessentials.internal.ActionNoParams
import com.architect.kmpessentials.internal.ActionPermissionStatusParams

expect class KmpPermissionsManager {
    companion object {
        /**
         * Requests a runtime permission based on the "permission" parameter specified
         * Must be invoked on the main thread (otherwise could cause a crash)
         * @param runAction if the user has allowed the permission, selecting "Allow"
         * */
        fun requestPermission(
            permission: Permission,
            runAction: ActionNoParams,
        )

        /**
         * Requests a runtime permission based on the "permission" parameter specified
         * Must be invoked on the main thread (otherwise could cause a crash)
         * @param onDenied if the user has selected "Don't Allow" on the System permission prompt
         * @param runAction if the user has allowed the permission, selecting "Allow"
         * */
        fun requestPermissionVerifyIfDenied(
            permission: Permission,
            runAction: ActionNoParams,
            onDenied: ActionNoParams,
        )

        /**
         * Must be invoked on the main thread (otherwise could cause a crash)
         * @param True if the permission is granted, False if Denied
         * */
        @Deprecated("This API is officially deprecated. Please migrate to \"isPermissionGranted(permission, actionResult)\"")
        fun isPermissionGranted(permission: Permission): Boolean

        /**
         * Must be invoked on the main thread (otherwise could cause a crash)
         * @param True if the permission is granted, False if Denied
         * */
        fun isPermissionGranted(permission: Permission, actionResult: ActionBoolParams)

        /**
         * Gets the current permission state of the permission you've set
         * */
        fun getCurrentPermissionState(permission: Permission, actionResult: ActionPermissionStatusParams)

        /**
         * Must be invoked on the main thread (otherwise could cause a crash)
         * @param True if the permission dialog can be shown, false if cannot be shown (if the user has already denied permission)
         * */
        fun canShowPromptDialog(permission: Permission, actionResult: ActionBoolParams)
    }
}