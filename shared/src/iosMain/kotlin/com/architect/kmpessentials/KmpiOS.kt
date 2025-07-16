package com.architect.kmpessentials

import platform.CoreMotion.CMMotionManager
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

object KmpiOS {

    // used for acc, gyro, magnotometer sensors api
    val motionManager = CMMotionManager()

    fun getTopViewController(): UIViewController? {
        return UIApplication.sharedApplication.keyWindow?.rootViewController()
    }

    fun presentByDismissingViewModal(modalController: UIViewController) {
        val topVC = getTopViewController()
        val presented = topVC?.presentedViewController
        if (presented != null) {
            presented.dismissViewControllerAnimated(false) {
                topVC.presentViewController(modalController, true, null)
            }
        } else {
            topVC?.presentViewController(modalController, true, null)
        }
    }
}