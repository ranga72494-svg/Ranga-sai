package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisState
import com.example.data.model.LanguageMode
import com.example.ui.components.ArcReactorView
import com.example.ui.components.AudioWaveformBar
import com.example.ui.components.TelemetryPill
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
fun HomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val jarvisState by viewModel.jarvisState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val isMuted by viewModel.isVoiceMuted.collectAsState()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBackground)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top HUD Header
        TopHudHeader(
            languageMode = languageMode,
            onLanguageSelected = { viewModel.setLanguage(it) },
            isMuted = isMuted,
            onToggleMute = { viewModel.toggleVoiceMute() },
            isWakeWordActive = isWakeWordActive,
            onToggleWakeWord = { viewModel.toggleWakeWord() }
        )

        // Telemetry Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TelemetryPill(
                icon = Icons.Default.BatteryChargingFull,
                label = "BAT",
                value = "${telemetry.batteryPercent}%",
                statusColor = if (telemetry.batteryPercent > 20) JarvisEmerald else JarvisGold
            )
            TelemetryPill(
                icon = Icons.Default.Wifi,
                label = "LINK",
                value = if (telemetry.isWifiEnabled) "ONLINE" else "OFFLINE",
                statusColor = if (telemetry.isWifiEnabled) JarvisCyan else JarvisGold
            )
            TelemetryPill(
                icon = Icons.Default.GraphicEq,
                label = "CORE",
                value = "OPTIMAL",
                statusColor = JarvisCyan
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Center Arc Reactor Visualizer
        Box(
            modifier = Modifier.padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            ArcReactorView(
                size = 170.dp,
                state = jarvisState,
                audioLevel = audioLevel
            )
        }

        // Live Equalizer Audio Waveform
        AudioWaveformBar(
            modifier = Modifier.padding(vertical = 4.dp),
            isActive = jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.SPEAKING,
            audioLevel = audioLevel,
            maxHeight = 22.dp
        )

        // Quick Suggestion Chips
        QuickActionChips(
            language = languageMode,
            onCommand = { cmd -> viewModel.sendUserMessage(cmd) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Transcript / Chat Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_transcript_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onExecuteAction = { actionType, data ->
                        viewModel.executeAction(actionType, data)
                    }
                )
            }
        }

        // Bottom Command Console
        BottomCommandBar(
            inputText = inputText,
            onTextChanged = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendUserMessage(inputText)
                    inputText = ""
                }
            },
            isListening = jarvisState == JarvisState.LISTENING,
            onToggleListening = {
                if (jarvisState == JarvisState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            }
        )
    }
}

@Composable
private fun TopHudHeader(
    languageMode: LanguageMode,
    onLanguageSelected: (LanguageMode) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    isWakeWordActive: Boolean,
    onToggleWakeWord: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title Branding
        Column {
            Text(
                text = "J.A.R.V.I.S.",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = JarvisCyan
            )
            Text(
                text = "MARK VII NEURAL INTERFACE",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Top Controls: Language Toggle & Mute & Wake Word
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Language Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisDarkSurfaceVariant)
                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        val nextLang = when (languageMode) {
                            LanguageMode.ENGLISH -> LanguageMode.TELUGU
                            LanguageMode.TELUGU -> LanguageMode.AUTO
                            LanguageMode.AUTO -> LanguageMode.ENGLISH
                        }
                        onLanguageSelected(nextLang)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (languageMode) {
                        LanguageMode.ENGLISH -> "ENG"
                        LanguageMode.TELUGU -> "తెలుగు"
                        LanguageMode.AUTO -> "AUTO"
                    },
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = JarvisGold
                )
            }

            // Audio Mute Toggle
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(36.dp)
                    .background(JarvisDarkSurfaceVariant, CircleShape)
                    .border(1.dp, JarvisCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Mute Voice",
                    tint = if (isMuted) TextMuted else JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionChips(
    language: LanguageMode,
    onCommand: (String) -> Unit
) {
    val chips = if (language == LanguageMode.TELUGU) {
        listOf(
            "సిస్టమ్ స్టేటస్",
            "లైట్ వెలిగించు",
            "లైట్ ఆపు",
            "శబ్దం పెంచు",
            "వైఫై సెట్టింగ్స్",
            "అలారం పెట్టు",
            "వాతావరణం ఎలా ఉంది?"
        )
    } else {
        listOf(
            "System Diagnostics",
            "Turn on Flashlight",
            "Turn off Flashlight",
            "Increase Volume",
            "Wi-Fi Controls",
            "Set Alarm for 7 AM",
            "Weather Report"
        )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(chips) { chip ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisDarkSurfaceVariant.copy(alpha = 0.8f))
                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp))
                    .clickable { onCommand(chip) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chip,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onExecuteAction: (ActionType, String?) -> Unit
) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Sender Tag
        Text(
            text = if (isUser) "BOSS" else "J.A.R.V.I.S.",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = if (isUser) JarvisGold else JarvisCyan,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Bubble Content
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) JarvisDarkSurfaceVariant else JarvisDarkSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) JarvisCardBorder else JarvisCyan.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )

                // Optional Action Trigger Button
                if (message.actionType != ActionType.NONE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(
                        onClick = { onExecuteAction(message.actionType, message.actionData) },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JarvisDarkBackground,
                            contentColor = JarvisCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Execute Action",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXECUTE: ${message.actionType.name.replace("_", " ")}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomCommandBar(
    inputText: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onToggleListening: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Voice Mic Button with glowing reactive ring
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isListening)
                        Brush.radialGradient(listOf(JarvisEmerald, Color(0xFF004D26)))
                    else
                        Brush.radialGradient(listOf(JarvisCyan, Color(0xFF003847)))
                )
                .clickable { onToggleListening() }
                .border(2.dp, if (isListening) JarvisEmerald else JarvisCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = JarvisDarkBackground,
                modifier = Modifier.size(26.dp)
            )
        }

        // Text Input Field
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChanged,
            placeholder = {
                Text(
                    text = "Command Jarvis...",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 15.sp,
                    color = TextMuted
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = JarvisDarkSurfaceVariant,
                unfocusedContainerColor = JarvisDarkSurface,
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = JarvisCyan
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("command_input_field")
        )

        // Send Button
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank(),
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (inputText.isNotBlank()) JarvisCyan else JarvisDarkSurfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send Command",
                tint = if (inputText.isNotBlank()) JarvisDarkBackground else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
