//package com.architect.kmpessentials.share.providers
//import kotlinx.cinterop.ObjCSignatureOverride
//import objcnames.classes.LPLinkMetadata
//import platform.Foundation.*
//import platform.UIKit.*
//import platform.darwin.*
//
//class ShareFileProvider(
//    private val fileUrl: NSURL,
//    private val title: String,
//    private val mimeType: String
//) : NSObject(), UIActivityItemSourceProtocol {
//
//    override fun activityViewControllerPlaceholderItem(activityViewController: UIActivityViewController): Any? {
//        return fileUrl
//    }
//
////    @ObjCSignatureOverride
////    override fun activityViewController(
////        activityViewController: UIActivityViewController,
////        activityType: String?
////    ): Any? {
////        return fileUrl
////    }
////
////    @ObjCSignatureOverride
////    override fun activityViewController(
////        activityViewController: UIActivityViewController,
////        dataTypeIdentifierForActivityType: String?
////    ): String {
////        return mimeType
////    }
//}