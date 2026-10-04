package com.rideclick.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class RideClickOverlayService : Service() {

    private lateinit var windowManager: WindowManager

    private var overlayView: View? = null

    // موقع الفقاعة
    private var bubbleX = 20
    private var bubbleY = 120

    // بيانات السحب
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var isDragging = false

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        showBubble()
    }

    // =========================================================
    // إنشاء الفقاعة
    // =========================================================

    private fun showBubble() {

        // إزالة أي View موجود
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        val bubble = TextView(this)

        bubble.text = "🟢 RideClick"
        bubble.textSize = 14f
        bubble.setTextColor(Color.WHITE)
        bubble.gravity = Gravity.CENTER
        bubble.setPadding(20, 12, 20, 12)

        val background = GradientDrawable()
        background.setColor(Color.rgb(30, 30, 30))
        background.cornerRadius = 100f
        background.setStroke(2, Color.rgb(0, 200, 80))

        bubble.background = background

        val params = createLayoutParams()

        // =====================================================
        // التحكم بالسحب والضغط
        // =====================================================

        bubble.setOnTouchListener { view, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    initialX = params.x
                    initialY = params.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    isDragging = false

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    // إذا تحركت أكثر من 10 بكسل نعتبرها عملية سحب
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isDragging = true
                    }

                    if (isDragging) {

                        // لأن Gravity = END
                        // زيادة حركة الإصبع لليمين تعني تقليل x
                        params.x = initialX - dx.toInt()
                        params.y = initialY + dy.toInt()

                        // منع الفقاعة من الخروج بعيدًا عن الشاشة
                        if (params.x < 0) {
                            params.x = 0
                        }

                        if (params.y < 0) {
                            params.y = 0
                        }

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

                    if (!isDragging) {

                        // ضغطة عادية
                        showPanel(view)

                    } else {

                        // انتهى السحب
                        bubbleX = params.x
                        bubbleY = params.y
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

        windowManager.addView(bubble, params)

        overlayView = bubble
    }

    // =========================================================
    // إنشاء إعدادات الفقاعة
    // =========================================================

    private fun createLayoutParams(): WindowManager.LayoutParams {

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            PixelFormat.TRANSLUCENT
        ).apply {

            gravity = Gravity.TOP or Gravity.END

            x = bubbleX
            y = bubbleY
        }
    }

    // =========================================================
    // لوحة RideClick
    // =========================================================

    private fun showPanel(oldBubble: View) {

        val panel = LinearLayout(this)

        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(30, 25, 30, 25)

        val background = GradientDrawable()
        background.setColor(Color.rgb(25, 25, 25))
        background.cornerRadius = 30f
        background.setStroke(2, Color.rgb(0, 200, 80))

        panel.background = background

        // -----------------------------------------------------
        // العنوان
        // -----------------------------------------------------

        val title = TextView(this)

        title.text = "🟢 RideClick"
        title.textSize = 20f
        title.setTextColor(Color.WHITE)

        panel.addView(title)

        // -----------------------------------------------------
        // الحالة
        // -----------------------------------------------------

        val status = TextView(this)

        status.text = "● جاهز لاستقبال الطلبات"
        status.textSize = 16f
        status.setTextColor(Color.GREEN)

        val statusParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        statusParams.topMargin = 15

        panel.addView(status, statusParams)

        // -----------------------------------------------------
        // الخدمات
        // -----------------------------------------------------

        val services = TextView(this)

        services.text = """
🚕 الخدمات:

جيني
بترا رايد
        """.trimIndent()

        services.textSize = 15f
        services.setTextColor(Color.WHITE)

        val servicesParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        servicesParams.topMargin = 15

        panel.addView(services, servicesParams)

        // -----------------------------------------------------
        // الفلاتر
        // -----------------------------------------------------

        val filters = TextView(this)

        filters.text = """
⚙️ الفلاتر:

السعر ≥ 4.00 د.أ
الوقت ≤ 5 دقائق
        """.trimIndent()

        filters.textSize = 15f
        filters.setTextColor(Color.WHITE)

        val filtersParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        filtersParams.topMargin = 15

        panel.addView(filters, filtersParams)

        // -----------------------------------------------------
        // زر إيقاف
        // -----------------------------------------------------

        val stopButton = Button(this)

        stopButton.text = "إيقاف RideClick"

        stopButton.setOnClickListener {

            stopSelf()
        }

        val stopParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        stopParams.topMargin = 20

        panel.addView(stopButton, stopParams)

        // -----------------------------------------------------
        // زر تصغير
        // -----------------------------------------------------

        val minimizeButton = Button(this)

        minimizeButton.text = "تصغير"

        minimizeButton.setOnClickListener {

            showBubble()
        }

        val minimizeParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        panel.addView(minimizeButton, minimizeParams)

        // =====================================================
        // إضافة اللوحة
        // =====================================================

        val panelParams = createLayoutParams()

        try {

            windowManager.addView(
                panel,
                panelParams
            )

            windowManager.removeView(oldBubble)

            overlayView = panel

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // إيقاف الخدمة
    // =========================================================

    override fun onDestroy() {

        overlayView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        overlayView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
