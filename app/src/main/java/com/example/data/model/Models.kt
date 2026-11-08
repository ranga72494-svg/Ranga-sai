package com.example.data.model

import java.util.UUID

enum class LanguageMode(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    TELUGU("తెలుగు (Telugu)", "te"),
    AUTO("Auto Detect", "auto")
}

enum class JarvisState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    OFFLINE
}

enum class ActionType {
    NONE,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    OPEN_WIFI,
    OPEN_BLUETOOTH,
    OPEN_HOTSPOT,
    OPEN_AIRPLANE,
    OPEN_DISPLAY_SETTINGS,
    SET_ALARM,
    SET_TIMER,
    MAKE_CALL,
    SEND_SMS,
    OPEN_WEATHER,
    RUN_DIAGNOSTICS
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: LanguageMode = LanguageMode.ENGLISH,
    val actionType: ActionType = ActionType.NONE,
    val actionData: String? = null
)

data class SystemTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val batteryTemp: Float = 28.5f,
    val networkType: String = "Wi-Fi (Online)",
    val isWifiEnabled: Boolean = true,
    val isBluetoothEnabled: Boolean = true,
    val isFlashlightOn: Boolean = false,
    val volumePercent: Int = 70,
    val ramUsedMb: Long = 180,
    val ramTotalMb: Long = 512,
    val uptimeSeconds: Long = 0,
    val coreStatus: String = "ACTIVE - ONLINE"
)

data class VoiceCommandResult(
    val textReply: String,
    val actionType: ActionType = ActionType.NONE,
    val actionData: String? = null,
    val language: LanguageMode = LanguageMode.ENGLISH
)
