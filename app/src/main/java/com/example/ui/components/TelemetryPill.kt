package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurfaceVariant
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary

@Composable
fun TelemetryPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    statusColor: Color = JarvisCyan
) {
    Row(
        modifier = modifier
            .background(JarvisDarkSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = statusColor,
            modifier = Modifier.size(15.dp)
        )

        Text(
            text = label.uppercase(),
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF7A9BBF)
        )

        Box(
            modifier = Modifier
                .size(4.dp)
                .background(statusColor, CircleShape)
        )

        Text(
            text = value,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}
