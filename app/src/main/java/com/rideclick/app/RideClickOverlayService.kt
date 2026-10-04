package com.rideclick.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class RideClickOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        showOverlay()
    }

    private fun showOverlay() {

        val textView = TextView(this)

        textView.text = "🟢 RideClick"
        textView.textSize = 16f
        textView.setTextColor(Color.WHITE)
        textView.setBackgroundColor(Color.rgb(30, 30, 30))
        textView.setPadding(24, 12, 24, 12)

        textView.setOnClickListener {
            stopOverlay()
        }

        val layoutType =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 120

        windowManager.addView(textView, params)

        overlayView = textView
    }

    private fun stopOverlay() {

        overlayView?.let {
            windowManager.removeView(it)
        }

        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {

        overlayView?.let {
            windowManager.removeView(it)
        }

        overlayView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
