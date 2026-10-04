package com.rideclick.app

import android.content.Intent
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RideClickApp(
                onOpenAccessibilitySettings = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun RideClickApp(
    onOpenAccessibilitySettings: () -> Unit
) {
    var enabled by remember { mutableStateOf(false) }
    var jeenyEnabled by remember { mutableStateOf(true) }
    var petraEnabled by remember { mutableStateOf(true) }
    var minPrice by remember { mutableStateOf("4.00") }
    var maxTime by remember { mutableStateOf("5") }

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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (enabled)
                        "جاهز لاستقبال الطلبات"
                    else
                        "متوقف",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (enabled) "تشغيل" else "إيقاف",
                            fontSize = 18.sp
                        )

                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "الخدمات المدعومة",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جيني — Jeeny",
                                fontSize = 17.sp
                            )

                            Switch(
                                checked = jeenyEnabled,
                                onCheckedChange = {
                                    jeenyEnabled = it
                                }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بترا رايد — Petra Ride",
                                fontSize = 17.sp
                            )

                            Switch(
                                checked = petraEnabled,
                                onCheckedChange = {
                                    petraEnabled = it
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "فلاتر الطلبات",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // سيتم ربط الحفظ لاحقًا بالتخزين المحلي
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ الإعدادات")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onOpenAccessibilitySettings
                ) {
                    Text("إعداد خدمة الوصول Accessibility")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الإعداد الحالي: السعر ≥ $minPrice د.أ  •  الوقت ≤ $maxTime دقائق",
                    fontSize = 14.sp
                )
            }
        }
    }
}
