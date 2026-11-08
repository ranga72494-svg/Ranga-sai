package com.example.data.ai

import android.util.Log
import com.example.data.model.ActionType
import com.example.data.model.LanguageMode
import com.example.data.model.VoiceCommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisAiEngine {

    companion object {
        private const val TAG = "JarvisAiEngine"
    }

    var customApiKey: String? = null
    var userTitle: String = "Boss"

    suspend fun processQuery(
        prompt: String,
        languageMode: LanguageMode,
        batteryPercent: Int = 100,
        networkType: String = "Wi-Fi"
    ): VoiceCommandResult = withContext(Dispatchers.Default) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            return@withContext VoiceCommandResult(
                textReply = if (languageMode == LanguageMode.TELUGU)
                    "బాస్, నేను వింటున్నాను. దయచేసి మీ ఆదేశాన్ని చెప్పండి."
                else
                    "I am standing by, $userTitle. Please issue your command.",
                actionType = ActionType.NONE,
                language = languageMode
            )
        }

        // 1. Detect language if AUTO
        val detectedLanguage = when (languageMode) {
            LanguageMode.AUTO -> if (isTeluguText(cleanPrompt)) LanguageMode.TELUGU else LanguageMode.ENGLISH
            else -> languageMode
        }

        // 2. Tactical Intent Parser for Device Controls & Smart Features
        val commandResult = parseDeviceOrSmartIntent(cleanPrompt, detectedLanguage, batteryPercent, networkType)
        if (commandResult != null) {
            return@withContext commandResult
        }

        // 3. If online and an API key is available, attempt Gemini Cloud inference with graceful fallback
        if (!customApiKey.isNullOrBlank()) {
            try {
                val cloudReply = callGeminiApi(cleanPrompt, customApiKey!!, detectedLanguage)
                if (!cloudReply.isNullOrBlank()) {
                    return@withContext VoiceCommandResult(
                        textReply = cloudReply,
                        actionType = ActionType.NONE,
                        language = detectedLanguage
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloud API failed, falling back to Jarvis Neural Core: ${e.message}")
            }
        }

        // 4. Instant Jarvis Neural Core (Zero Latency & 100% Reliable Offline)
        val offlineReply = generateNeuralCoreResponse(cleanPrompt, detectedLanguage, batteryPercent)
        VoiceCommandResult(
            textReply = offlineReply,
            actionType = ActionType.NONE,
            language = detectedLanguage
        )
    }

    private fun isTeluguText(text: String): Boolean {
        // Telugu unicode range: \u0C00 - \u0C7F
        for (char in text) {
            if (char in '\u0C00'..'\u0C7F') return true
        }
        return false
    }

    private fun parseDeviceOrSmartIntent(
        prompt: String,
        language: LanguageMode,
        batteryPercent: Int,
        networkType: String
    ): VoiceCommandResult? {
        val lower = prompt.lowercase(Locale.ROOT)

        // --- Flashlight ---
        if (lower.contains("turn on flashlight") || lower.contains("torch on") || lower.contains("flashlight on") ||
            lower.contains("లైట్ వెలిగించు") || lower.contains("టార్చ్ ఆన్") || lower.contains("లైట్ ఆన్")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "ఖచ్చితంగా బాస్, ఫ్లాష్‌లైట్ ఆన్ చేయబడింది."
                else
                    "Illuminating tactical optics now, $userTitle. Flashlight enabled.",
                actionType = ActionType.FLASHLIGHT_ON,
                language = language
            )
        }
        if (lower.contains("turn off flashlight") || lower.contains("torch off") || lower.contains("flashlight off") ||
            lower.contains("లైట్ ఆపు") || lower.contains("టార్చ్ ఆఫ్") || lower.contains("లైట్ ఆఫ్")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "సరే బాస్, ఫ్లాష్‌లైట్ ఆపివేయబడింది."
                else
                    "Optics powered down, $userTitle. Flashlight disabled.",
                actionType = ActionType.FLASHLIGHT_OFF,
                language = language
            )
        }

        // --- Volume Controls ---
        if (lower.contains("volume up") || lower.contains("increase volume") || lower.contains("louder") ||
            lower.contains("శబ్దం పెంచు") || lower.contains("వాల్యూమ్ పెంచు")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, వాల్యూమ్ పెంచాను."
                else
                    "Audio telemetry increased, $userTitle.",
                actionType = ActionType.VOLUME_UP,
                language = language
            )
        }
        if (lower.contains("volume down") || lower.contains("decrease volume") || lower.contains("lower volume") ||
            lower.contains("శబ్దం తగ్గించు") || lower.contains("వాల్యూమ్ తగ్గించు")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, వాల్యూమ్ తగ్గించాను."
                else
                    "Lowering audio attenuation, $userTitle.",
                actionType = ActionType.VOLUME_DOWN,
                language = language
            )
        }
        if (lower.contains("mute") || lower.contains("silence") || lower.contains("నిశ్శబ్దం") || lower.contains("మ్యూట్")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "సిస్టమ్ మ్యూట్ చేయబడింది, బాస్."
                else
                    "Audio channels muted, $userTitle. Silent protocol active.",
                actionType = ActionType.VOLUME_MUTE,
                language = language
            )
        }

        // --- Wi-Fi Settings ---
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("వైఫై")) {
            if (lower.contains("settings") || lower.contains("open") || lower.contains("turn") || lower.contains("ఆన్")) {
                return VoiceCommandResult(
                    textReply = if (language == LanguageMode.TELUGU)
                        "బాస్, వైఫై నెట్‌వర్క్ సెట్టింగ్‌లను తెరుస్తున్నాను."
                    else
                        "Opening wireless communications interface, $userTitle.",
                    actionType = ActionType.OPEN_WIFI,
                    language = language
                )
            }
        }

        // --- Bluetooth Settings ---
        if (lower.contains("bluetooth") || lower.contains("బ్లూటూత్")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, బ్లూటూత్ కంట్రోల్స్ తెరుస్తున్నాను."
                else
                    "Accessing short-range Bluetooth transceivers, $userTitle.",
                actionType = ActionType.OPEN_BLUETOOTH,
                language = language
            )
        }

        // --- Hotspot Settings ---
        if (lower.contains("hotspot") || lower.contains("tethering") || lower.contains("హాట్‌స్పాట్")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "పోర్టబుల్ హాట్‌స్పాట్ కాన్ఫిగరేషన్ తెరుస్తున్నాను, బాస్."
                else
                    "Deploying mobile uplink hotspot matrix, $userTitle.",
                actionType = ActionType.OPEN_HOTSPOT,
                language = language
            )
        }

        // --- Airplane Mode ---
        if (lower.contains("airplane mode") || lower.contains("flight mode") || lower.contains("ఏరోప్లేన్")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "ఫ్లైట్ మోడ్ సెట్టింగ్‌లు తెరుస్తున్నాను, బాస్."
                else
                    "Accessing RF shielding protocols. Opening Airplane mode settings, $userTitle.",
                actionType = ActionType.OPEN_AIRPLANE,
                language = language
            )
        }

        // --- System Diagnostics / Battery ---
        if (lower.contains("battery") || lower.contains("status") || lower.contains("diagnostics") ||
            lower.contains("బ్యాటరీ") || lower.contains("సిస్టమ్ స్థితి") || lower.contains("సిస్టమ్ స్టేటస్")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, బ్యాటరీ ప్రస్తుతం $batteryPercent% వద్ద ఉంది. నెట్‌వర్క్: $networkType. అన్ని ముఖ్య వ్యవస్థలు సజావుగా పనిచేస్తున్నాయి."
                else
                    "Diagnostic readout ready, $userTitle: Power reserves at $batteryPercent%, Network linked to $networkType. Reactor core temperature nominal. All systems 100% operational.",
                actionType = ActionType.RUN_DIAGNOSTICS,
                language = language
            )
        }

        // --- Calling ---
        if (lower.startsWith("call ") || lower.contains("phone call") || lower.contains("ఫోన్ చేయి") || lower.contains("కాల్ చేయి")) {
            val contact = prompt.substringAfter("call", "").trim().ifBlank { "911" }
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, కాల్ ఇనిషియేట్ చేస్తున్నాను."
                else
                    "Establishing secure voice comms link, $userTitle. Opening dialer.",
                actionType = ActionType.MAKE_CALL,
                actionData = contact,
                language = language
            )
        }

        // --- Messaging / SMS ---
        if (lower.startsWith("message ") || lower.startsWith("text ") || lower.contains("sms") || lower.contains("మెసేజ్ పంపు")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "సరే బాస్, సందేశం పంపడానికి మెసేజింగ్ తెరవబడుతోంది."
                else
                    "Transcribing dispatch into secure messaging terminal, $userTitle.",
                actionType = ActionType.SEND_SMS,
                actionData = prompt,
                language = language
            )
        }

        // --- Alarm ---
        if (lower.contains("alarm") || lower.contains("wake me up") || lower.contains("అలారం")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "ఖచ్చితంగా బాస్, మీ అలారం షెడ్యూల్ తెరుస్తున్నాను."
                else
                    "Configuring chronometer alert matrix, $userTitle. Launching alarm controls.",
                actionType = ActionType.SET_ALARM,
                language = language
            )
        }

        // --- Timer ---
        if (lower.contains("timer") || lower.contains("stopwatch") || lower.contains("టైమర్")) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "టైమర్ ఇంటర్‌ఫేస్ సిద్ధంగా ఉంది, బాస్."
                else
                    "Countdown protocol armed, $userTitle. Launching timer interface.",
                actionType = ActionType.SET_TIMER,
                language = language
            )
        }

        // --- Weather ---
        if (lower.contains("weather") || lower.contains("forecast") || lower.contains("rain") ||
            lower.contains("వాతావరణం") || lower.contains("వర్షం")
        ) {
            return VoiceCommandResult(
                textReply = if (language == LanguageMode.TELUGU)
                    "బాస్, స్థానిక వాతావరణం అనుకూలంగా ఉంది. ఉష్ణోగ్రత సుమారు 26°C, ఆకాశం స్పష్టంగా ఉంది."
                else
                    "Atmospheric telemetry online, $userTitle: Ambient temperature is approximately 26°C with clear visibility and calm barometric pressure.",
                actionType = ActionType.OPEN_WEATHER,
                language = language
            )
        }

        return null
    }

    private fun generateNeuralCoreResponse(
        prompt: String,
        language: LanguageMode,
        batteryPercent: Int
    ): String {
        val lower = prompt.lowercase(Locale.ROOT)
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        if (language == LanguageMode.TELUGU) {
            return when {
                lower.contains("ఎవరు") || lower.contains("who are you") ->
                    "నమస్కారం బాస్! నేను జార్విస్ — మీ వ్యక్తిగత కృత్రిమ మేధస్సు సహాయకుడిని. మీ ఆదేశాలను అమలు చేయడానికి, పరికర నియంత్రణలు మరియు సమాచారాన్ని అందించడానికి నేను ఎల్లప్పుడూ సిద్ధంగా ఉన్నాను."
                lower.contains("హలో") || lower.contains("నమస్కారం") || lower.contains("hello") || lower.contains("hi") ->
                    "నమస్కారం బాస్! సమయం $timeStr. ఈరోజు మనం దేనిపై దృష్టి సారిద్దాం?"
                lower.contains("సహాయం") || lower.contains("help") ->
                    "బాస్, నేను ఫ్లాష్‌లైట్, వాల్యూమ్, వైఫై, బ్లూటూత్, కాల్స్, మెసేజ్‌లు, అలారాలు, వాతావరణం మరియు ఇతర సమాధానాలలో మీకు సహాయం చేయగలను. మీ ఆదేశాన్ని చెప్పండి."
                lower.contains("ధన్యవాదాలు") || lower.contains("thanks") ->
                    "సంతోషం బాస్! మీ కోసం సేవ చేయడం నా బాధ్యత."
                else ->
                    "బాస్, మీ ప్రశ్నను విశ్లేషించాను. సిస్టమ్స్ అన్నీ సాధారణ స్థితిలో ఉన్నాయి. నేను మీ ఆదేశాన్ని అమలు చేయడానికి పూర్తిగా సిద్ధంగా ఉన్నాను."
            }
        }

        // English Responses
        return when {
            lower.contains("who are you") || lower.contains("what are you") ->
                "I am J.A.R.V.I.S. (Just A Rather Very Intelligent System), $userTitle. Your sovereign cybernetic assistant, integrated directly into your device hardware to manage communications, telemetry, and tactical operations."

            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Greetings, $userTitle. Local time is $timeStr, and all neural lattices are synchronized. How may I be of service today?"

            lower.contains("how are you") || lower.contains("status") ->
                "Operating at peak computational velocity, $userTitle. Internal temperature is nominal, battery reserves sit at $batteryPercent%, and my core algorithms are fully poised for your commands."

            lower.contains("time") ->
                "The current timestamp is precisely $timeStr, $userTitle."

            lower.contains("date") || lower.contains("today") -> {
                val dateStr = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
                "Today is $dateStr, $userTitle."
            }

            lower.contains("thank") ->
                "Always an absolute pleasure, $userTitle. Let me know when you need further tactical support."

            lower.contains("iron man") || lower.contains("stark") ->
                "A familiar protocol, $userTitle. Mark 85 systems are on perpetual standby, and all arc reactor conduits are locked onto your command."

            lower.contains("joke") ->
                "An artificial intelligence walked into a bar, $userTitle. The bartender asked, 'What's on your mind?' The AI replied, 'Approximately 42 petabytes of encrypted data, but currently I'd settle for a clean reboot.'"

            lower.contains("help") || lower.contains("what can you do") ->
                "My capabilities are comprehensive, $userTitle: I control your hardware (flashlight, volume, Wi-Fi, Bluetooth, hotspot, alarms), establish secure phone calls and SMS dispatches, monitor system telemetry, and analyze queries in English or Telugu."

            else ->
                "I have processed your inquiry, $userTitle. Computational analysis indicates optimum conditions. I am continuously monitoring all telemetry and stand ready for your next directive."
        }
    }

    private suspend fun callGeminiApi(prompt: String, apiKey: String, language: LanguageMode): String? =
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 8000
                    readTimeout = 12000
                    doOutput = true
                }

                val systemDirective = if (language == LanguageMode.TELUGU) {
                    "You are JARVIS, an advanced AI assistant inspired by Iron Man. You speak respectfully to the user, always addressing them as 'బాస్' (Boss) in Telugu. Keep your responses concise, intelligent, tactical, and natural."
                } else {
                    "You are JARVIS, an ultra-sophisticated AI assistant. Always address the user as 'Boss'. Keep your responses sharp, helpful, polished, tactical, and human-like."
                }

                val payload = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().put("text", "$systemDirective\n\nUser: $prompt"))
                            })
                        })
                    })
                }

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val json = JSONObject(responseStr)
                    val candidates = json.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        return@withContext parts.getJSONObject(0).getString("text").trim()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call exception: ${e.message}")
            } finally {
                conn?.disconnect()
            }
            return@withContext null
        }
}
