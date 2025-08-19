package com.architect.kmpessentials.extensions

import kotlinx.cinterop.*
import platform.Foundation.*

fun NSData?.utf8String(): String? {
    this ?: return null
    return NSString.create(this, encoding = NSUTF8StringEncoding)?.toString()
}

fun NSData?.utf16String(): String? {
    this ?: return null
    return NSString.create(this, encoding = NSUTF16StringEncoding)?.toString()
}

fun NSString?.asKString(): String? = this?.toString()