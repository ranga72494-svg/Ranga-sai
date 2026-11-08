package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGoldGlow
import com.example.ui.theme.OrbitronFontFamily
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ArcReactorView(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    state: JarvisState = JarvisState.IDLE,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorInfinite")

    // Rotation speeds based on state
    val durationOuter = when (state) {
        JarvisState.PROCESSING -> 3000
        JarvisState.LISTENING -> 5000
        JarvisState.SPEAKING -> 6000
        else -> 12000
    }

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationOuter, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationOuter / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    val primaryColor = when (state) {
        JarvisState.PROCESSING -> JarvisGold
        JarvisState.SPEAKING -> JarvisCyan
        JarvisState.LISTENING -> JarvisEmerald
        else -> JarvisCyan
    }

    val glowColor = when (state) {
        JarvisState.PROCESSING -> JarvisGoldGlow
        JarvisState.SPEAKING -> JarvisCyanGlow
        JarvisState.LISTENING -> Color(0x6600E676)
        else -> JarvisCyanGlow
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_view"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = min(this.size.width, this.size.height) / 2f * 0.92f

            // 1. Ambient outer glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * pulseGlow
                ),
                radius = radius * pulseGlow,
                center = center
            )

            // 2. Outer technical ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Rotating outer notches (24 tick marks)
            rotate(degrees = rotationOuter, pivot = center) {
                drawOuterTicks(center, radius, primaryColor)
            }

            // 4. Middle segmented targeting arcs
            val midRadius = radius * 0.76f
            rotate(degrees = rotationInner, pivot = center) {
                drawSegmentedArcs(center, midRadius, primaryColor, state)
            }

            // 5. Inner reactor ring with audio modulation
            val audioBoost = (audioLevel * 12.dp.toPx()).coerceIn(0f, 25f)
            val innerRadius = (radius * 0.48f + audioBoost) * pulseGlow

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            drawCircle(
                color = primaryColor,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // 6. Center core triangle/point target
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx() + (audioLevel * 4.dp.toPx()),
                center = center
            )
        }

        // Center State Text
        val centerLabel = when (state) {
            JarvisState.LISTENING -> "LISTENING"
            JarvisState.PROCESSING -> "PROCESSING"
            JarvisState.SPEAKING -> "TALKING"
            JarvisState.OFFLINE -> "OFFLINE"
            JarvisState.IDLE -> "JARVIS"
        }

        Text(
            text = centerLabel,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = primaryColor
        )
    }
}

private fun DrawScope.drawOuterTicks(center: Offset, radius: Float, color: Color) {
    val tickCount = 24
    val tickLength = 7.dp.toPx()
    for (i in 0 until tickCount) {
        val angleRad = Math.toRadians((i * (360f / tickCount)).toDouble())
        val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
        val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
        val endX = (center.x + radius * cos(angleRad)).toFloat()
        val endY = (center.y + radius * sin(angleRad)).toFloat()

        val tickColor = if (i % 6 == 0) color else color.copy(alpha = 0.35f)
        val strokeW = if (i % 6 == 0) 3.dp.toPx() else 1.5.dp.toPx()

        drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSegmentedArcs(
    center: Offset,
    radius: Float,
    color: Color,
    state: JarvisState
) {
    val strokeWidth = 3.5.dp.toPx()
    val arcSegments = listOf(
        Triple(0f, 65f, color),
        Triple(90f, 65f, color.copy(alpha = 0.6f)),
        Triple(180f, 65f, color),
        Triple(270f, 65f, color.copy(alpha = 0.6f))
    )

    arcSegments.forEach { (startAngle, sweepAngle, arcColor) ->
        drawArc(
            color = arcColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
