package com.lakhvinder.rbsbot.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lakhvinder.rbsbot.data.local.AiProvider
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.data.remote.AiClient
import com.lakhvinder.rbsbot.ui.components.RbsTopBar
import com.lakhvinder.rbsbot.ui.theme.ElectricBlue
import com.lakhvinder.rbsbot.ui.theme.SaffronGold
import com.lakhvinder.rbsbot.ui.theme.Success
import com.lakhvinder.rbsbot.ui.theme.TealNeon
import com.lakhvinder.rbsbot.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    settingsVM: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val s by settingsVM.settings.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RbsTopBar(
                title = "Settings",
                subtitle = "API setup",
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onSettings = {},
                showSettings = false
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TutorialCard(provider = s.provider) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(ApiKeyGuide.urlFor(s.provider)))
                )
            }

            ProviderSelector(
                provider = s.provider,
                onChange = { settingsVM.setProvider(it) }
            )

            ModeCard(
                s = s,
                onEducation = settingsVM::setEducationMode,
                onFreeOnly = settingsVM::setFreeOnly,
                onAutoModel = settingsVM::setAutoModel,
                onShowModel = settingsVM::setShowModel,
                onIndianContext = settingsVM::setIndianContext,
                onFallback = settingsVM::setFallback
            )

            when (s.provider) {
                AiProvider.GEMINI -> GeminiSection(
                    apiKey = s.geminiKey,
                    model = s.geminiModel,
                    grounding = s.geminiGrounding,
                    onKeyChange = settingsVM::setGeminiKey,
                    onModelChange = settingsVM::setGeminiModel,
                    onGroundingChange = settingsVM::setGeminiGrounding
                )
                AiProvider.OPENROUTER -> OpenRouterSection(
                    apiKey = s.openRouterKey,
                    model = s.openRouterModel,
                    onKeyChange = settingsVM::setOpenRouterKey,
                    onModelChange = settingsVM::setOpenRouterModel
                )
            }

            ReadinessCard(ready = s.isReady(), provider = s.provider)

            Text(
                "Sab settings apne phone me automatically save hoti hain (local). Koi data server par nahi jaata.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TutorialCard(provider: AiProvider, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A3F)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = SaffronGold, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Free API Key kaise banaye?", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    ApiKeyGuide.hintFor(provider),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

private object ApiKeyGuide {
    fun urlFor(provider: AiProvider): String = when (provider) {
        AiProvider.GEMINI -> "https://aistudio.google.com/apikey"
        AiProvider.OPENROUTER -> "https://openrouter.ai/keys"
    }

    fun hintFor(provider: AiProvider): String = when (provider) {
        AiProvider.GEMINI -> "Kholo: aistudio.google.com/apikey -> Create API key (free). Tap to open."
        AiProvider.OPENROUTER -> "Kholo: openrouter.ai/keys -> Create key (free models ke liye). Tap to open."
    }
}

@Composable
private fun ProviderSelector(provider: AiProvider, onChange: (AiProvider) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "Active AI API",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = provider == AiProvider.GEMINI,
                    onClick = { onChange(AiProvider.GEMINI) },
                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                )
                Column {
                    Text("Google Gemini", style = MaterialTheme.typography.bodyLarge)
                    Text("Aaj ki free Flash models (3.5-flash)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = provider == AiProvider.OPENROUTER,
                    onClick = { onChange(AiProvider.OPENROUTER) },
                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                )
                Column {
                    Text("OpenRouter", style = MaterialTheme.typography.bodyLarge)
                    Text("Verified free models (Gemma 4, Nemotron 3)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeminiSection(
    apiKey: String,
    model: String,
    grounding: Boolean,
    onKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onGroundingChange: (Boolean) -> Unit
) {
    SectionCard(title = "Google Gemini") {
        var showKey by rememberSaveable { mutableStateOf(false) }
        LabeledField("Gemini API Key", required = true)
        OutlinedTextField(
            value = apiKey,
            onValueChange = onKeyChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("AIza...") },
            visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        LabeledField("Model")
        ModelDropdown(list = AiClient.GEMINI_MODELS, selected = model, onSelect = onModelChange)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Google Search Grounding", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Current Affairs ke liye - Gemini live Google search se naya data dega. NOTE: iske liye Google AI ka paid/billing wala plan chahiye - free key par OFF hi rakho.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = grounding,
                onCheckedChange = onGroundingChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = TealNeon)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenRouterSection(
    apiKey: String,
    model: String,
    onKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit
) {
    SectionCard(title = "OpenRouter (Free Models)") {
        var showKey by rememberSaveable { mutableStateOf(false) }
        LabeledField("OpenRouter API Key", required = true)
        OutlinedTextField(
            value = apiKey,
            onValueChange = onKeyChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("sk-or-v1-...") },
            visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        LabeledField("Free Model")
        ModelDropdown(list = AiClient.OPENROUTER_FREE_MODELS, selected = model, onSelect = onModelChange)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun LabeledField(label: String, required: Boolean = false) {
    Text(
        if (required) "$label *" else label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(list: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select model") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            list.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m) },
                    onClick = {
                        onSelect(m)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadinessCard(ready: Boolean, provider: AiProvider) {
    val label = when (provider) {
        AiProvider.GEMINI -> "Gemini"
        AiProvider.OPENROUTER -> "OpenRouter"
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = (if (ready) Success else MaterialTheme.colorScheme.errorContainer).copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (ready) Success else SaffronGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (ready) "$label key ready hai - ab Quiz / Chat chalao!" else "$label key abhi nahi daali - quiz/chat ke liye zaroori hai.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (ready) Success else SaffronGold
            )
        }
    }
}

@Composable
private fun ModeCard(
    s: com.lakhvinder.rbsbot.data.local.UserSettings,
    onEducation: (Boolean) -> Unit,
    onFreeOnly: (Boolean) -> Unit,
    onAutoModel: (Boolean) -> Unit,
    onShowModel: (Boolean) -> Unit,
    onIndianContext: (Boolean) -> Unit,
    onFallback: (Boolean) -> Unit
) {
    SectionCard(title = "Education & Model Settings") {
        ToggleRow("Education Mode", "Friendly AI teacher - step-by-step samjhao, basics se", s.educationMode, onEducation)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hindi / Hinglish replies", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (s.language == AppLanguage.HINDI) "Ab ON hai - answers simple Hindi/Hinglish me" else "Ab OFF hai - answers English me (top-bar language button se badlo)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ToggleRow("Indian Context Priority", "Rajasthan syllabus, board pattern, Indian examples", s.indianContext, onIndianContext)
        ToggleRow("Auto Model Selection", "Topic ke hisaab se best free model khud chune", s.autoModel, onAutoModel)
        ToggleRow("Free Models Only", "Sirf verified free models - koi paid nahi", s.freeOnly, onFreeOnly)
        ToggleRow("Fallback Enabled", "Model fail ho to dusra free model try kare", s.fallback, onFallback)
        ToggleRow("Show Current Model", "Kaunsa AI model chal raha hai, wo dikhao", s.showModel, onShowModel)

        Spacer(Modifier.height(6.dp))
        val baseModel = com.lakhvinder.rbsbot.data.remote.AiClient.resolveModel(s)
        val modelLine = when {
            !s.autoModel -> "Manual model: $baseModel"
            s.provider == com.lakhvinder.rbsbot.data.local.AiProvider.GEMINI -> "Gemini model: $baseModel"
            else -> "Auto (topic-based) -> default: $baseModel"
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = if (s.freeOnly) Success else SaffronGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(modelLine, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (s.showModel) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Active model: ${if (com.lakhvinder.rbsbot.data.remote.AiClient.lastUsedModel != null) com.lakhvinder.rbsbot.data.remote.AiClient.lastUsedModel else "abhi koi call nahi hui"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    sub: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = TealNeon)
        )
    }
}
