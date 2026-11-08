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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class QuickContact(val name: String, val role: String, val number: String, val color: Color)

@Composable
fun CommunicationsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var dialNumber by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsText by remember { mutableStateOf("") }

    val quickContacts = listOf(
        QuickContact("Emergency Services", "SOS Hotline", "911", JarvisCrimson),
        QuickContact("HQ Control", "Tactical Ops", "18005550199", JarvisCyan),
        QuickContact("Primary Line", "Secure Uplink", "1234567890", JarvisGold),
        QuickContact("Tech Support", "Jarvis Diagnostics", "18002738255", JarvisEmerald)
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
                    text = "COMMUNICATIONS CONSOLE",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Encrypted voice calling & SMS frequency matrix",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Voice Dial Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dialpad, contentDescription = "Dialer", tint = JarvisCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VOICE CALL TRANSMITTER",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dialNumber,
                            onValueChange = { dialNumber = it },
                            placeholder = { Text("Enter telephone number...", fontFamily = RajdhaniFontFamily, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = JarvisDarkSurfaceVariant,
                                unfocusedContainerColor = JarvisDarkSurfaceVariant,
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = JarvisCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dial_number_input")
                        )

                        ElevatedButton(
                            onClick = {
                                if (dialNumber.isNotBlank()) {
                                    viewModel.deviceManager.makeCall(dialNumber)
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = JarvisCyan,
                                contentColor = JarvisDarkBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("call_action_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CALL", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SMS Transmitter Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Message, contentDescription = "SMS", tint = JarvisGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMS DISPATCH TERMINAL",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = smsNumber,
                        onValueChange = { smsNumber = it },
                        placeholder = { Text("Recipient Number / Name...", fontFamily = RajdhaniFontFamily, color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = JarvisDarkSurfaceVariant,
                            unfocusedContainerColor = JarvisDarkSurfaceVariant,
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = smsText,
                        onValueChange = { smsText = it },
                        placeholder = { Text("Compose encrypted dispatch message...", fontFamily = RajdhaniFontFamily, color = TextMuted) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = JarvisDarkSurfaceVariant,
                            unfocusedContainerColor = JarvisDarkSurfaceVariant,
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ElevatedButton(
                        onClick = {
                            viewModel.deviceManager.sendSms(smsNumber, smsText)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = JarvisGold,
                            contentColor = JarvisDarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send SMS")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRANSMIT SMS", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Speed Dial Targets
        item {
            Text(
                text = "TACTICAL SPEED-DIAL CHANNELS",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(quickContacts) { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.deviceManager.makeCall(contact.number) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(JarvisDarkSurfaceVariant, CircleShape)
                                .border(1.dp, contact.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (contact.number == "911") Icons.Default.Warning else Icons.Default.Call,
                                contentDescription = contact.name,
                                tint = contact.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = contact.name,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${contact.role} • ${contact.number}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(contact.color.copy(alpha = 0.15f))
                            .border(1.dp, contact.color, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "DIAL",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = contact.color
                        )
                    }
                }
            }
        }
    }
}
