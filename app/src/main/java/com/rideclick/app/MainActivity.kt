package com.rideclick.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RideClickApp()
        }
    }
}

@Composable
fun RideClickApp() {

    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "rideclick_settings",
            Context.MODE_PRIVATE
        )
    }

    var enabled by remember {
        mutableStateOf(
            prefs.getBoolean("enabled", false)
        )
    }

    var jeenyEnabled by remember {
        mutableStateOf(
            prefs.getBoolean("jeeny", true)
        )
    }

    var petraEnabled by remember {
        mutableStateOf(
            prefs.getBoolean("petra", true)
        )
    }

    var minPrice by remember {
        mutableStateOf(
            prefs.getString("min_price", "4.00") ?: "4.00"
        )
    }

    var maxTime by remember {
        mutableStateOf(
            prefs.getString("max_time", "5") ?: "5"
        )
    }

    fun saveSettings() {

        prefs.edit()
            .putBoolean("enabled", enabled)
            .putBoolean("jeeny", jeenyEnabled)
            .putBoolean("petra", petraEnabled)
            .putString("min_price", minPrice)
            .putString("max_time", maxTime)
            .apply()
    }

    fun openOverlaySettings() {

        if (!Settings.canDrawOverlays(context)) {

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )

            context.startActivity(intent)

        } else {

            val intent = Intent(
                context,
                RideClickOverlayService::class.java
            )

            context.startService(intent)
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {

        MaterialTheme {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),

                verticalArrangement = Arrangement.Top,

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "RideClick",
                    fontSize = 30.sp,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (enabled)
                        "جاهز لاستقبال الطلبات"
                    else
                        "متوقف",

                    fontSize = 20.sp,

                    color = if (enabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = if (enabled)
                                "RideClick يعمل"
                            else
                                "RideClick متوقف",

                            fontSize = 18.sp
                        )

                        Switch(
                            checked = enabled,

                            onCheckedChange = {

                                enabled = it

                                saveSettings()

                                if (enabled) {
                                    openOverlaySettings()
                                }
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "الخدمات المدعومة",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = "جيني — Jeeny",
                                fontSize = 17.sp
                            )

                            Switch(
                                checked = jeenyEnabled,

                                onCheckedChange = {

                                    jeenyEnabled = it

                                    saveSettings()
                                }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = "بترا رايد — Petra Ride",
                                fontSize = 17.sp
                            )

                            Switch(
                                checked = petraEnabled,

                                onCheckedChange = {

                                    petraEnabled = it

                                    saveSettings()
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "فلاتر الطلبات",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = minPrice,

                    onValueChange = {
                        minPrice = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("الحد الأدنى للسعر")
                    },

                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = maxTime,

                    onValueChange = {
                        maxTime = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("أقصى وقت للطلب بالدقائق")
                    },

                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {
                        saveSettings()
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ الإعدادات")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                TextButton(
                    onClick = {

                        val intent = Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )

                        context.startActivity(intent)
                    }
                ) {
                    Text("تفعيل Accessibility")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        openOverlaySettings()
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تشغيل الفقاعة العائمة")
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text =
                        "السعر ≥ $minPrice د.أ  •  الوقت ≤ $maxTime دقائق",

                    fontSize = 14.sp
                )
            }
        }
    }
}
