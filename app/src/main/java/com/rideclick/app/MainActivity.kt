package com.rideclick.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = SettingsRepository(this)
        setContent { RideClickApp(repo) }
    }
}

@Composable
fun RideClickApp(repo: SettingsRepository) {
    var rules by remember { mutableStateOf(repo.loadRules()) }
    var monitor by remember { mutableStateOf(false) }

    MaterialTheme {
        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("RideClick", style = MaterialTheme.typography.headlineMedium)
                    Text("فلترة أذكى. قبول أسرع.", style = MaterialTheme.typography.titleMedium)
                    Text("نسخة Android الأولى: مراقبة وتحليل فقط، بدون قبول تلقائي.")

                    HorizontalDivider()

                    Text("شروط القبول", style = MaterialTheme.typography.titleLarge)
                    SettingNumber("الحد الأدنى للسعر", rules.minPrice, "د.أ") {
                        rules = rules.copy(minPrice = it); repo.saveRules(rules)
                    }
                    SettingNumber("أقصى ETA", rules.maxEta.toDouble(), "دقائق") {
                        rules = rules.copy(maxEta = it.toInt()); repo.saveRules(rules)
                    }
                    SettingNumber("أقصى مسافة", rules.maxDistance, "كم") {
                        rules = rules.copy(maxDistance = it); repo.saveRules(rules)
                    }
                    SettingNumber("الحد الأدنى للسعر/كم", rules.minPricePerKm, "د.أ/كم") {
                        rules = rules.copy(minPricePerKm = it); repo.saveRules(rules)
                    }

                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("وضع المراقبة")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = monitor, onCheckedChange = { monitor = it })
                    }

                    Button(
                        onClick = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("فتح إعدادات Accessibility")
                    }

                    Text(
                        "ملاحظة: لا يتم تنفيذ أي ضغط أو قبول تلقائي في هذه النسخة. يجب اختبار قراءة واجهات التطبيقات الحقيقية أولاً.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingNumber(
    label: String,
    value: Double,
    suffix: String,
    onChange: (Double) -> Unit
) {
    var text by remember(value) {
        mutableStateOf(String.format(Locale.US, "%.2f", value))
    }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.replace(',', '.').toDoubleOrNull()?.let(onChange)
        },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
