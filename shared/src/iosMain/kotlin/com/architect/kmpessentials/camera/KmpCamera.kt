package com.architect.kmpessentials.camera

import com.architect.kmpessentials.KmpiOS
import com.architect.kmpessentials.camera.internal.CameraControlDelegate
import com.architect.kmpessentials.camera.internal.VideoCameraControlDelegate
import com.architect.kmpessentials.internal.ActionStringParams
import com.architect.kmpessentials.logging.KmpLogging
import com.architect.kmpessentials.logging.constants.ErrorCodes
import com.architect.kmpessentials.mainThread.KmpMainThread
import com.architect.kmpessentials.permissions.KmpPermissionsManager
import com.architect.kmpessentials.permissions.Permission
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureMode
import platform.UIKit.UIImagePickerControllerSourceType

actual class KmpCamera {
    actual companion object {
        actual fun isSupported(): Boolean {
            return true
        }

        internal lateinit var videoActionResult: ActionStringParams
        private val videoDelegate = VideoCameraControlDelegate()

        internal lateinit var photoActionResult: ActionStringParams
        private val photoDelegate = CameraControlDelegate()

        private fun getCameraDevice(): UIImagePickerController {
            val camera = UIImagePickerController()
            camera.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            camera.allowsEditing = true
            return camera
        }

        private fun postErrorForCamera() {
            KmpLogging.writeError(
                "Camera Hardware Missing",
                "Unsupported device. Missing camera or hardware for this feature. Please try again on another device."
            )
        }

        actual fun capturePhoto(actionResult: ActionStringParams) {
            KmpPermissionsManager.isPermissionGranted(Permission.Camera) { r ->
                if (r) {
                    KmpMainThread.runViaMainThread {
                        if (UIImagePickerController.isSourceTypeAvailable(
                                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                            )
                        ) {
                            photoActionResult = actionResult
                            val camera = getCameraDevice()
                            camera.cameraCaptureMode =
                                UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModePhoto
                            camera.delegate = photoDelegate

                            KmpiOS.presentByDismissingViewModal(camera)
                        } else {
                            postErrorForCamera()
                        }
                    }
                } else {
                    KmpLogging.writeErrorWithCode(ErrorCodes.RUNTIME_PERMISSION_NOT_GRANTED)
                }
            }
        }

        actual fun captureVideo(actionResult: ActionStringParams) {
            KmpPermissionsManager.isPermissionGranted(Permission.Microphone) {
                if (it) {
                    KmpPermissionsManager.isPermissionGranted(Permission.Camera) { r ->
                        if (r) {
                            if (UIImagePickerController.isSourceTypeAvailable(
                                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                                )
                            ) {
                                KmpMainThread.runViaMainThread {
                                    videoActionResult = actionResult

                                    val camera = getCameraDevice()
                                    camera.mediaTypes = listOf("public.movie")
                                    camera.cameraCaptureMode =
                                        UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModeVideo
                                    camera.delegate = videoDelegate

                                    KmpiOS.presentByDismissingViewModal(camera)
                                }
                            } else {
                                postErrorForCamera()
                            }
                        } else {
                            KmpLogging.writeErrorWithCode(ErrorCodes.RUNTIME_PERMISSION_NOT_GRANTED)
                        }
                    }
                } else {
                    KmpLogging.writeErrorWithCode(ErrorCodes.RUNTIME_PERMISSION_NOT_GRANTED)
                }
            }
        }
    }
}