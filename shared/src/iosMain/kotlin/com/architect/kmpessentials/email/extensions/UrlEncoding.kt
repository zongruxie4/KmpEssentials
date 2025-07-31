package com.architect.kmpessentials.email.extensions

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFURLCreateStringByAddingPercentEscapes
import platform.CoreFoundation.kCFStringEncodingUTF8

@OptIn(ExperimentalForeignApi::class)
fun String.encodeURL(): String = CFURLCreateStringByAddingPercentEscapes(
    null,
    this as CFStringRef,
    null,
    "!*'();:@&=+$,/?%#[]" as CFStringRef,
    kCFStringEncodingUTF8
).toString()