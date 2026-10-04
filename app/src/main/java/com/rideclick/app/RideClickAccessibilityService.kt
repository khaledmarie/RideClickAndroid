package com.rideclick.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
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
    private var expanded = false

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        showBubble()
    }

    private fun showBubble() {

        val bubble = TextView(this)

        bubble.text = "🟢 RideClick"
        bubble.textSize = 16f
        bubble.setTextColor(Color.WHITE)
        bubble.gravity = Gravity.CENTER
        bubble.setPadding(24, 14, 24, 14)

        val background = GradientDrawable()
        background.setColor(Color.rgb(30, 30, 30))
        background.cornerRadius = 50f

        bubble.background = background

        bubble.setOnClickListener {
            if (expanded) {
                showBubble()
            } else {
                showPanel()
            }
        }

        val params = createLayoutParams()

        windowManager.addView(bubble, params)

        overlayView = bubble
    }

    private fun showPanel() {

        removeCurrentView()

        val panel = LinearLayout(this)

        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(24, 20, 24, 20)

        val background = GradientDrawable()

        background.setColor(Color.rgb(30, 30, 30))
        background.cornerRadius = 30f

        panel.background = background

        val title = TextView(this)

        title.text = "🟢 RideClick"
        title.textSize = 20f
        title.setTextColor(Color.WHITE)

        panel.addView(title)

        val status = TextView(this)

        status.text = "جاهز لاستقبال الطلبات"
        status.textSize = 16f
        status.setTextColor(Color.WHITE)

        val statusParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        statusParams.topMargin = 12

        panel.addView(status, statusParams)

        val filters = TextView(this)

        filters.text =
            "السعر ≥ 4.00 د.أ\nالوقت ≤ 5 دقائق"

        filters.textSize = 15f
        filters.setTextColor(Color.LTGRAY)

        val filterParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        filterParams.topMargin = 12

        panel.addView(filters, filterParams)

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

        stopParams.topMargin = 12

        panel.addView(stopButton, stopParams)

        val closeButton = Button(this)

        closeButton.text = "تصغير"

        closeButton.setOnClickListener {

            expanded = false

            showBubble()
        }

        val closeParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        panel.addView(closeButton, closeParams)

        val params = createLayoutParams()

        windowManager.addView(panel, params)

        overlayView = panel

        expanded = true
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            android.graphics.PixelFormat.TRANSLUCENT
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

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}
