package com.example.data.controller

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import com.example.data.model.SystemTelemetry

class DeviceManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var isTorchOn = false
    private var cameraId: String? = null

    init {
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.e("DeviceManager", "Failed to get camera characteristics: ${e.message}")
        }
    }

    fun vibrate(durationMs: Long = 80) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("DeviceManager", "Vibrate error: ${e.message}")
        }
    }

    fun setFlashlight(enable: Boolean): Boolean {
        return try {
            val targetId = cameraId ?: cameraManager?.cameraIdList?.firstOrNull()
            if (targetId != null && cameraManager != null) {
                cameraManager.setTorchMode(targetId, enable)
                isTorchOn = enable
                vibrate(50)
                true
            } else {
                false
            }
        } catch (e: CameraAccessException) {
            Log.e("DeviceManager", "Flashlight error: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("DeviceManager", "Flashlight exception: ${e.message}")
            false
        }
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!isTorchOn)
    }

    fun isFlashlightActive(): Boolean = isTorchOn

    fun adjustVolume(increase: Boolean): Int {
        audioManager?.let { am ->
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            vibrate(40)
            return getCurrentVolumePercent()
        }
        return 50
    }

    fun muteVolume(): Boolean {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
            } else {
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_MUSIC, true)
            }
            vibrate(60)
            return true
        }
        return false
    }

    fun setVolumePercent(percent: Int) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = ((percent / 100f) * max).toInt().coerceIn(0, max)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            vibrate(30)
        }
    }

    fun getCurrentVolumePercent(): Int {
        audioManager?.let { am ->
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            return if (max > 0) ((current.toFloat() / max) * 100).toInt() else 50
        }
        return 50
    }

    fun openWifiSettings(): Boolean {
        return safeLaunchIntent(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    fun openBluetoothSettings(): Boolean {
        return safeLaunchIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun openHotspotSettings(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.android.settings", "com.android.settings.TetherSettings")
        }
        return if (!safeLaunchIntent(intent)) {
            safeLaunchIntent(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        } else true
    }

    fun openAirplaneModeSettings(): Boolean {
        return safeLaunchIntent(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
    }

    fun openDisplaySettings(): Boolean {
        return safeLaunchIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS))
    }

    fun makeCall(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.trim().replace(" ", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeLaunchIntent(intent)
    }

    fun sendSms(phoneNumber: String, messageText: String = ""): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${phoneNumber.trim()}")
            if (messageText.isNotEmpty()) {
                putExtra("sms_body", messageText)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeLaunchIntent(intent)
    }

    fun setAlarm(hour: Int, minutes: Int, message: String = "Jarvis Alarm"): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeLaunchIntent(intent)
    }

    fun setTimer(seconds: Int, message: String = "Jarvis Timer"): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeLaunchIntent(intent)
    }

    fun openApp(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            safeLaunchIntent(launchIntent)
        } else {
            false
        }
    }

    fun getTelemetry(): SystemTelemetry {
        var batteryPercent = 85
        var isCharging = false
        var batteryTemp = 30.0f

        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.let {
                batteryPercent = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
                val status = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
                isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
            }
        } catch (e: Exception) {
            Log.e("DeviceManager", "Battery check error: ${e.message}")
        }

        var networkType = "Offline (Local Core Active)"
        try {
            val activeNet = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNet)
            if (caps != null) {
                networkType = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (Ultra High Speed)"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/LTE"
                    else -> "Connected (Active)"
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceManager", "Network check error: ${e.message}")
        }

        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMem = runtime.maxMemory() / (1024 * 1024)

        return SystemTelemetry(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            batteryTemp = batteryTemp,
            networkType = networkType,
            isWifiEnabled = networkType.contains("Wi-Fi"),
            isBluetoothEnabled = true,
            isFlashlightOn = isTorchOn,
            volumePercent = getCurrentVolumePercent(),
            ramUsedMb = usedMem,
            ramTotalMb = maxMem,
            uptimeSeconds = android.os.SystemClock.elapsedRealtime() / 1000,
            coreStatus = "JARVIS v4.2 - OPTIMAL"
        )
    }

    private fun safeLaunchIntent(intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            vibrate(40)
            true
        } catch (e: Exception) {
            Log.e("DeviceManager", "Failed to launch intent: ${e.message}")
            false
        }
    }
}
