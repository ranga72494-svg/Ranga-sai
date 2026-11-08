package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold

@Composable
fun AudioWaveformBar(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    audioLevel: Float = 0f,
    barCount: Int = 18,
    maxHeight: Dp = 28.dp,
    color: Color = JarvisCyan
) {
    val transition = rememberInfiniteTransition(label = "WaveformAnimation")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 45)
            val animatedHeightFraction by transition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarHeight$i"
            )

            val currentHeight = if (isActive) {
                val factor = (animatedHeightFraction * 0.6f + audioLevel * 0.8f).coerceIn(0.1f, 1f)
                maxHeight * factor
            } else {
                3.dp
            }

            val barColor = if (i % 4 == 0) JarvisGold else color

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(currentHeight)
                    .background(
                        color = barColor.copy(alpha = if (isActive) 0.9f else 0.25f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            if (i < barCount - 1) {
                Box(modifier = Modifier.width(3.5.dp))
            }
        }
    }
}
