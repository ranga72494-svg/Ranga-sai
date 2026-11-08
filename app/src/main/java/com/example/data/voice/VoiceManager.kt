package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onWakeWordDetected: () -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    var speechPitch: Float = 0.95f
    var speechRate: Float = 1.05f
    var isVoiceMuted: Boolean = false
    var currentLanguage: LanguageMode = LanguageMode.ENGLISH

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to initialize TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupTtsLanguage(currentLanguage)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioLevel.value = 0f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioLevel.value = 0f
                }
            })
        } else {
            Log.e("VoiceManager", "TTS initialization failed with code $status")
        }
    }

    fun setLanguage(language: LanguageMode) {
        currentLanguage = language
        setupTtsLanguage(language)
    }

    private fun setupTtsLanguage(language: LanguageMode) {
        if (!isTtsReady || tts == null) return
        try {
            when (language) {
                LanguageMode.TELUGU -> {
                    val teluguLocale = Locale("te", "IN")
                    val result = tts?.setLanguage(teluguLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("VoiceManager", "Telugu TTS not supported natively on this device, using fallback")
                    }
                }
                else -> {
                    val result = tts?.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                }
            }
            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechRate)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error setting language: ${e.message}")
        }
    }

    fun speak(text: String, language: LanguageMode = currentLanguage) {
        if (isVoiceMuted || !isTtsReady || text.isBlank()) return
        try {
            setupTtsLanguage(language)
            val cleanText = text.replace(Regex("[*#_`~]"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS speak failed: ${e.message}")
            _isSpeaking.value = false
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _audioLevel.value = 0f
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS stop failed: ${e.message}")
        }
    }

    fun startListening(language: LanguageMode = currentLanguage) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("VoiceManager", "Speech recognition not available on device")
            return
        }

        stopSpeaking()
        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                    }

                    override fun onBeginningOfSpeech() {
                        _audioLevel.value = 0.5f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB to 10dB to 0..1 range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                        _audioLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        _audioLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        _audioLevel.value = 0f
                        _partialTranscript.value = ""
                        Log.w("VoiceManager", "Speech recognition error code: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        _audioLevel.value = 0f
                        _partialTranscript.value = ""
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            checkWakeWordAndDispatch(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull().orEmpty()
                        _partialTranscript.value = partial
                        if (containsWakeWord(partial)) {
                            onWakeWordDetected()
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val langCode = when (language) {
                LanguageMode.TELUGU -> "te-IN"
                else -> "en-US"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("VoiceManager", "Start listening failed: ${e.message}")
            isListening = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
            _audioLevel.value = 0f
            _partialTranscript.value = ""
        } catch (e: Exception) {
            Log.e("VoiceManager", "Stop listening error: ${e.message}")
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return lower.contains("hey jarvis") ||
                lower.contains("jarvis") ||
                lower.contains("జార్విస్")
    }

    private fun checkWakeWordAndDispatch(text: String) {
        val lower = text.lowercase(Locale.ROOT)
        if (containsWakeWord(lower)) {
            onWakeWordDetected()
        }
        onSpeechRecognized(text)
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS shutdown error: ${e.message}")
        }
    }
}
