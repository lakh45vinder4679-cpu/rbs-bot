package com.lakhvinder.rbsbot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.ui.theme.ElectricBlue
import com.lakhvinder.rbsbot.ui.theme.OutlinesDark
import com.lakhvinder.rbsbot.ui.theme.PurpleNeon
import com.lakhvinder.rbsbot.ui.theme.SaffronGold
import com.lakhvinder.rbsbot.ui.theme.SurfaceVariantDark
import com.lakhvinder.rbsbot.ui.theme.TealNeon

data class SubjectTile(
    val name: String,
    val tagline: String,
    val gradient: List<Color>,
    val icon: ImageVector,
    val accent: Color
)

val SubjectTiles = listOf(
    SubjectTile(
        "Rajasthan History", "Rajwade, veer shasak, itihas",
        listOf(Color(0xFF3D5AFE), Color(0xFF7B5CFF)), Icons.Filled.AutoStories, Color(0xFFA9BCFF)
    ),
    SubjectTile(
        "Geography", "Map, jalvayu, nadiya, marusthal",
        listOf(Color(0xFF0EA5A0), Color(0xFF22D3EE)), Icons.Filled.Landscape, Color(0xFF67E8F9)
    ),
    SubjectTile(
        "Art & Culture", "Lok kala, mehandi, fest, gharana",
        listOf(Color(0xFFF59E0B), Color(0xFFFB7185)), Icons.Filled.Palette, Color(0xFFFDE68A)
    ),
    SubjectTile(
        "Polity", "Rajya, vidhan sabha, panchayati raj",
        listOf(Color(0xFF7C3AED), Color(0xFFD946EF)), Icons.Filled.AccountBalance, Color(0xFFF0ABFC)
    ),
    SubjectTile(
        "Current Affairs", "Rajasthan news, yojnaye, awards",
        listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)), Icons.Filled.Newspaper, Color(0xFFBAE6FD)
    )
)

@Composable
fun RbsTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSettings: () -> Unit,
    showSettings: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        FilterChip(
            selected = true,
            onClick = onToggleLanguage,
            label = { Text(language.nativeName, fontWeight = FontWeight.SemiBold) },
            leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        Spacer(Modifier.width(2.dp))
        if (showSettings) {
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun CenterLoading(text: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = ElectricBlue)
        if (text != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GradientTile(
    tile: SubjectTile,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .background(Brush.linearGradient(tile.gradient), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Icon(
                tile.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.TopEnd),
                tint = tile.accent
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    tile.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
                Text(
                    tile.tagline,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            OutlinedButton(onClick = onRetry, border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer)) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun StatusInfo(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantDark.copy(alpha = 0.55f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
