package com.rideclick.app

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private var clickMarkerView: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // موقع الفقاعة
    private var bubbleX = 20
    private var bubbleY = 120

    // آخر طلب
    private var lastPrice: Double? = null
    private var lastMinutes: Int? = null
    private var lastMatched: Boolean? = null
    private var lastAcceptFound: Boolean? = null

    // الفلاتر الحالية
    private var currentMinPrice: Double = 4.0
    private var currentMaxMinutes: Int = 5

    private var offerTextView: TextView? = null
    private var filtersTextView: TextView? = null

    // =============================
    // استقبال بيانات Accessibility
    // =============================

    private val offerReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                when (intent?.action) {

                    RideClickAccessibilityService.ACTION_CLICK_MARKER -> {

                        val x =
                            intent.getIntExtra(
                                RideClickAccessibilityService.EXTRA_CLICK_X,
                                -1
                            )

                        val y =
                            intent.getIntExtra(
                                RideClickAccessibilityService.EXTRA_CLICK_Y,
                                -1
                            )

                        if (x >= 0 && y >= 0) {
                            showClickMarker(
                                x,
                                y
                            )
                        }

                        return
                    }

                    RideClickAccessibilityService.ACTION_OFFER_UPDATE -> {
                        // نكمل أدناه لتحديث بيانات الطلب
                    }

                    else -> return
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

                val acceptFound =
                    intent.getBooleanExtra(
                        RideClickAccessibilityService.EXTRA_ACCEPT_FOUND,
                        false
                    )

                currentMinPrice =
                    intent.getDoubleExtra(
                        RideClickAccessibilityService.EXTRA_MIN_PRICE,
                        readMinPrice()
                    )

                currentMaxMinutes =
                    intent.getIntExtra(
                        RideClickAccessibilityService.EXTRA_MAX_MINUTES,
                        readMaxMinutes()
                    )

                lastPrice = price
                lastMinutes = minutes
                lastMatched = matched
                lastAcceptFound = acceptFound

                updateOfferText()
                updateFiltersText()
            }
        }

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        // اقرأ الفلاتر عند تشغيل الخدمة
        currentMinPrice = readMinPrice()
        currentMaxMinutes = readMaxMinutes()

        val filter =
            IntentFilter().apply {

                addAction(
                    RideClickAccessibilityService.ACTION_OFFER_UPDATE
                )

                addAction(
                    RideClickAccessibilityService.ACTION_CLICK_MARKER
                )
            }

        registerReceiver(
            offerReceiver,
            filter,
            RECEIVER_NOT_EXPORTED
        )

        showBubble()
    }

    // =============================
    // قراءة أقل سعر محفوظ
    // =============================

    private fun readMinPrice(): Double {

        val prefs =
            getSharedPreferences(
                MainActivity.PREFS_NAME,
                MODE_PRIVATE
            )

        return prefs.getFloat(
            MainActivity.KEY_MIN_PRICE,
            MainActivity.DEFAULT_MIN_PRICE
        ).toDouble()
    }

    // =============================
    // قراءة أقصى وقت محفوظ
    // =============================

    private fun readMaxMinutes(): Int {

        val prefs =
            getSharedPreferences(
                MainActivity.PREFS_NAME,
                MODE_PRIVATE
            )

        return prefs.getInt(
            MainActivity.KEY_MAX_MINUTES,
            MainActivity.DEFAULT_MAX_MINUTES
        )
    }

    // =============================
    // إعدادات Overlay
    // =============================

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

    // =============================
    // إظهار الفقاعة
    // =============================

    private fun showBubble() {

        overlayView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        overlayView = null
        offerTextView = null
        filtersTextView = null

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

        val bubbleSize =
            (
                65 *
                resources.displayMetrics.density
            ).toInt()

        params.width = bubbleSize
        params.height = bubbleSize

        var initialX = 0
        var initialY = 0

        var initialTouchX = 0f
        var initialTouchY = 0f

        var isDragging = false

        bubble.setOnTouchListener {
                view,
                event ->

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

                        params.x =
                            initialX -
                            dx.toInt()

                        params.y =
                            initialY +
                            dy.toInt()

                        params.x =
                            params.x.coerceAtLeast(0)

                        params.y =
                            params.y.coerceAtLeast(0)

                        try {

                            windowManager
                                .updateViewLayout(
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

                        bubbleX =
                            params.x

                        bubbleY =
                            params.y

                    } else {

                        showPanel(
                            view
                        )
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

    // =============================
    // لوحة RideClick
    // =============================

    private fun showPanel(
        oldBubble: View
    ) {

        // نقرأ أحدث فلاتر
        currentMinPrice =
            readMinPrice()

        currentMaxMinutes =
            readMaxMinutes()

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

                        cornerRadius =
                            30f

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
        // العنوان
        // =========================

        val title =
            TextView(this).apply {

                text =
                    "🚕 RideClick"

                textSize =
                    20f

                setTextColor(
                    Color.WHITE
                )
            }

        panel.addView(
            title
        )

        // =========================
        // الحالة
        // =========================

        val status =
            TextView(this).apply {

                text =
                    "● جاهز لاستقبال الطلبات"

                textSize =
                    16f

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
        // بيانات آخر طلب
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

                        cornerRadius =
                            20f
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

        updateOfferText()

        // =========================
        // الفلاتر الحالية
        // =========================

        filtersTextView =
            TextView(this).apply {

                textSize =
                    15f

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    10,
                    15,
                    10,
                    15
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
            filtersTextView,
            filtersParams
        )

        updateFiltersText()

        // =========================
        // زر تحديث الفلاتر
        // =========================

        val refreshButton =
            Button(this).apply {

                text =
                    "🔄 تحديث الفلاتر"

                setOnClickListener {

                    currentMinPrice =
                        readMinPrice()

                    currentMaxMinutes =
                        readMaxMinutes()

                    updateFiltersText()
                }
            }

        panel.addView(
            refreshButton
        )

        // =========================
        // زر تصغير
        // =========================

        val minimizeButton =
            Button(this).apply {

                text =
                    "تصغير"

                setOnClickListener {

                    showBubble()
                }
            }

        panel.addView(
            minimizeButton
        )

        // =========================
        // زر إيقاف
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

                topMargin = 15
            }

        panel.addView(
            stopButton,
            stopParams
        )

        // =========================
        // عرض اللوحة
        // =========================

        val panelParams =
            createLayoutParams()

        try {

            windowManager.addView(
                panel,
                panelParams
            )

            windowManager.removeView(
                oldBubble
            )

            overlayView =
                panel

        } catch (_: Exception) {

            try {
                windowManager.removeView(
                    panel
                )
            } catch (_: Exception) {
            }
        }
    }

    // =============================
    // دائرة حمراء في مكان محاولة الضغط
    // تظهر لمدة ثانية واحدة فقط
    // =============================

    private fun showClickMarker(
        screenX: Int,
        screenY: Int
    ) {

        clickMarkerView?.let {

            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }

        clickMarkerView = null

        val markerSize =
            (
                42 *
                    resources.displayMetrics.density
                ).toInt()

        val marker =
            View(this).apply {

                background =
                    GradientDrawable().apply {

                        shape =
                            GradientDrawable.OVAL

                        setColor(
                            Color.argb(
                                210,
                                255,
                                0,
                                0
                            )
                        )

                        setStroke(
                            (
                                3 *
                                    resources.displayMetrics.density
                                ).toInt(),
                            Color.WHITE
                        )
                    }
            }

        val params =
            WindowManager.LayoutParams(
                markerSize,
                markerSize,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {

                gravity =
                    Gravity.TOP or Gravity.START

                x =
                    screenX -
                        markerSize / 2

                y =
                    screenY -
                        markerSize / 2
            }

        try {

            windowManager.addView(
                marker,
                params
            )

            clickMarkerView = marker

            mainHandler.postDelayed(
                {

                    if (
                        clickMarkerView === marker
                    ) {

                        try {
                            windowManager.removeView(
                                marker
                            )
                        } catch (_: Exception) {
                        }

                        clickMarkerView = null
                    }
                },
                1000L
            )

        } catch (_: Exception) {

            clickMarkerView = null
        }
    }

    // =============================
    // تحديث بيانات آخر طلب
    // =============================

    private fun updateOfferText() {

        val textView =
            offerTextView ?: return

        val price =
            lastPrice

        val minutes =
            lastMinutes

        val matched =
            lastMatched

        val acceptFound =
            lastAcceptFound

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

        val acceptStatus =
            if (acceptFound == true) {

                "🎯 تم العثور على شريط قبول العرض"

            } else {

                "⚠️ لم يتم العثور على شريط قبول العرض"
            }

        if (matched) {

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