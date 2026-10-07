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

        // نفس الأسماء الموجودة في MainActivity
        private const val PREFS_NAME = "rideclick_prefs"
        private const val KEY_MIN_PRICE = "min_price"
        private const val KEY_MAX_MINUTES = "max_minutes"

        private const val DEFAULT_MIN_PRICE = 4.0f
        private const val DEFAULT_MAX_MINUTES = 5

        // حماية من تكرار الضغط
        private const val CLICK_COOLDOWN_MS = 3000L
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

        // ==========================================
        // 1. قراءة كل النصوص الظاهرة على الشاشة
        // ==========================================

        val texts = mutableListOf<String>()

        collectTexts(
            root,
            texts
        )

        if (texts.isEmpty()) {
            return
        }

        Log.d(
            TAG,
            "Package = ${root.packageName}"
        )

        Log.d(
            TAG,
            "Screen texts = $texts"
        )

        // ==========================================
        // 2. استخراج السعر
        // ==========================================

        val price =
            extractPrice(texts)

        if (price == null) {

            Log.d(
                TAG,
                "Price not detected"
            )

            return
        }

        // ==========================================
        // 3. استخراج وقت الوصول إلى الراكب
        // وليس عداد قبول الطلب
        // ==========================================

        val minutes =
            extractArrivalMinutes(texts)

        if (minutes == null) {

            Log.d(
                TAG,
                "Arrival minutes not detected"
            )

            return
        }

        // ==========================================
        // 4. قراءة الفلاتر من MainActivity
        // ==========================================

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val minPrice =
            prefs.getFloat(
                KEY_MIN_PRICE,
                DEFAULT_MIN_PRICE
            ).toDouble()

        val maxMinutes =
            prefs.getInt(
                KEY_MAX_MINUTES,
                DEFAULT_MAX_MINUTES
            )

        // ==========================================
        // 5. مقارنة الطلب بالشروط
        // ==========================================

        val matched =
            price >= minPrice &&
            minutes <= maxMinutes

        Log.d(
            TAG,
            "Offer -> " +
                "Price=$price, " +
                "ArrivalMinutes=$minutes, " +
                "MinPrice=$minPrice, " +
                "MaxMinutes=$maxMinutes, " +
                "Matched=$matched"
        )

        // ==========================================
        // 6. إرسال المعلومات للفقاعة
        // ==========================================

        sendOfferUpdate(
            root = root,
            price = price,
            minutes = minutes,
            matched = matched,
            minPrice = minPrice,
            maxMinutes = maxMinutes
        )

        // ==========================================
        // 7. إذا الطلب مطابق نحاول الضغط على القبول
        // ==========================================

        if (matched) {

            Log.d(
                TAG,
                "Offer matches filters"
            )

            findAndClickAccept(root)

        } else {

            Log.d(
                TAG,
                "Offer rejected by filters"
            )
        }
    }

    // ==============================================
    // جمع النصوص من Accessibility Tree
    // ==============================================

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

    // ==============================================
    // استخراج السعر
    // أمثلة:
    // 6 JOD
    // 6.00 JOD
    // JOD 6.00
    // 6 د.أ
    // 6 دينار
    // ==============================================

    private fun extractPrice(
        texts: List<String>
    ): Double? {

        val patterns =
            listOf(

                Regex(
                    """(\d+(?:[.,]\d+)?)\s*(?:JOD|د\.?\s*أ|دينار)""",
                    RegexOption.IGNORE_CASE
                ),

                Regex(
                    """(?:JOD|د\.?\s*أ|دينار)\s*(\d+(?:[.,]\d+)?)""",
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
                        "Price detected = $value"
                    )

                    return value
                }
            }
        }

        return null
    }

    // ==============================================
    // استخراج وقت الوصول إلى الراكب
    //
    // نحاول أولاً البحث عن النصوص التي تحتوي
    // على معنى الوصول / الالتقاط / pickup.
    //
    // ثم نستخدم قراءة الدقائق كخيار احتياطي.
    // ==============================================

    private fun extractArrivalMinutes(
        texts: List<String>
    ): Int? {

        val minutePattern =
            Regex(
                """(\d+)\s*(?:دقيقة|دقائق|min|mins|minute|minutes)""",
                RegexOption.IGNORE_CASE
            )

        // كلمات تساعدنا على تمييز وقت الوصول
        // عن عداد قبول الطلب
        val arrivalKeywords =
            listOf(
                "وصول",
                "الوصول",
                "راكب",
                "الراكب",
                "التقاط",
                "pickup",
                "pick up",
                "away",
                "arrival"
            )

        // ==========================================
        // المحاولة الأولى:
        // وقت مرتبط بكلمة تدل على الوصول
        // ==========================================

        for (text in texts) {

            val lower =
                text.lowercase()

            val looksLikeArrival =
                arrivalKeywords.any {
                    lower.contains(
                        it.lowercase()
                    )
                }

            if (!looksLikeArrival) {
                continue
            }

            val match =
                minutePattern.find(text)
                    ?: continue

            val value =
                match.groupValues[1]
                    .toIntOrNull()

            if (value != null) {

                Log.d(
                    TAG,
                    "Arrival time detected = $value minutes"
                )

                return value
            }
        }

        // ==========================================
        // المحاولة الثانية:
        // البحث عن قيمة دقائق عامة
        //
        // هذه احتياطية فقط لأن شكل نص جيني
        // الفعلي قد يختلف.
        // ==========================================

        for (text in texts) {

            // نتجنب النصوص التي تبدو كعداد قبول
            val lower =
                text.lowercase()

            if (
                lower.contains("قبول") ||
                lower.contains("accept") ||
                lower.contains("timer")
            ) {
                continue
            }

            val match =
                minutePattern.find(text)
                    ?: continue

            val value =
                match.groupValues[1]
                    .toIntOrNull()

            if (value != null) {

                Log.d(
                    TAG,
                    "Possible arrival time detected = $value minutes"
                )

                return value
            }
        }

        return null
    }

    // ==============================================
    // إرسال البيانات إلى Overlay
    // ==============================================

    private fun sendOfferUpdate(
        root: AccessibilityNodeInfo,
        price: Double,
        minutes: Int,
        matched: Boolean,
        minPrice: Double,
        maxMinutes: Int
    ) {

        try {

            val acceptFound =
                hasAcceptNode(root)

            val intent =
                android.content.Intent(
                    ACTION_OFFER_UPDATE
                ).apply {

                    setPackage(packageName)

                    putExtra(
                        EXTRA_PLATFORM,
                        detectPlatform(root)
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
                        EXTRA_DISTANCE,
                        0.0
                    )

                    putExtra(
                        EXTRA_MATCHED,
                        matched
                    )

                    putExtra(
                        EXTRA_ACCEPT_FOUND,
                        acceptFound
                    )

                    putExtra(
                        EXTRA_MIN_PRICE,
                        minPrice
                    )

                    putExtra(
                        EXTRA_MAX_MINUTES,
                        maxMinutes
                    )
                }

            sendBroadcast(intent)

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Unable to send offer update",
                e
            )
        }
    }

    // ==============================================
    // تحديد التطبيق
    // ==============================================

    private fun detectPlatform(
        root: AccessibilityNodeInfo
    ): String {

        val packageNameText =
            root.packageName
                ?.toString()
                ?.lowercase()
                ?: return "Unknown"

        return when {

            packageNameText.contains(
                "jeeny"
            ) -> "Jeeny"

            packageNameText.contains(
                "petra"
            ) -> "Petra Ride"

            else -> packageNameText
        }
    }

    // ==============================================
    // هل يوجد عنصر قبول؟
    // ==============================================

    private fun hasAcceptNode(
        root: AccessibilityNodeInfo
    ): Boolean {

        val possibleTexts =
            listOf(
                "قبول العرض",
                "قبول",
                "Accept"
            )

        for (text in possibleTexts) {

            val nodes =
                root.findAccessibilityNodeInfosByText(
                    text
                )

            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }

        return false
    }

    // ==============================================
    // البحث عن القبول والضغط عليه
    // ==============================================

    private fun findAndClickAccept(
        root: AccessibilityNodeInfo
    ) {

        val now =
            System.currentTimeMillis()

        // حماية من تكرار الضغط
        if (
            now - lastClickTime <
            CLICK_COOLDOWN_MS
        ) {

            Log.d(
                TAG,
                "Click ignored because of cooldown"
            )

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

                val clickableNode =
                    findClickableParent(node)

                if (clickableNode != null) {

                    val success =
                        clickableNode.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK
                        )

                    if (success) {

                        lastClickTime = now

                        Log.d(
                            TAG,
                            "Jeeny accept click SUCCESS"
                        )

                        return
                    }
                }
            }
        }

        Log.d(
            TAG,
            "Matching offer detected, " +
                "but clickable accept node was not found"
        )
    }

    // ==============================================
    // أحياناً النص نفسه ليس Clickable
    // لذلك نصعد إلى الأب حتى نجد العنصر القابل للنقر
    // ==============================================

    private fun findClickableParent(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? {

        var current =
            node

        while (current != null) {

            if (current.isClickable) {
                return current
            }

            current =
                current.parent
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
