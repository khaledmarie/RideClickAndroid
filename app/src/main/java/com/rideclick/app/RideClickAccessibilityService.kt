package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RideClick"

        // شروط RideClick الحالية
        private const val MIN_PRICE = 4.00
        private const val MAX_MINUTES = 5
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(TAG, "RideClick Accessibility started")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        val root = rootInActiveWindow ?: return

        // قراءة جميع النصوص الموجودة على الشاشة
        val screenTexts = mutableListOf<String>()

        collectTexts(root, screenTexts)

        if (screenTexts.isEmpty()) return

        val fullText = screenTexts.joinToString(" | ")

        Log.d(TAG, "SCREEN = $fullText")

        // هل ظهر طلب جيني؟
        val offerDetected =
            fullText.contains(
                "قبول العرض",
                ignoreCase = true
            ) ||
            fullText.contains(
                "JOD",
                ignoreCase = true
            )

        if (!offerDetected) {
            return
        }

        Log.d(TAG, "New Jeeny offer detected")

        // استخراج السعر
        val price = extractPrice(screenTexts)

        // استخراج وقت الوصول
        val minutes = extractMinutes(screenTexts)

        Log.d(
            TAG,
            "PRICE = $price | MINUTES = $minutes"
        )

        // لا نحكم على الطلب إلا إذا قرأنا القيمتين
        if (price == null || minutes == null) {

            Log.d(
                TAG,
                "Offer detected but data incomplete"
            )

            return
        }

        // مقارنة الطلب بالشروط
        val priceAccepted =
            price >= MIN_PRICE

        val timeAccepted =
            minutes <= MAX_MINUTES

        val accepted =
            priceAccepted && timeAccepted

        if (accepted) {

            Log.d(
                TAG,
                "✅ MATCHED OFFER | " +
                    "Price=$price | " +
                    "Minutes=$minutes"
            )

        } else {

            Log.d(
                TAG,
                "❌ REJECTED OFFER | " +
                    "Price=$price | " +
                    "Minutes=$minutes"
            )
        }
    }

    /**
     * قراءة جميع النصوص الموجودة داخل شجرة Accessibility
     */
    private fun collectTexts(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>
    ) {

        if (node == null) return

        try {

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
                !result.contains(description)
            ) {

                result.add(description)
            }

            for (i in 0 until node.childCount) {

                collectTexts(
                    node.getChild(i),
                    result
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error reading accessibility nodes",
                e
            )
        }
    }

    /**
     * محاولة استخراج السعر.
     *
     * أمثلة:
     * JOD 1.50
     * 1.50 JOD
     * JOD 4.25
     */
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

                val price =
                    value.toDoubleOrNull()

                if (price != null) {

                    return price
                }
            }
        }

        return null
    }

    /**
     * استخراج عدد دقائق الوصول.
     *
     * أمثلة:
     * يبعد 1 دقائق
     * يبعد 3 دقائق
     * 5 دقائق
     */
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

                val minutes =
                    match.groupValues[1]
                        .toIntOrNull()

                if (minutes != null) {

                    return minutes
                }
            }
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
