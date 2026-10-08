            textView.text =
                """
                📥 آخر طلب Jeeny
                💰 JOD %.2f
                ⏱️ %d دقيقة
                ✅ مطابق للشروط
                $acceptStatus
                """.trimIndent()
                    .format(
                        price,
                        minutes
                    )

            textView.setTextColor(
                Color.GREEN
            )

        } else {

            textView.text =
                """
                📥 آخر طلب Jeeny
                💰 JOD %.2f
                ⏱️ %d دقيقة
                ❌ غير مطابق للشروط
                $acceptStatus
                """.trimIndent()
                    .format(
                        price,
                        minutes
                    )

            textView.setTextColor(
                Color.rgb(
                    255,
                    100,
                    100
                )
            )
        }
    }

    // =============================
    // تحديث عرض الفلاتر
    // =============================

    private fun updateFiltersText() {

        val textView =
            filtersTextView ?: return

        textView.text =
            """
            ⚙️ الفلاتر الحالية:
            💰 السعر ≥ %.2f د.أ
            ⏱️ الوقت ≤ %d دقائق
            """.trimIndent()
                .format(
                    currentMinPrice,
                    currentMaxMinutes
                )
    }

    // =============================
    // إغلاق الخدمة
    // =============================

    override fun onDestroy() {

        try {

            unregisterReceiver(
                offerReceiver
            )

        } catch (_: Exception) {
        }

        clickMarkerView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        mainHandler.removeCallbacksAndMessages(null)
        clickMarkerView = null

        overlayView?.let {

            try {

                windowManager.removeView(
                    it
                )

            } catch (_: Exception) {
            }
        }

        overlayView = null
        offerTextView = null
        filtersTextView = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
