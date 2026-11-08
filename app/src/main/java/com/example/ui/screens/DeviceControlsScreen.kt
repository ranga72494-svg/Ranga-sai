package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun DeviceControlsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isTorchOn by viewModel.isFlashlightOn.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = "DEVICE CONTROL MATRIX",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tactical hardware switches & system overrides",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Master Volume Card with Slider & Quick Adjust
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Audio Telemetry",
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASTER AUDIO ATTENUATION",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${telemetry.volumePercent}%",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = JarvisGold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = telemetry.volumePercent.toFloat(),
                        onValueChange = { percent ->
                            viewModel.setVolume(percent.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.executeAction(com.example.data.model.ActionType.VOLUME_DOWN) },
                            modifier = Modifier
                                .background(JarvisDarkSurfaceVariant, CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(Icons.Default.VolumeDown, contentDescription = "Volume Down", tint = TextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisDarkSurfaceVariant)
                                .clickable { viewModel.executeAction(com.example.data.model.ActionType.VOLUME_MUTE) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeMute, contentDescription = "Mute", tint = JarvisGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MUTE ALL", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JarvisGold)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.executeAction(com.example.data.model.ActionType.VOLUME_UP) },
                            modifier = Modifier
                                .background(JarvisDarkSurfaceVariant, CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Volume Up", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Flashlight Switch Card
        item {
            ControlCard(
                title = "TACTICAL OPTICS (FLASHLIGHT)",
                subtitle = if (isTorchOn) "Emitter Active • High Beam" else "Emitter Inactive",
                icon = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                iconColor = if (isTorchOn) JarvisGold else TextMuted,
                trailingContent = {
                    Switch(
                        checked = isTorchOn,
                        onCheckedChange = { viewModel.toggleFlashlight() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisGold,
                            checkedTrackColor = Color(0xFF4A3B00),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = JarvisDarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("flashlight_switch")
                    )
                }
            )
        }

        // Wi-Fi Quick Access
        item {
            ControlCard(
                title = "WI-FI WIRELESS INTERFACE",
                subtitle = telemetry.networkType,
                icon = Icons.Default.Wifi,
                iconColor = JarvisCyan,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisDarkSurfaceVariant)
                            .clickable { viewModel.deviceManager.openWifiSettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("CONFIGURE", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = JarvisCyan)
                    }
                }
            )
        }

        // Bluetooth Quick Access
        item {
            ControlCard(
                title = "BLUETOOTH TRANSCEIVER",
                subtitle = "Short-range wireless comms",
                icon = Icons.Default.Bluetooth,
                iconColor = JarvisCyan,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisDarkSurfaceVariant)
                            .clickable { viewModel.deviceManager.openBluetoothSettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("DEVICES", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = JarvisCyan)
                    }
                }
            )
        }

        // Hotspot & Tethering
        item {
            ControlCard(
                title = "PORTABLE HOTSPOT & TETHERING",
                subtitle = "Wireless access point protocol",
                icon = Icons.Default.WifiTethering,
                iconColor = JarvisEmerald,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisDarkSurfaceVariant)
                            .clickable { viewModel.deviceManager.openHotspotSettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("TETHER", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = JarvisEmerald)
                    }
                }
            )
        }

        // Airplane Mode
        item {
            ControlCard(
                title = "AIRPLANE MODE & RF SHIELDING",
                subtitle = "Disconnect all radio frequency comms",
                icon = Icons.Default.AirplanemodeActive,
                iconColor = JarvisGold,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisDarkSurfaceVariant)
                            .clickable { viewModel.deviceManager.openAirplaneModeSettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("MANAGE", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = JarvisGold)
                    }
                }
            )
        }

        // Display & Brightness Settings
        item {
            ControlCard(
                title = "DISPLAY & SCREEN TIMEOUT",
                subtitle = "Manage lock screen and visual lumens",
                icon = Icons.Default.BrightnessHigh,
                iconColor = JarvisCyan,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisDarkSurfaceVariant)
                            .clickable { viewModel.deviceManager.openDisplaySettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("DISPLAY", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = JarvisCyan)
                    }
                }
            )
        }

        // Chronometer & Alarms
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.deviceManager.setAlarm(7, 0) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = "Alarm", tint = JarvisCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("SET ALARM", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                        Text("07:00 AM", fontFamily = RajdhaniFontFamily, fontSize = 12.sp, color = JarvisGold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.deviceManager.setTimer(300) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = JarvisEmerald, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("COUNTDOWN", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                        Text("5 Minutes", fontFamily = RajdhaniFontFamily, fontSize = 12.sp, color = JarvisEmerald)
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    trailingContent: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(JarvisDarkSurfaceVariant, CircleShape)
                        .border(1.dp, JarvisCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            trailingContent()
        }
    }
}
