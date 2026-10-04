package com.rideclick.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class RideClickOverlayService : Service() {

    private lateinit var windowManager: WindowManager

    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        showBubble()
    }

    private fun showBubble() {

        removeCurrentView()

        val bubble = TextView(this)

        bubble.text = "🟢 RideClick"
        bubble.textSize = 16f
        bubble.setTextColor(Color.WHITE)
        bubble.gravity = Gravity.CENTER

        bubble.setPadding(
            30,
            18,
            30,
            18
        )

        val background = GradientDrawable()
        background.setColor(Color.rgb(30, 30, 30))
        background.cornerRadius = 60f

        bubble.background = background

        bubble.setOnClickListener {
            showPanel(bubble)
        }

        windowManager.addView(
            bubble,
            createLayoutParams()
        )

        overlayView = bubble
    }

    private fun showPanel(oldBubble: View) {

        val panel = LinearLayout(this)

        panel.orientation = LinearLayout.VERTICAL

        panel.setPadding(
            30,
            25,
            30,
            25
        )

        val background = GradientDrawable()

        background.setColor(
            Color.rgb(30, 30, 30)
        )

        background.cornerRadius = 30f

        panel.background = background

        // العنوان
        val title = TextView(this)

        title.text = "🟢 RideClick"
        title.textSize = 22f
        title.setTextColor(Color.WHITE)

        panel.addView(title)

        // الحالة
        val status = TextView(this)

        status.text = "● جاهز لاستقبال الطلبات"
        status.textSize = 16f
        status.setTextColor(Color.GREEN)

        val statusParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        statusParams.topMargin = 15

        panel.addView(
            status,
            statusParams
        )

        // الخدمات
        val services = TextView(this)

        services.text =
            "الخدمات:\nجيني\nبترا رايد"

        services.textSize = 15f
        services.setTextColor(Color.WHITE)

        val servicesParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        servicesParams.topMargin = 15

        panel.addView(
            services,
            servicesParams
        )

        // الفلاتر
        val filters = TextView(this)

        filters.text =
            "الفلاتر:\nالسعر ≥ 4.00 د.أ\nالوقت ≤ 5 دقائق"

        filters.textSize = 15f
        filters.setTextColor(Color.LTGRAY)

        val filtersParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        filtersParams.topMargin = 15

        panel.addView(
            filters,
            filtersParams
        )

        // زر الإيقاف
        val stopButton = Button(this)

        stopButton.text = "إيقاف RideClick"

        stopButton.setOnClickListener {
            stopSelf()
        }

        val stopParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        stopParams.topMargin = 15

        panel.addView(
            stopButton,
            stopParams
        )

        // زر التصغير
        val minimizeButton = Button(this)

        minimizeButton.text = "تصغير"

        minimizeButton.setOnClickListener {
            showBubble()
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

        // نضيف اللوحة
        windowManager.addView(
            panel,
            createLayoutParams()
        )

        // نحذف الفقاعة القديمة
        try {
            windowManager.removeView(oldBubble)
        } catch (_: Exception) {
        }

        // الآن اللوحة هي الـ View الحالية
        overlayView = panel
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

            x = 20
            y = 120
        }
    }

    private fun removeCurrentView() {

        overlayView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        overlayView = null
    }

    override fun onDestroy() {

        removeCurrentView()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
