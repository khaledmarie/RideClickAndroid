package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RideClick"

        private const val MIN_PRICE = 4.00
        private const val MAX_MINUTES = 5

        const val ACTION_OFFER_UPDATE =
            "com.rideclick.app.OFFER_UPDATE"

        const val EXTRA_PRICE = "price"
        const val EXTRA_MINUTES = "minutes"
        const val EXTRA_MATCHED = "matched"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility started")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val root = rootInActiveWindow ?: return

        val texts = mutableListOf<String>()

        collectTexts(root, texts)

        if (texts.isEmpty()) return

        val fullText = texts.joinToString(" | ")

        Log.d(TAG, "SCREEN = $fullText")

        // التأكد أن هناك عرضًا ظاهرًا
        val offerDetected =
            fullText.contains("قبول العرض", true) ||
            fullText.contains("JOD", true)

        if (!offerDetected) return

        val price = extractPrice(texts)
        val minutes = extractMinutes(texts)

        Log.d(
            TAG,
            "PRICE=$price MINUTES=$minutes"
        )

        if (price == null || minutes == null) {
            return
        }

        val matched =
            price >= MIN_PRICE &&
            minutes <= MAX_MINUTES

        sendOfferToOverlay(
            price,
            minutes,
            matched
        )
    }

    private fun sendOfferToOverlay(
        price: Double,
        minutes: Int,
        matched: Boolean
    ) {

        val intent =
            Intent(ACTION_OFFER_UPDATE).apply {

                setPackage(packageName)

                putExtra(EXTRA_PRICE, price)
                putExtra(EXTRA_MINUTES, minutes)
                putExtra(EXTRA_MATCHED, matched)
            }

        sendBroadcast(intent)

        Log.d(
            TAG,
            "Offer sent to overlay: $price / $minutes / $matched"
        )
    }

    private fun collectTexts(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>
    ) {

        if (node == null) return

        node.text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { result.add(it) }

        node.contentDescription
            ?.toString()
            ?.trim()
            ?.takeIf {
                it.isNotEmpty() &&
                !result.contains(it)
            }
            ?.let { result.add(it) }

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

        val regexes = listOf(

            Regex(
                """JOD\s*([0-9]+(?:[.,][0-9]+)?)""",
                RegexOption.IGNORE_CASE
            ),

            Regex(
                """([0-9]+(?:[.,][0-9]+)?)\s*JOD""",
                RegexOption.IGNORE_CASE
            )
        )

        for (text in texts) {

            for (regex in regexes) {

                val match =
                    regex.find(text)
                        ?: continue

                val value =
                    match.groupValues[1]
                        .replace(",", ".")

                value.toDoubleOrNull()
                    ?.let { return it }
            }
        }

        return null
    }

    private fun extractMinutes(
        texts: List<String>
    ): Int? {

        val regexes = listOf(

            Regex(
                """يبعد\s*([0-9]+)\s*دقائق?"""
            ),

            Regex(
                """([0-9]+)\s*دقائق?"""
            ),

            Regex(
                """([0-9]+)\s*دقيقة"""
            )
        )

        for (text in texts) {

            for (regex in regexes) {

                val match =
                    regex.find(text)
                        ?: continue

                match.groupValues[1]
                    .toIntOrNull()
                    ?.let { return it }
            }
        }

        return null
    }

    override fun onInterrupt() {
        Log.d(
            TAG,
            "Accessibility interrupted"
        )
    }
}
