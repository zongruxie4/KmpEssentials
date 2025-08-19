package com.architect.kmpessentials.launcher

import com.architect.kmpessentials.aliases.DefaultActionWithBooleanReturn
import com.architect.kmpessentials.mainThread.KmpMainThread
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.MapKit.MKLaunchOptionsDirectionsModeDriving
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.WatchKit.WKApplication
import platform.WatchKit.WKInterfaceDevice

// most apis on watch os will require watch connectivity framework (will require an active iphone connection to work)
@OptIn(ExperimentalForeignApi::class)
actual class KmpLauncher {
    actual companion object {
        actual fun startTimer(seconds: Double, action: DefaultActionWithBooleanReturn) {
            KmpMainThread.runViaMainThread {
                val timer = NSTimer.scheduledTimerWithTimeInterval(seconds, false, { timer ->
                    if (!action()) {
                        timer?.invalidate()
                    }
                });

                NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes);
            }
        }

        actual fun cancelAllTimers() {

        }

        actual fun startTimerRepeating(seconds: Double, action: DefaultActionWithBooleanReturn) {
            KmpMainThread.runViaMainThread {
                val timer = NSTimer.scheduledTimerWithTimeInterval(seconds, true, { timer ->
                    if (!action()) {
                        timer?.invalidate()
                    }
                });

                NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes);
            }
        }

        actual fun startTimerRepeating(
            seconds: Double,
            allowCancel: Boolean,
            action: DefaultActionWithBooleanReturn
        ) {
            KmpMainThread.runViaMainThread {
                val timer = NSTimer.scheduledTimerWithTimeInterval(seconds, true, { timer ->
                    if (!action()) {
                        timer?.invalidate()
                    }
                });

                NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes);
            }
        }


        actual fun startTimerRepeatingWithInitialCallback(
            seconds: Double,
            action: DefaultActionWithBooleanReturn
        ) {
            KmpMainThread.runViaMainThread {
                if (action()) {
                    val timer = NSTimer.scheduledTimerWithTimeInterval(seconds, true, { timer ->
                        if (!action()) {
                            timer?.invalidate()
                        }
                    });

                    NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes);
                }
            }
        }

        actual fun startTimerRepeatingWithInitialCallback(
            seconds: Double,
            allowCancel: Boolean,
            action: DefaultActionWithBooleanReturn
        ) {
            KmpMainThread.runViaMainThread {
                if (action()) {
                    val timer = NSTimer.scheduledTimerWithTimeInterval(seconds, true, { timer ->
                        if (!action()) {
                            timer?.invalidate()
                        }
                    });

                    NSRunLoop.mainRunLoop.addTimer(timer, NSRunLoopCommonModes);
                }
            }
        }


        actual fun launchExternalMapsAppWithAddress(
            address: String,
            markerTitle: String
        ) {
            KmpMainThread.runViaMainThread {
                val geocoder = CLGeocoder()
                geocoder.geocodeAddressString(address) { result, error ->
                    val coordinates = result?.firstOrNull()
                    if (coordinates != null) {
                        (coordinates as CLPlacemark).location?.coordinate?.useContents {
                            launchExternalMapsAppWithCoordinates(latitude, longitude)
                        }
                    }
                }
            }
        }

        actual fun launchExternalMapsAppWithCoordinates(
            latitude: Double,
            longitude: Double,
            markerTitle: String
        ) {
            KmpMainThread.runViaMainThread {
                val placemark =
                    MKPlacemark(coordinate = CLLocationCoordinate2DMake(latitude, longitude))
                val mapItem = MKMapItem(placemark = placemark)
                val launchOptions =
                    mapOf<kotlin.Any?, String>(MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeDriving)
                mapItem.openInMapsWithLaunchOptions(launchOptions)
            }
        }

        actual fun launchExternalUrlViaBrowser(linkPath: String) {
            openExternalLink(linkPath)
        }

        actual fun launchExternalUrlViaAnyApp(linkPath: String) {
            openExternalLink(linkPath)
        }

        private fun openExternalLink(linkPath: String) {
            val link = NSURL.URLWithString(linkPath)!!
            WKApplication.sharedApplication().openSystemURL(link)
        }

        actual fun launchAppInternalSettings() {
//            WKApplication.sharedApplication().openSystemURL(
//                NSURL.URLWithString(
//                    Setting
//                )!!
//            )
        }

        actual fun launchAppStoreViaIdentifier(appStoreLink: String) {
//            KmpMainThread.runViaMainThread {
//                val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController
//                val store = SKStoreProductViewController()
//                store.loadProductWithParameters(
//                    mapOf(SKStoreProductParameterITunesItemIdentifier to appStoreLink),
//                    null
//                )
//                viewController?.presentViewController(store, true, null)
//            }
        }
    }
}