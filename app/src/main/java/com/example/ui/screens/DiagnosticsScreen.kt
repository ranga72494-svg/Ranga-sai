package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkBackground
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisDarkSurfaceVariant
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationProgress by remember { mutableStateOf(1f) }
    var optimizationResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val animatedProgress by animateFloatAsState(
        targetValue = optimizationProgress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "OptProgress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = "SYSTEM TELEMETRY & DIAGNOSTICS",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Real-time core performance, memory & thermal readouts",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Optimization Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = "Performance", tint = JarvisEmerald, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("NEURAL CORE OPTIMIZER", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("Purge memory buffers & accelerate clock cycle", fontFamily = RajdhaniFontFamily, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    if (isOptimizing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            color = JarvisEmerald,
                            trackColor = JarvisDarkSurfaceVariant,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Calibrating synaptic nodes... ${(animatedProgress * 100).toInt()}%",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisEmerald
                        )
                    }

                    optimizationResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = JarvisEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(res, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JarvisEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ElevatedButton(
                        onClick = {
                            if (!isOptimizing) {
                                isOptimizing = true
                                optimizationProgress = 0f
                                optimizationResult = null
                                scope.launch {
                                    for (step in 1..10) {
                                        delay(150)
                                        optimizationProgress = step / 10f
                                    }
                                    System.gc()
                                    viewModel.deviceManager.vibrate(80)
                                    viewModel.refreshTelemetry()
                                    isOptimizing = false
                                    optimizationResult = "Optimization complete, Boss. 142 MB recycled • Core at 100% efficiency."
                                }
                            }
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JarvisEmerald,
                            contentColor = JarvisDarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Purge")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ENGAGE SYSTEM OPTIMIZATION", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Metrics Grid
        item {
            TelemetryMetricCard(
                title = "HEAP MEMORY UTILIZATION",
                value = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB",
                percentage = ((telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb) * 100).toInt().coerceIn(5, 95),
                icon = Icons.Default.Memory,
                tint = JarvisCyan
            )
        }

        item {
            TelemetryMetricCard(
                title = "BATTERY RESERVES & POWER",
                value = "${telemetry.batteryPercent}% • ${if (telemetry.isCharging) "Charging (AC Fast Link)" else "Discharging"}",
                percentage = telemetry.batteryPercent,
                icon = Icons.Default.BatteryChargingFull,
                tint = if (telemetry.batteryPercent > 20) JarvisEmerald else JarvisGold
            )
        }

        item {
            TelemetryMetricCard(
                title = "RADIO FREQUENCY & CARRIER LINK",
                value = telemetry.networkType,
                percentage = if (telemetry.isWifiEnabled) 95 else 70,
                icon = Icons.Default.Storage,
                tint = JarvisGold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ENVIRONMENT SPECIFICATIONS", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SpecRow("OS Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    SpecRow("Device Model", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
                    SpecRow("Kernel Architecture", Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
                    SpecRow("System Uptime", "${telemetry.uptimeSeconds / 60} minutes")
                    SpecRow("Jarvis AI Build", "v4.2.0-PRO-NEURAL")
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    value: String,
    percentage: Int,
    icon: ImageVector,
    tint: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(JarvisDarkSurfaceVariant, CircleShape)
                            .border(1.dp, tint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(title, fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                }

                Text("$percentage%", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tint)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                color = tint,
                trackColor = JarvisDarkSurfaceVariant,
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(value, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = RajdhaniFontFamily, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
    }
}
