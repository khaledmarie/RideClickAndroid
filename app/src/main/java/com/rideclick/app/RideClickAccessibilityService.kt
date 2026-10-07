package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
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

        private const val PREFS_NAME = "rideclick_settings"
        private const val KEY_MIN_PRICE = "min_price"
        private const val KEY_MAX_DISTANCE = "max_distance"
    }

    private var lastClickTime = 0L

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
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Unable to inspect screen",
                e
            )
        }
    }

    private fun inspectScreen(
        root: AccessibilityNodeInfo
    ) {

        val texts = mutableListOf<String>()

        collectTexts(
            root,
            texts
        )

        if (texts.isEmpty()) return

        Log.d(
            TAG,
            "Screen texts: $texts"
        )

        val price =
            extractPrice(texts)

        val distance =
            extractDistance(texts)

        if (price == null || distance == null) {
            return
        }

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val minPrice =
            prefs.getFloat(
                KEY_MIN_PRICE,
                4.0f
            ).toDouble()

        val maxDistance =
            prefs.getFloat(
                KEY_MAX_DISTANCE,
                5.0f
            ).toDouble()

        val matched =
            price >= minPrice &&
            distance <= maxDistance

        Log.d(
            TAG,
            "Price=$price " +
                "Distance=$distance " +
                "MinPrice=$minPrice " +
                "MaxDistance=$maxDistance " +
                "Matched=$matched"
        )

        if (matched) {
            findAndClickAccept(root)
        }
    }

    private fun collectTexts(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>
    ) {
        if (node == null) return

        val text =
            node.text
                ?.toString()
                ?.trim()

        if (!text.isNullOrEmpty()) {
            result.add(text)
        }

        val description =
            node.contentDescription
                ?.toString()
                ?.trim()

        if (
            !description.isNullOrEmpty() &&
            description != text
        ) {
            result.add(description)
        }

        for (i in 0 until node.childCount) {
            collectTexts(
                node.getChild(i),
                result
            )
        }
    }

    private fun extractPrice(
        texts: List<String>
    ): Double? {

        val patterns =
            listOf(
                Regex(
                    """(\d+(?:[.,]\d+)?)\s*(?:د\.?\s*أ|دينار|JOD)"""
                ),
                Regex(
                    """(?:JOD)\s*(\d+(?:[.,]\d+)?)""",
                    RegexOption.IGNORE_CASE
                )
            )

        for (text in texts) {

            for (pattern in patterns) {

                val match =
                    pattern.find(text)
                        ?: continue

                val value =
                    match.groupValues[1]
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (value != null) {

                    Log.d(
                        TAG,
                        "Price detected: $value"
                    )

                    return value
                }
            }
        }

        return null
    }

    private fun extractDistance(
        texts: List<String>
    ): Double? {

        val pattern =
            Regex(
                """(\d+(?:[.,]\d+)?)\s*(?:كم|km)""",
                RegexOption.IGNORE_CASE
            )

        for (text in texts) {

            val match =
                pattern.find(text)
                    ?: continue

            val value =
                match.groupValues[1]
                    .replace(",", ".")
                    .toDoubleOrNull()

            if (value != null) {

                Log.d(
                    TAG,
                    "Distance detected: $value"
                )

                return value
            }
        }

        return null
    }

    private fun findAndClickAccept(
        root: AccessibilityNodeInfo
    ) {

        // منع تكرار الضغط بسبب تكرار Accessibility Events
        val now =
            System.currentTimeMillis()

        if (now - lastClickTime < 3000) {
            return
        }

        val possibleTexts =
            listOf(
                "قبول العرض",
                "قبول",
                "Accept"
            )

        for (acceptText in possibleTexts) {

            val nodes =
                root.findAccessibilityNodeInfosByText(
                    acceptText
                )

            if (nodes.isNullOrEmpty()) {
                continue
            }

            for (node in nodes) {

                val clickable =
                    findClickableParent(node)

                if (clickable != null) {

                    val success =
                        clickable.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK
                        )

                    if (success) {

                        lastClickTime = now

                        Log.d(
                            TAG,
                            "Offer accepted automatically"
                        )

                        return
                    }
                }
            }
        }

        Log.d(
            TAG,
            "Matching offer found, but accept node not clickable"
        )
    }

    private fun findClickableParent(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? {

        var current = node

        while (current != null) {

            if (current.isClickable) {
                return current
            }

            current = current.parent
        }

        return null
    }

    override fun onInterrupt() {

        Log.d(
            TAG,
            "RideClick Accessibility interrupted"
        )
    }
}
