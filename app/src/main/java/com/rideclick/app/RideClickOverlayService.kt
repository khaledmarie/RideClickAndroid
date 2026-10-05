package com.rideclick.app

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class RideClickOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    // موقع الفقاعة
    private var bubbleX = 20
    private var bubbleY = 120

    // آخر طلب تمت قراءته
    private var lastPrice: Double? = null
    private var lastMinutes: Int? = null
    private var lastMatched: Boolean? = null

    // مكان عرض نتيجة الطلب داخل اللوحة
    private var offerTextView: TextView? = null

    // استقبال بيانات الطلب من Accessibility Service
    private val offerReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {

            if (
                intent?.action !=
                RideClickAccessibilityService.ACTION_OFFER_UPDATE
            ) {
                return
            }

            val price =
                intent.getDoubleExtra(
                    RideClickAccessibilityService.EXTRA_PRICE,
                    0.0
                )

            val minutes =
                intent.getIntExtra(
                    RideClickAccessibilityService.EXTRA_MINUTES,
                    0
                )

            val matched =
                intent.getBooleanExtra(
                    RideClickAccessibilityService.EXTRA_MATCHED,
                    false
                )

            // حفظ آخر طلب
            lastPrice = price
            lastMinutes = minutes
            lastMatched = matched

            // تحديث اللوحة إذا كانت مفتوحة
            updateOfferText()
        }
    }

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        // تسجيل Receiver لاستقبال نتائج القراءة
        val filter =
            IntentFilter(
                RideClickAccessibilityService.ACTION_OFFER_UPDATE
            )

        registerReceiver(
            offerReceiver,
            filter,
            RECEIVER_NOT_EXPORTED
        )

        showBubble()
    }

    private fun createLayoutParams():
        WindowManager.LayoutParams {

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {

            gravity =
                Gravity.TOP or Gravity.END

            x = bubbleX
            y = bubbleY
        }
    }

    private fun showBubble() {

        // إزالة الواجهة القديمة
        overlayView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        overlayView = null
        offerTextView = null

        // صورة التكسي الأصفر
        val bubble =
            ImageView(this).apply {

                setImageResource(
                    R.drawable.taxi_icon
                )

                scaleType =
                    ImageView.ScaleType.FIT_CENTER

                contentDescription =
                    "RideClick"
            }

        val params =
            createLayoutParams()

        // حجم الفقاعة 65dp
        val bubbleSize =
            (
                65 *
                    resources.displayMetrics.density
                ).toInt()

        params.width = bubbleSize
        params.height = bubbleSize

        // متغيرات السحب
        var initialX = 0
        var initialY = 0

        var initialTouchX = 0f
        var initialTouchY = 0f

        var isDragging = false

        bubble.setOnTouchListener { view, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    initialX = params.x
                    initialY = params.y

                    initialTouchX =
                        event.rawX

                    initialTouchY =
                        event.rawY

                    isDragging = false

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx =
                        event.rawX -
                            initialTouchX

                    val dy =
                        event.rawY -
                            initialTouchY

                    if (
                        abs(dx) > 10 ||
                        abs(dy) > 10
                    ) {
                        isDragging = true
                    }

                    if (isDragging) {

                        // لأن الفقاعة مثبتة من جهة اليمين
                        params.x =
                            initialX -
                                dx.toInt()

                        params.y =
                            initialY +
                                dy.toInt()

                        // منع الإحداثيات السالبة
                        params.x =
                            params.x.coerceAtLeast(0)

                        params.y =
                            params.y.coerceAtLeast(0)

                        try {

                            windowManager.updateViewLayout(
                                view,
                                params
                            )

                        } catch (_: Exception) {
                        }
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {

                    if (isDragging) {

                        // حفظ موقع الفقاعة
                        bubbleX = params.x
                        bubbleY = params.y

                    } else {

                        // الضغط يفتح لوحة المعلومات
                        showPanel(view)
                    }

                    true
                }

                MotionEvent.ACTION_CANCEL -> {

                    isDragging = false
                    true
                }

                else -> true
            }
        }

        windowManager.addView(
            bubble,
            params
        )

        overlayView = bubble
    }

    private fun showPanel(
        oldBubble: View
    ) {

        val panel =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    30,
                    25,
                    30,
                    25
                )

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.rgb(
                                25,
                                25,
                                25
                            )
                        )

                        cornerRadius = 30f

                        setStroke(
                            2,
                            Color.rgb(
                                0,
                                200,
                                80
                            )
                        )
                    }
            }

        // =========================
        // عنوان التطبيق
        // =========================

        val title =
            TextView(this).apply {

                text = "🚕 RideClick"
                textSize = 20f

                setTextColor(
                    Color.WHITE
                )
            }

        panel.addView(title)

        // =========================
        // حالة التطبيق
        // =========================

        val status =
            TextView(this).apply {

                text =
                    "● جاهز لاستقبال الطلبات"

                textSize = 16f

                setTextColor(
                    Color.GREEN
                )
            }

        val statusParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = 15
            }

        panel.addView(
            status,
            statusParams
        )

        // =========================
        // نتيجة قراءة طلب Jeeny
        // =========================

        offerTextView =
            TextView(this).apply {

                textSize = 18f

                setTextColor(
                    Color.WHITE
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    20,
                    20,
                    20,
                    20
                )

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.rgb(
                                40,
                                40,
                                40
                            )
                        )

                        cornerRadius = 20f
                    }
            }

        val offerParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = 20
            }

        panel.addView(
            offerTextView,
            offerParams
        )

        // عرض آخر طلب إن وجد
        updateOfferText()

        // =========================
        // الخدمات
        // =========================

        val services =
            TextView(this).apply {

                text = """
                    🚕 الخدمات:
                    جيني
                    بترا رايد
                """.trimIndent()

                textSize = 15f

                setTextColor(
                    Color.WHITE
                )
            }

        val servicesParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = 15
            }

        panel.addView(
            services,
            servicesParams
        )

        // =========================
        // الفلاتر
        // =========================

        val filters =
            TextView(this).apply {

                text = """
                    ⚙️ الفلاتر:
                    السعر ≥ 4.00 د.أ
                    الوقت ≤ 5 دقائق
                """.trimIndent()

                textSize = 15f

                setTextColor(
                    Color.WHITE
                )
            }

        val filtersParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = 15
            }

        panel.addView(
            filters,
            filtersParams
        )

        // =========================
        // زر إيقاف التطبيق
        // =========================

        val stopButton =
            Button(this).apply {

                text =
                    "إيقاف RideClick"

                setOnClickListener {
                    stopSelf()
                }
            }

        val stopParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = 20
            }

        panel.addView(
            stopButton,
            stopParams
        )

        // =========================
        // زر التصغير
        // =========================

        val minimizeButton =
            Button(this).apply {

                text = "تصغير"

                setOnClickListener {
                    showBubble()
                }
            }

        val minimizeParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        panel.addView(
            minimizeButton,
            minimizeParams
        )

        // =========================
        // عرض اللوحة مكان الفقاعة
        // =========================

        val panelParams =
            createLayoutParams()

        try {

            // إضافة اللوحة أولاً
            windowManager.addView(
                panel,
                panelParams
            )

            // ثم إزالة الفقاعة
            windowManager.removeView(
                oldBubble
            )

            overlayView = panel

        } catch (_: Exception) {

            try {
                windowManager.removeView(
                    panel
                )
            } catch (_: Exception) {
            }
        }
    }

    // =========================
    // تحديث بيانات طلب Jeeny
    // =========================

    private fun updateOfferText() {

        val textView =
            offerTextView ?: return

        val price =
            lastPrice

        val minutes =
            lastMinutes

        val matched =
            lastMatched

        // لم يتم التقاط أي طلب حتى الآن
        if (
            price == null ||
            minutes == null ||
            matched == null
        ) {

            textView.text =
                """
                📡 مراقبة Jeeny
                بانتظار طلب جديد...
                """.trimIndent()

            textView.setTextColor(
                Color.LTGRAY
            )

            return
        }

        // الطلب مطابق للشروط
        if (matched) {

            textView.text =
                """
                📥 آخر طلب Jeeny
                💰 JOD %.2f
                ⏱️ %d دقيقة
                ✅ مطابق للشروط
                """.trimIndent()
                    .format(
                        price,
                        minutes
                    )

            textView.setTextColor(
                Color.GREEN
            )

        } else {

            // الطلب غير مطابق للشروط
            textView.text =
                """
                📥 آخر طلب Jeeny
                💰 JOD %.2f
                ⏱️ %d دقيقة
                ❌ غير مطابق للشروط
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

    override fun onDestroy() {

        // إلغاء استقبال Broadcast
        try {

            unregisterReceiver(
                offerReceiver
            )

        } catch (_: Exception) {
        }

        // إزالة الـ Overlay
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

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
