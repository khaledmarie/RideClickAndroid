            for (node in nodes) {

                val clickableNode =
                    findClickableParent(node)

                if (clickableNode != null) {

                    // علامة تشخيصية فقط:
                    // نأخذ مركز العنصر الذي سنطلب من Accessibility الضغط عليه
                    // ونرسل موقعه إلى Overlay ليظهر دائرة حمراء لمدة ثانية.
                    val bounds = Rect()
                    clickableNode.getBoundsInScreen(bounds)

                    if (!bounds.isEmpty) {
                        sendClickMarker(
                            x = bounds.centerX(),
                            y = bounds.centerY()
                        )
                    }

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
    // إرسال مكان محاولة الضغط إلى Overlay
    // ==============================================

    private fun sendClickMarker(
        x: Int,
        y: Int
    ) {

        try {

            val intent =
                android.content.Intent(
                    ACTION_CLICK_MARKER
                ).apply {

                    setPackage(packageName)

                    putExtra(
                        EXTRA_CLICK_X,
                        x
                    )

                    putExtra(
                        EXTRA_CLICK_Y,
                        y
                    )
                }

            sendBroadcast(intent)

            Log.d(
                TAG,
                "Click marker sent at x=$x, y=$y"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Unable to send click marker",
                e
            )
        }
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
