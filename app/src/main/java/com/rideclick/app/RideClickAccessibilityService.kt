package com.rideclick.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
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

        // لمنع تكرار معالجة نفس الطلب بسرعة
        private const val OFFER_COOLDOWN = 1500L
    }

    private var lastOfferKey = ""
    private var lastOfferTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(
            TAG,
            "RideClick Accessibility started"
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        val root =
            rootInActiveWindow ?: return

        val texts =
            mutableListOf<String>()

        collectTexts(
            root,
            texts
        )

        if (texts.isEmpty()) {
            return
        }

        val fullText =
            texts.joinToString(" | ")

        Log.d(
            TAG,
            "SCREEN = $fullText"
        )

        // =========================
        // هل يوجد طلب Jeeny؟
        // =========================

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

        // =========================
        // استخراج السعر والوقت
        // =========================

        val price =
            extractPrice(texts)

        val minutes =
            extractMinutes(texts)

        Log.d(
            TAG,
            "PRICE=$price | MINUTES=$minutes"
        )

        if (
            price == null ||
            minutes == null
        ) {

            Log.d(
                TAG,
                "Offer detected but data incomplete"
            )

            return
        }

        // =========================
        // منع التكرار السريع
        // =========================

        val offerKey =
            "$price-$minutes"

        val now =
            System.currentTimeMillis()

        if (
            offerKey == lastOfferKey &&
            now - lastOfferTime < OFFER_COOLDOWN
        ) {

            return
        }

        lastOfferKey = offerKey
        lastOfferTime = now

        // =========================
        // مقارنة الشروط
        // =========================

        val matched =
            price >= MIN_PRICE &&
            minutes <= MAX_MINUTES

        Log.d(
            TAG,
            if (matched) {
                "✅ OFFER MATCHED"
            } else {
                "❌ OFFER NOT MATCHED"
            }
        )

        // إرسال النتيجة للـ Overlay
        sendOfferToOverlay(
            price,
            minutes,
            matched
        )

        // =========================
        // البحث عن شريط قبول العرض
        // =========================

        val acceptNode =
            findAcceptNode(root)

        if (acceptNode != null) {

            val bounds =
                Rect()

            acceptNode.getBoundsInScreen(
                bounds
            )

            Log.d(
                TAG,
                "✅ ACCEPT CONTROL FOUND"
            )

            Log.d(
                TAG,
                "Accept text = ${acceptNode.text}"
            )

            Log.d(
                TAG,
                "Accept description = ${acceptNode.contentDescription}"
            )

            Log.d(
                TAG,
                "Accept class = ${acceptNode.className}"
            )

            Log.d(
                TAG,
                "Accept bounds = $bounds"
            )

            Log.d(
                TAG,
                "Clickable = ${acceptNode.isClickable}"
            )

            Log.d(
                TAG,
                "Scrollable = ${acceptNode.isScrollable}"
            )

            Log.d(
                TAG,
                "Enabled = ${acceptNode.isEnabled}"
            )

            // مهم جدًا:
            // لا يوجد ضغط ولا سحب هنا.
            //
            // هذه المرحلة فقط للتأكد
            // أن RideClick يستطيع العثور
            // على شريط "قبول العرض".

        } else {

            Log.d(
                TAG,
                "⚠️ ACCEPT CONTROL NOT FOUND"
            )
        }
    }

    // =============================
    // إرسال الطلب إلى Overlay
    // =============================

    private fun sendOfferToOverlay(
        price: Double,
        minutes: Int,
        matched: Boolean
    ) {

        val intent =
            Intent(
                ACTION_OFFER_UPDATE
            ).apply {

                setPackage(
                    packageName
                )

                putExtra(
                    EXTRA_PRICE,
                    price
                )

                putExtra(
                    EXTRA_MINUTES,
                    minutes
                )

                putExtra(
                    EXTRA_MATCHED,
                    matched
                )
            }

        sendBroadcast(intent)
    }

    // =============================
    // البحث عن "قبول العرض"
    // =============================

    private fun findAcceptNode(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? {

        if (node == null) {
            return null
        }

        val text =
            node.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val description =
            node.contentDescription
                ?.toString()
                ?.trim()
                .orEmpty()

        if (
            text.contains(
                "قبول العرض",
                ignoreCase = true
            ) ||
            description.contains(
                "قبول العرض",
                ignoreCase = true
            )
        ) {

            return node
        }

        for (
            i in 0 until node.childCount
        ) {

            val result =
                findAcceptNode(
                    node.getChild(i)
                )

            if (result != null) {
                return result
            }
        }

        return null
    }

    // =============================
    // قراءة النصوص
    // =============================

    private fun collectTexts(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>
    ) {

        if (node == null) {
            return
        }

        val text =
            node.text
                ?.toString()
                ?.trim()

        if (
            !text.isNullOrEmpty()
        ) {

            result.add(text)
        }

        val description =
            node.contentDescription
                ?.toString()
                ?.trim()

        if (
            !description.isNullOrEmpty() &&
            !result.contains(
                description
            )
        ) {

            result.add(
                description
            )
        }

        for (
            i in 0 until node.childCount
        ) {

            collectTexts(
                node.getChild(i),
                result
            )
        }
    }

    // =============================
    // استخراج السعر
    // =============================

    private fun extractPrice(
        texts: List<String>
    ): Double? {

        val regexes =
            listOf(

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

            for (
                regex in regexes
            ) {

                val match =
                    regex.find(text)
                        ?: continue

                val value =
                    match
                        .groupValues[1]
                        .replace(
                            ",",
                            "."
                        )

                val price =
                    value.toDoubleOrNull()

                if (price != null) {
                    return price
                }
            }
        }

        return null
    }

    // =============================
    // استخراج الوقت
    // =============================

    private fun extractMinutes(
        texts: List<String>
    ): Int? {

        val regexes =
            listOf(

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

            for (
                regex in regexes
            ) {

                val match =
                    regex.find(text)
                        ?: continue

                val minutes =
                    match
                        .groupValues[1]
                        .toIntOrNull()

                if (
                    minutes != null
                ) {

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
