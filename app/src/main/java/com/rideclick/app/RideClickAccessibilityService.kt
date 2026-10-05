package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RideClick"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "RideClick Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        val root = rootInActiveWindow ?: return

        // حالياً نقرأ الشاشة فقط
        // لاحقاً سنربطها بفلاتر السعر والوقت
        inspectScreen(root)
    }

    private fun inspectScreen(root: AccessibilityNodeInfo) {

        // البحث عن زر قبول العرض في Jeeny
        val acceptNodes =
            root.findAccessibilityNodeInfosByText("قبول العرض")

        if (!acceptNodes.isNullOrEmpty()) {

            Log.d(TAG, "Jeeny offer detected")

            /*
             * مهم:
             * في هذه المرحلة لا نضغط تلقائياً.
             *
             * أولاً سنقرأ:
             * 1. السعر
             * 2. الوقت
             * 3. نتأكد أن التطبيق Jeeny
             * 4. نطبق الفلاتر
             *
             * وبعدها فقط نستدعي:
             *
             * clickAcceptOffer(root)
             */
        }
    }

    /*
     * الضغط على زر قبول العرض
     *
     * نبحث عن النص "قبول العرض".
     * إذا كان العنصر نفسه Clickable نضغطه.
     *
     * إذا لم يكن Clickable نصعد إلى Parent
     * حتى نجد العنصر القابل للنقر.
     */
    private fun clickAcceptOffer(
        root: AccessibilityNodeInfo
    ): Boolean {

        val nodes =
            root.findAccessibilityNodeInfosByText("قبول العرض")

        if (nodes.isNullOrEmpty()) {
            Log.d(TAG, "Accept button not found")
            return false
        }

        for (node in nodes) {

            var current: AccessibilityNodeInfo? = node

            while (current != null) {

                if (current.isClickable) {

                    val clicked =
                        current.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK
                        )

                    Log.d(
                        TAG,
                        "Accept button click result: $clicked"
                    )

                    return clicked
                }

                current = current.parent
            }
        }

        Log.d(TAG, "Clickable parent not found")

        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "RideClick Accessibility interrupted")
    }
}
