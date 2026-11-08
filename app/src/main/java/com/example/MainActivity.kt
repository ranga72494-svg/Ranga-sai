package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.JarvisService
import com.example.ui.screens.CommunicationsScreen
import com.example.ui.screens.DeviceControlsScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkBackground
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisDarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.JarvisViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    CORE_HUD("CORE", Icons.Default.GraphicEq),
    CONTROLS("CONTROLS", Icons.Default.Tune),
    COMMS("COMMS", Icons.Default.Call),
    DIAGNOSTICS("TELEMETRY", Icons.Default.Speed),
    SETTINGS("CONFIG", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                JarvisAppRoot()
            }
        }
    }
}

@Composable
fun JarvisAppRoot(viewModel: JarvisViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.CORE_HUD) }

    // Request permissions for audio recording, notifications, and camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            JarvisService.startService(context)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            JarvisService.startService(context)
        }
    }

    // Handle back button on secondary tabs
    if (currentTab != NavigationTab.CORE_HUD) {
        BackHandler {
            currentTab = NavigationTab.CORE_HUD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDarkBackground)
            .statusBarsPadding(),
        bottomBar = {
            JarvisBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.CORE_HUD -> HomeScreen(viewModel = viewModel)
                    NavigationTab.CONTROLS -> DeviceControlsScreen(viewModel = viewModel)
                    NavigationTab.COMMS -> CommunicationsScreen(viewModel = viewModel)
                    NavigationTab.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                    NavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun JarvisBottomNavigation(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = JarvisDarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, JarvisCardBorder)
            .navigationBarsPadding()
            .height(64.dp)
            .testTag("jarvis_bottom_nav")
    ) {
        NavigationTab.values().forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JarvisDarkBackground,
                    selectedTextColor = JarvisCyan,
                    indicatorColor = JarvisCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
