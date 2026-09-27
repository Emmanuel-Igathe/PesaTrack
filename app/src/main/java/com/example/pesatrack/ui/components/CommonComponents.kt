package com.example.pesatrack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pesatrack.ui.theme.*

@Composable
fun SmartAlertBanner(
    isExceeded: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isExceeded) AlertRed.copy(alpha = 0.15f) else AlertAmber.copy(alpha = 0.15f)
    val contentColor = if (isExceeded) AlertRed else AlertAmber
    val icon = if (isExceeded) Icons.Default.Warning else Icons.Default.Info

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(contentColor.copy(alpha = 0.5f))
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Alert",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%s %,.2f", currencySymbol, amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getCategoryIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("rent") || lower.contains("house") -> Icons.Default.Home
        lower.contains("food") || lower.contains("grocer") -> Icons.Default.Restaurant
        lower.contains("transport") || lower.contains("school") || lower.contains("bus") -> Icons.Default.DirectionsBus
        lower.contains("electri") || lower.contains("power") -> Icons.Default.Bolt
        lower.contains("water") -> Icons.Default.WaterDrop
        lower.contains("pocket") || lower.contains("salary") || lower.contains("income") -> Icons.Default.Payments
        lower.contains("saving") -> Icons.Default.Savings
        lower.contains("phone") || lower.contains("airtime") -> Icons.Default.Smartphone
        lower.contains("laptop") || lower.contains("tech") -> Icons.Default.Laptop
        lower.contains("wifi") || lower.contains("internet") -> Icons.Default.Wifi
        else -> Icons.Default.Category
    }
}
