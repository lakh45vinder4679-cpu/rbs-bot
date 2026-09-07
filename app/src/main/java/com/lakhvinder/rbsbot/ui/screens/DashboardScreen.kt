package com.lakhvinder.rbsbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.ui.components.GradientTile
import com.lakhvinder.rbsbot.ui.components.RbsTopBar
import com.lakhvinder.rbsbot.ui.components.SubjectTiles
import com.lakhvinder.rbsbot.ui.theme.SaffronGold
import com.lakhvinder.rbsbot.ui.theme.TealNeon
import com.lakhvinder.rbsbot.viewmodel.BookmarksViewModel

@Composable
fun DashboardScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSettings: () -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenBookmarks: () -> Unit,
    bookmarksVM: BookmarksViewModel = viewModel()
) {
    val count by bookmarksVM.count.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RbsTopBar(
                title = "RBS Bot",
                subtitle = "by Lakhvinder • RBSE Exam Prep",
                language = language,
                onToggleLanguage = onToggleLanguage,
                onSettings = onSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenChat,
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                text = { Text("Chat with RBS Bot") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(onOpenChat)
            }
            item {
                Text(
                    "Practice Quiz",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Subject select karo — AI 10 questions banayega",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(SubjectTiles.chunked(2)) { _, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tile ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                            GradientTile(tile) { onOpenSubject(tile.name) }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item {
                BookmarksRow(count, onOpenBookmarks)
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
    }
}

@Composable
private fun HeroCard(onOpenChat: () -> Unit) {
    Surface(
        onClick = onOpenChat,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF141C33), Color(0xFF20294A))),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = SaffronGold.copy(alpha = 0.5f),
                    modifier = Modifier
                        .width(140.dp)
                        .height(140.dp)
                        .align(Alignment.BottomEnd)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Namaste! Kaisa chal raha hai prep?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Koi doubt? RBS Bot teacher ko poocho — turant jawab milega.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = TealNeon,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            "Ask now",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFF062018),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksRow(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = SaffronGold)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Bookmarked Questions", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Apne saved questions yahan milenge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                Badge {
                    Text("$count")
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
