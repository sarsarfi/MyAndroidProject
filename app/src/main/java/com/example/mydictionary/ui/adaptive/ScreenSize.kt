package com.example.mydictionary.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

sealed class DeviceType {
    object Phone : DeviceType()      // موبایل
    object Foldable : DeviceType()   // گوشی تاشو
    object Tablet : DeviceType()     // تبلت
}

@Composable
fun rememberDeviceType(): DeviceType {
    // قدم 1: عرض صفحه را بر حسب dp حساب کن
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // قدم 2: تصمیم بگیر بر اساس عرض صفحه
    return when {
        screenWidthDp < 600 -> DeviceType.Phone
        screenWidthDp < 840 -> DeviceType.Foldable
        else -> DeviceType.Tablet
    }
}