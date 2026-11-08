package com.example.ui.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.JarvisAiEngine
import com.example.data.controller.DeviceManager
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisState
import com.example.data.model.LanguageMode
import com.example.data.model.SystemTelemetry
import com.example.data.voice.VoiceManager
import com.example.service.JarvisService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val deviceManager = DeviceManager(application)
    val aiEngine = JarvisAiEngine()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _languageMode = MutableStateFlow(LanguageMode.ENGLISH)
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _isWakeWordActive = MutableStateFlow(true)
    val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    val voiceManager: VoiceManager by lazy {
        VoiceManager(
            context = getApplication(),
            onSpeechRecognized = { text ->
                sendUserMessage(text)
            },
            onWakeWordDetected = {
                deviceManager.vibrate(100)
                _jarvisState.value = JarvisState.LISTENING
            }
        )
    }

    init {
        refreshTelemetry()
        // Welcome message from Jarvis addressing user as Boss
        val welcome = ChatMessage(
            text = "Systems online, Boss. J.A.R.V.I.S. core v4.2 is fully operational. How may I assist you today?",
            isFromUser = false,
            language = LanguageMode.ENGLISH
        )
        _chatMessages.value = listOf(welcome)

        // Observe voice manager speaking state
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _jarvisState.value = JarvisState.SPEAKING
                } else if (_jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }

        // Observe voice manager audio levels
        viewModelScope.launch {
            voiceManager.audioLevel.collect { level ->
                _audioLevel.value = level
            }
        }

        // Periodically refresh telemetry
        viewModelScope.launch {
            while (true) {
                delay(10000)
                refreshTelemetry()
            }
        }
    }

    fun refreshTelemetry() {
        val t = deviceManager.getTelemetry()
        _telemetry.value = t
        _isFlashlightOn.value = t.isFlashlightOn
    }

    fun setLanguage(mode: LanguageMode) {
        _languageMode.value = mode
        voiceManager.setLanguage(mode)
        deviceManager.vibrate(40)

        val confirmText = if (mode == LanguageMode.TELUGU)
            "బాస్, భాషను తెలుగుగా మార్చాను. నేను మీ ఆదేశాలకు సిద్ధంగా ఉన్నాను."
        else
            "Language protocol adjusted to ${mode.displayName}, Boss. Standing by."

        val msg = ChatMessage(text = confirmText, isFromUser = false, language = mode)
        _chatMessages.value = _chatMessages.value + msg
        speakIfEnabled(confirmText, mode)
    }

    fun toggleVoiceMute() {
        val newMute = !_isVoiceMuted.value
        _isVoiceMuted.value = newMute
        voiceManager.isVoiceMuted = newMute
        if (newMute) {
            voiceManager.stopSpeaking()
        }
        deviceManager.vibrate(40)
    }

    fun toggleWakeWord() {
        val active = !_isWakeWordActive.value
        _isWakeWordActive.value = active
        if (active) {
            JarvisService.startService(getApplication())
        } else {
            JarvisService.stopService(getApplication())
        }
        deviceManager.vibrate(40)
    }

    fun startListening() {
        _jarvisState.value = JarvisState.LISTENING
        deviceManager.vibrate(60)
        voiceManager.startListening(_languageMode.value)
    }

    fun stopListening() {
        voiceManager.stopListening()
        if (_jarvisState.value == JarvisState.LISTENING) {
            _jarvisState.value = JarvisState.IDLE
        }
    }

    fun sendUserMessage(text: String) {
        val query = text.trim()
        if (query.isBlank()) return

        stopListening()

        val userMsg = ChatMessage(
            text = query,
            isFromUser = true,
            language = _languageMode.value
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _jarvisState.value = JarvisState.PROCESSING

        viewModelScope.launch {
            val result = aiEngine.processQuery(
                prompt = query,
                languageMode = _languageMode.value,
                batteryPercent = _telemetry.value.batteryPercent,
                networkType = _telemetry.value.networkType
            )

            // Execute action if parsed
            if (result.actionType != ActionType.NONE) {
                executeAction(result.actionType, result.actionData)
            }

            val replyMsg = ChatMessage(
                text = result.textReply,
                isFromUser = false,
                language = result.language,
                actionType = result.actionType,
                actionData = result.actionData
            )
            _chatMessages.value = _chatMessages.value + replyMsg
            _jarvisState.value = JarvisState.IDLE

            speakIfEnabled(result.textReply, result.language)
            refreshTelemetry()
        }
    }

    fun executeAction(actionType: ActionType, data: String? = null) {
        when (actionType) {
            ActionType.FLASHLIGHT_ON -> {
                deviceManager.setFlashlight(true)
                _isFlashlightOn.value = true
            }
            ActionType.FLASHLIGHT_OFF -> {
                deviceManager.setFlashlight(false)
                _isFlashlightOn.value = false
            }
            ActionType.VOLUME_UP -> {
                deviceManager.adjustVolume(increase = true)
                refreshTelemetry()
            }
            ActionType.VOLUME_DOWN -> {
                deviceManager.adjustVolume(increase = false)
                refreshTelemetry()
            }
            ActionType.VOLUME_MUTE -> {
                deviceManager.muteVolume()
                refreshTelemetry()
            }
            ActionType.OPEN_WIFI -> deviceManager.openWifiSettings()
            ActionType.OPEN_BLUETOOTH -> deviceManager.openBluetoothSettings()
            ActionType.OPEN_HOTSPOT -> deviceManager.openHotspotSettings()
            ActionType.OPEN_AIRPLANE -> deviceManager.openAirplaneModeSettings()
            ActionType.OPEN_DISPLAY_SETTINGS -> deviceManager.openDisplaySettings()
            ActionType.MAKE_CALL -> {
                val number = data ?: "911"
                deviceManager.makeCall(number)
            }
            ActionType.SEND_SMS -> {
                deviceManager.sendSms(phoneNumber = "", messageText = data ?: "")
            }
            ActionType.SET_ALARM -> deviceManager.setAlarm(hour = 7, minutes = 0)
            ActionType.SET_TIMER -> deviceManager.setTimer(seconds = 300)
            ActionType.RUN_DIAGNOSTICS -> refreshTelemetry()
            else -> {}
        }
    }

    fun toggleFlashlight() {
        val newState = deviceManager.toggleFlashlight()
        _isFlashlightOn.value = newState
        deviceManager.vibrate(40)
    }

    fun setVolume(percent: Int) {
        deviceManager.setVolumePercent(percent)
        refreshTelemetry()
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
        deviceManager.vibrate(40)
    }

    private fun speakIfEnabled(text: String, language: LanguageMode) {
        if (!_isVoiceMuted.value) {
            voiceManager.speak(text, language)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
