package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class RideClickAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // سيتم إضافة منطق قراءة طلبات Jeeny / Petra Ride هنا لاحقًا
    }

    override fun onInterrupt() {
        // إيقاف خدمة Accessibility عند الحاجة
    }
}
