package com.architect.kmpessentials

import platform.CoreMotion.CMMotionManager
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

object KmpiOS {

    // used for acc, gyro, magnotometer sensors api
    val motionManager = CMMotionManager()

    fun getTopViewController() : UIViewController?{
        return UIApplication.sharedApplication.keyWindow?.rootViewController()
    }
//
//    public fun UIViewController.topViewController(): UIViewController? {
//        when (this) {
//            is UINavigationController -> return visibleViewController?.topViewController()
//            is UITabBarController -> return selectedViewController?.topViewController()
//            else -> {
//                if (presentedViewController == null) {
//                    return this
//                }
//                return presentedViewController?.topViewController()
//            }
//        }
//    }
//
//    public fun UIApplication.topViewController(): UIViewController? =
//        connectedScenes
//            .filter {
//                (it as? UIWindowScene)?.activationState == UISceneActivationStateForegroundActive
//            }.firstOrNull()
//            .let {
//                ((it as? UIWindowScene)?.keyWindow)
//                    ?.rootViewController
//                    ?.topViewController()
//            }
}