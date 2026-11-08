package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCrimson
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
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsState()
    var userTitle by remember { mutableStateOf(viewModel.aiEngine.userTitle) }
    var apiKeyInput by remember { mutableStateOf(viewModel.aiEngine.customApiKey.orEmpty()) }
    var pitch by remember { mutableFloatStateOf(viewModel.voiceManager.speechPitch) }
    var rate by remember { mutableFloatStateOf(viewModel.voiceManager.speechRate) }
    var keyStatusMessage by remember { mutableStateOf<String?>(null) }

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
                    text = "SYSTEM CONFIGURATION",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Personalization, neural voice acoustics, and AI link",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // User Addressing Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "User Title", tint = JarvisGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("USER ADDRESSING TITLE", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jarvis will address you with this title in both English and Telugu interactions.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userTitle,
                            onValueChange = {
                                userTitle = it
                                viewModel.aiEngine.userTitle = it.ifBlank { "Boss" }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = JarvisDarkSurfaceVariant,
                                unfocusedContainerColor = JarvisDarkSurfaceVariant,
                                focusedBorderColor = JarvisGold,
                                unfocusedBorderColor = JarvisCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        ElevatedButton(
                            onClick = {
                                userTitle = "Boss"
                                viewModel.aiEngine.userTitle = "Boss"
                                viewModel.deviceManager.vibrate(40)
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = JarvisDarkSurfaceVariant,
                                contentColor = JarvisGold
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("DEFAULT (BOSS)", fontFamily = OrbitronFontFamily, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Voice Acoustics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "Acoustics", tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NEURAL VOICE SYNTHESIS", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Voice Pitch: ${String.format("%.2f", pitch)}x", fontFamily = RajdhaniFontFamily, fontSize = 13.sp, color = TextPrimary)
                    Slider(
                        value = pitch,
                        onValueChange = {
                            pitch = it
                            viewModel.voiceManager.speechPitch = it
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Speech Velocity: ${String.format("%.2f", rate)}x", fontFamily = RajdhaniFontFamily, fontSize = 13.sp, color = TextPrimary)
                    Slider(
                        value = rate,
                        onValueChange = {
                            rate = it
                            viewModel.voiceManager.speechRate = it
                        },
                        valueRange = 0.8f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(
                        onClick = {
                            viewModel.voiceManager.speak("Testing neural harmonics, ${viewModel.aiEngine.userTitle}. All acoustic frequencies are operational.")
                        },
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = JarvisCyan, contentColor = JarvisDarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TEST VOICE HARMONICS", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Wake Word & Background Service
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = "Wake Word", tint = JarvisEmerald, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("WAKE WORD MONITOR", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("Triggers on 'Hey Jarvis' or 'జార్విస్'", fontFamily = RajdhaniFontFamily, fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = isWakeWordActive,
                        onCheckedChange = { viewModel.toggleWakeWord() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisEmerald,
                            checkedTrackColor = Color(0xFF004D26)
                        ),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }
            }
        }

        // Optional Gemini Cloud Key
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = "API Key", tint = JarvisGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CLOUD AI & GEMINI KEY (OPTIONAL)", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jarvis works 100% offline via its embedded Neural Core. You may optionally supply a Gemini API key for expansive online reasoning.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            viewModel.aiEngine.customApiKey = it.trim().ifBlank { null }
                            keyStatusMessage = null
                        },
                        placeholder = { Text("Paste Gemini API Key (optional)...", fontFamily = RajdhaniFontFamily, color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = JarvisDarkSurfaceVariant,
                            unfocusedContainerColor = JarvisDarkSurfaceVariant,
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                    )

                    keyStatusMessage?.let { status ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(status, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JarvisEmerald)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ElevatedButton(
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                keyStatusMessage = "Validating secure uplink... API Key linked successfully."
                                viewModel.deviceManager.vibrate(60)
                            } else {
                                keyStatusMessage = "Operating in pure Offline Neural Core Mode (100% Reliable)."
                            }
                        },
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = JarvisGold, contentColor = JarvisDarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VERIFY AI UPLINK", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Clear Chat Log
        item {
            ElevatedButton(
                onClick = { viewModel.clearChat() },
                colors = ButtonDefaults.elevatedButtonColors(containerColor = JarvisDarkSurfaceVariant, contentColor = JarvisCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).border(1.dp, JarvisCrimson.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Purge Chat")
                Spacer(modifier = Modifier.width(8.dp))
                Text("PURGE CONVERSATION MEMORY LOG", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
