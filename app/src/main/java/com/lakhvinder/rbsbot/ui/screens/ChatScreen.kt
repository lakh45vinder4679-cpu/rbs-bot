package com.lakhvinder.rbsbot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.data.local.ChatMessageEntity
import com.lakhvinder.rbsbot.ui.components.RbsTopBar
import com.lakhvinder.rbsbot.ui.theme.ElectricBlue
import com.lakhvinder.rbsbot.ui.theme.SurfaceVariantDark
import com.lakhvinder.rbsbot.ui.theme.TealNeon
import com.lakhvinder.rbsbot.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
    chatVM: ChatViewModel = viewModel()
) {
    val messages by chatVM.messages.collectAsStateWithLifecycle()
    val sending by chatVM.sending.collectAsStateWithLifecycle()
    val error by chatVM.error.collectAsStateWithLifecycle()
    val modelLabel by chatVM.modelLabel.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            chatVM.consumeError()
        }
    }
    LaunchedEffect(messages.size, sending) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RbsTopBar(
                title = "Chat with RBS Bot",
                subtitle = "Doubt Solver • by Lakhvinder",
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onSettings = onSettings
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (modelLabel.isNotEmpty()) {
                Text(
                    modelLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Ex: Rajasthan ki jalvayu samjhao, ya Rajasthan ke veer shasak kaun the?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
                if (messages.isEmpty()) {
                    item {
                        EmptyChatHint()
                    }
                }
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg)
                }
                if (sending) {
                    item {
                        TypingBubble()
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            InputBar(
                sending = sending,
                onSend = { chatVM.sendMessage(it) },
                onClear = { chatVM.clearChat() }
            )
        }
    }
}

@Composable
private fun EmptyChatHint() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("RBS Bot se poocho", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Rajasthan History • Geography • Art & Culture • Polity • Current Affairs\n\nJawab Hindi ya English me milega - upar language toggle karo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessageEntity) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) ElectricBlue.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Text(
                msg.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TealNeon)
                Text("soch raha hai...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InputBar(sending: Boolean, onSend: (String) -> Unit, onClear: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClear) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Clear chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Apna question likho...") },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealNeon,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = SurfaceVariantDark.copy(alpha = 0.4f),
                unfocusedContainerColor = SurfaceVariantDark.copy(alpha = 0.2f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            })
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            },
            enabled = !sending && text.isNotBlank(),
            shape = RoundedCornerShape(50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}
