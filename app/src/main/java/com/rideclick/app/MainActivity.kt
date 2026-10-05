package com.rideclick.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "rideclick_prefs"
        const val KEY_MIN_PRICE = "min_price"
        const val KEY_MAX_MINUTES = "max_minutes"

        const val DEFAULT_MIN_PRICE = 4.0f
        const val DEFAULT_MAX_MINUTES = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                40,
                60,
                40,
                40
            )
        }

        // =========================
        // العنوان
        // =========================

        val title = TextView(this).apply {
            text = "🚕 RideClick"
            textSize = 28f
        }

        layout.addView(title)

        // =========================
        // أقل سعر
        // =========================

        val priceLabel = TextView(this).apply {
            text = "💰 أقل سعر للطلب (JOD)"
            textSize = 18f
        }

        layout.addView(priceLabel)

        val savedPrice = prefs.getFloat(
            KEY_MIN_PRICE,
            DEFAULT_MIN_PRICE
        )

        val priceInput = EditText(this).apply {

            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(
                savedPrice.toString()
            )

            hint = "مثال: 4.00"
        }

        layout.addView(priceInput)

        // =========================
        // أقصى وقت
        // =========================

        val minutesLabel = TextView(this).apply {
            text = "⏱️ أقصى وقت للوصول (دقائق)"
            textSize = 18f
        }

        layout.addView(minutesLabel)

        val savedMinutes = prefs.getInt(
            KEY_MAX_MINUTES,
            DEFAULT_MAX_MINUTES
        )

        val minutesInput = EditText(this).apply {

            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            setText(
                savedMinutes.toString()
            )

            hint = "مثال: 5"
        }

        layout.addView(minutesInput)

        // =========================
        // زر حفظ الفلاتر
        // =========================

        val saveButton = Button(this).apply {

            text = "💾 حفظ الفلاتر"

            setOnClickListener {

                val price =
                    priceInput.text
                        .toString()
                        .replace(",", ".")
                        .toFloatOrNull()

                val minutes =
                    minutesInput.text
                        .toString()
                        .toIntOrNull()

                if (
                    price == null ||
                    price < 0
                ) {

                    Toast.makeText(
                        this@MainActivity,
                        "أدخل سعرًا صحيحًا",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                if (
                    minutes == null ||
                    minutes <= 0
                ) {

                    Toast.makeText(
                        this@MainActivity,
                        "أدخل عدد دقائق صحيحًا",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

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

                Toast.makeText(
                    this@MainActivity,
                    "✅ تم حفظ الفلاتر: السعر ≥ %.2f JOD — الوقت ≤ %d دقيقة"
                        .format(
                            price,
                            minutes
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        layout.addView(saveButton)

        // =========================
        // تشغيل الفقاعة
        // =========================

        val overlayButton = Button(this).apply {

            text = "🟢 تشغيل RideClick"

            setOnClickListener {

                if (
                    !Settings.canDrawOverlays(
                        this@MainActivity
                    )
                ) {

                    Toast.makeText(
                        this@MainActivity,
                        "فعّل إذن الظهور فوق التطبيقات",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                startService(
                    Intent(
                        this@MainActivity,
                        RideClickOverlayService::class.java
                    )
                )

                Toast.makeText(
                    this@MainActivity,
                    "RideClick يعمل",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        layout.addView(overlayButton)

        setContentView(layout)
    }
}
