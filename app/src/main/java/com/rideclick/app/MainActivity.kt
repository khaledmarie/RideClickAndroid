package com.rideclick.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    companion object {

        const val PREFS_NAME = "rideclick_prefs"

        const val KEY_MIN_PRICE = "min_price"
        const val KEY_MAX_MINUTES = "max_minutes"

        const val DEFAULT_MIN_PRICE = 4.0f
        const val DEFAULT_MAX_MINUTES = 5
    }

    private lateinit var priceInput: EditText
    private lateinit var minutesInput: EditText
    private lateinit var currentFiltersText: TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        createMainScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::currentFiltersText.isInitialized) {
            updateCurrentFiltersText()
        }
    }

    // ==========================================
    // إنشاء الواجهة الرئيسية
    // ==========================================

    private fun createMainScreen() {

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        val savedPrice =
            prefs.getFloat(
                KEY_MIN_PRICE,
                DEFAULT_MIN_PRICE
            )

        val savedMinutes =
            prefs.getInt(
                KEY_MAX_MINUTES,
                DEFAULT_MAX_MINUTES
            )

        // ======================================
        // الحاوية الرئيسية
        // ======================================

        val layout =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    40,
                    60,
                    40,
                    40
                )

                setBackgroundColor(
                    Color.rgb(
                        20,
                        20,
                        20
                    )
                )
            }

        // ======================================
        // عنوان التطبيق
        // ======================================

        val title =
            TextView(this).apply {

                text =
                    "🚕 RideClick"

                textSize =
                    28f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )
            }

        layout.addView(title)

        // ======================================
        // وصف
        // ======================================

        val description =
            TextView(this).apply {

                text =
                    "فلترة طلبات التوصيل"

                textSize =
                    16f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.LTGRAY
                )

                setPadding(
                    0,
                    10,
                    0,
                    30
                )
            }

        layout.addView(description)

        // ======================================
        // عرض الفلاتر الحالية
        // ======================================

        currentFiltersText =
            TextView(this).apply {

                textSize =
                    17f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.GREEN
                )

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

        layout.addView(
            currentFiltersText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    30
            }
        )

        updateCurrentFiltersText()

        // ======================================
        // عنوان أقل سعر
        // ======================================

        val priceLabel =
            TextView(this).apply {

                text =
                    "💰 أقل سعر للطلب (JOD)"

                textSize =
                    18f

                setTextColor(
                    Color.WHITE
                )
            }

        layout.addView(priceLabel)

        // ======================================
        // إدخال أقل سعر
        // ======================================

        priceInput =
            EditText(this).apply {

                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    savedPrice.toString()
                )

                hint =
                    "مثال: 4.00"

                textSize =
                    18f

                setTextColor(
                    Color.WHITE
                )

                setHintTextColor(
                    Color.GRAY
                )
            }

        layout.addView(
            priceInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    25
            }
        )

        // ======================================
        // عنوان أقصى وقت
        // ======================================

        val minutesLabel =
            TextView(this).apply {

                text =
                    "⏱️ أقصى وقت للوصول (دقائق)"

                textSize =
                    18f

                setTextColor(
                    Color.WHITE
                )
            }

        layout.addView(minutesLabel)

        // ======================================
        // إدخال أقصى وقت
        // ======================================

        minutesInput =
            EditText(this).apply {

                inputType =
                    InputType.TYPE_CLASS_NUMBER

                setText(
                    savedMinutes.toString()
                )

                hint =
                    "مثال: 5"

                textSize =
                    18f

                setTextColor(
                    Color.WHITE
                )

                setHintTextColor(
                    Color.GRAY
                )
            }

        layout.addView(
            minutesInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    20
            }
        )

        // ======================================
        // زر حفظ الفلاتر
        // ======================================

        val saveButton =
            Button(this).apply {

                text =
                    "💾 حفظ الفلاتر"

                setOnClickListener {

                    saveFilters()
                }
            }

        layout.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // ======================================
        // زر تشغيل RideClick
        // ======================================

        val startButton =
            Button(this).apply {

                text =
                    "🟢 تشغيل RideClick"

                setOnClickListener {

                    startRideClick()
                }
            }

        layout.addView(
            startButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin =
                    15
            }
        )

        // ======================================
        // فتح إعدادات Accessibility
        // ======================================

        val accessibilityButton =
            Button(this).apply {

                text =
                    "♿ فتح إعدادات Accessibility"

                setOnClickListener {

                    openAccessibilitySettings()
                }
            }

        layout.addView(
            accessibilityButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin =
                    15
            }
        )

        // ======================================
        // عرض الواجهة
        // ======================================

        setContentView(layout)
    }

    // ==========================================
    // حفظ الفلاتر
    // ==========================================

    private fun saveFilters() {

        val priceText =
            priceInput.text
                .toString()
                .trim()
                .replace(
                    ",",
                    "."
                )

        val minutesText =
            minutesInput.text
                .toString()
                .trim()

        val price =
            priceText.toFloatOrNull()

        val minutes =
            minutesText.toIntOrNull()

        // فحص السعر

        if (
            price == null ||
            price < 0
        ) {

            Toast.makeText(
                this,
                "أدخل سعرًا صحيحًا",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // فحص الوقت

        if (
            minutes == null ||
            minutes <= 0
        ) {

            Toast.makeText(
                this,
                "أدخل عدد دقائق صحيحًا",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // حفظ القيم

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        prefs.edit()
            .putFloat(
                KEY_MIN_PRICE,
                price
            )
            .putInt(
                KEY_MAX_MINUTES,
                minutes
            )
            .apply()

        // تحديث العرض

        updateCurrentFiltersText()

        Toast.makeText(
            this,
            "✅ تم حفظ الفلاتر",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ==========================================
    // تحديث الفلاتر المعروضة
    // ==========================================

    private fun updateCurrentFiltersText() {

        val prefs =
            getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        val price =
            prefs.getFloat(
                KEY_MIN_PRICE,
                DEFAULT_MIN_PRICE
            )

        val minutes =
            prefs.getInt(
                KEY_MAX_MINUTES,
                DEFAULT_MAX_MINUTES
            )

        currentFiltersText.text =
            """
            ⚙️ الفلاتر الحالية
            💰 السعر ≥ %.2f JOD
            ⏱️ الوقت ≤ %d دقائق
            """.trimIndent()
                .format(
                    price,
                    minutes
                )
    }

    // ==========================================
    // تشغيل RideClick Overlay
    // ==========================================

    private fun startRideClick() {

        // فحص صلاحية الظهور فوق التطبيقات

        if (
            !Settings.canDrawOverlays(this)
        ) {

            Toast.makeText(
                this,
                "فعّل إذن الظهور فوق التطبيقات أولًا",
                Toast.LENGTH_LONG
            ).show()

            try {

                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse(
                            "package:$packageName"
                        )
                    )

                startActivity(intent)

            } catch (_: Exception) {

                startActivity(
                    Intent(
                        Settings.ACTION_SETTINGS
                    )
                )
            }

            return
        }

        // تشغيل خدمة الفقاعة

        try {

            val serviceIntent =
                Intent(
                    this,
                    RideClickOverlayService::class.java
                )

            startService(
                serviceIntent
            )

            Toast.makeText(
                this,
                "🟢 RideClick يعمل",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "حدث خطأ أثناء تشغيل RideClick",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ==========================================
    // فتح إعدادات Accessibility
    // ==========================================

    private fun openAccessibilitySettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                )

            startActivity(intent)

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "تعذر فتح إعدادات Accessibility",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
