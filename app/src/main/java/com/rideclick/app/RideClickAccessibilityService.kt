package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideClickAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val text = StringBuilder()
        collectText(root, text)

        // Monitor-only phase: parse and evaluate, but NEVER click/accept.
        val platform = detectPlatform(event)
        val request = platform?.let { TextRequestParser.parse(text.toString(), it) }
        if (request != null) {
            val evaluation = RuleEngine.evaluate(
                request,
                Rules(),
                setOf(Platform.JEENY, Platform.PETRA_RIDE)
            )
            // TODO: expose evaluation to the in-app Live Monitor.
            // Intentionally no performAction()/gesture dispatch/auto-accept here.
            evaluation.accepted
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, out: StringBuilder) {
        node.text?.let { out.append(it).append(' ') }
        node.contentDescription?.let { out.append(it).append(' ') }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                collectText(child, out)
                child.recycle()
            }
        }
    }

    private fun detectPlatform(event: AccessibilityEvent?): Platform? {
        val pkg = event?.packageName?.toString() ?: return null
        return when {
            pkg.contains("jeeny", ignoreCase = true) -> Platform.JEENY
            pkg.contains("petra", ignoreCase = true) -> Platform.PETRA_RIDE
            else -> null
        }
    }

    override fun onInterrupt() = Unit
}
