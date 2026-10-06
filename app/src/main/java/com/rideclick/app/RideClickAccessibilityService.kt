package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RideClick"

        const val ACTION_OFFER_UPDATE =
            "com.rideclick.app.ACTION_OFFER_UPDATE"

        const val EXTRA_PLATFORM = "platform"
        const val EXTRA_PRICE = "price"
        const val EXTRA_MINUTES = "minutes"
        const val EXTRA_DISTANCE = "distance"
        const val EXTRA_MATCHED = "matched"
        const val EXTRA_ACCEPT_FOUND = "accept_found"
        const val EXTRA_MIN_PRICE = "min_price"
        const val EXTRA_MAX_MINUTES = "max_minutes"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(
            TAG,
            "RideClick Accessibility Service connected"
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        if (event == null) return

        val root = rootInActiveWindow ?: return

        try {
            inspectScreen(root)
        } catch (exception: RuntimeException) {
            Log.e(
                TAG,
                "Unable to inspect screen",
                exception
            )
        }
    }

    private fun inspectScreen(
        root: AccessibilityNodeInfo
    ) {
        val acceptNodes =
            root.findAccessibilityNodeInfosByText(
                "قبول العرض"
            )

        if (!acceptNodes.isNullOrEmpty()) {
            Log.d(
                TAG,
                "Offer text detected in package: " +
                    root.packageName
            )
        }

        // هذه النسخة لا تضغط على زر القبول.
        // قراءة السعر والوقت وتطبيق الفلاتر
        // تحتاج ربط ملفات المرحلة الثانية.
    }

    override fun onInterrupt() {
        Log.d(
            TAG,
            "RideClick Accessibility interrupted"
        )
    }
}
