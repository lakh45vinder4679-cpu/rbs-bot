package com.lakhvinder.rbsbot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.model.Mcq
import com.lakhvinder.rbsbot.ui.components.CenterLoading
import com.lakhvinder.rbsbot.ui.components.ErrorPanel
import com.lakhvinder.rbsbot.ui.components.RbsTopBar
import com.lakhvinder.rbsbot.ui.theme.Danger
import com.lakhvinder.rbsbot.ui.theme.ElectricBlue
import com.lakhvinder.rbsbot.ui.theme.SaffronGold
import com.lakhvinder.rbsbot.ui.theme.Success
import com.lakhvinder.rbsbot.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    subject: String,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
    quizVM: QuizViewModel = viewModel()
) {
    val state by quizVM.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbar.showSnackbar(it)
            quizVM.consumeMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RbsTopBar(
                title = subject,
                subtitle = "AI Quiz",
                onBack = onBack,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onSettings = onSettings
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> CenterLoading("AI se questions bante hain... thodi der")
                state.error != null -> ErrorPanel(state.error.orEmpty(), quizVM::loadQuestions)
                state.finished -> ScoreCard(state.score, state.questions.size, quizVM::restart, onBack)
                else -> QuizContent(state.current, state.progress, state.questions.size, state.selected, state.answered, quizVM::selectOption)
            }
        }
    }

    if (state.showExplain) {
        ModalBottomSheet(onDismissRequest = quizVM::closeExplain) {
            val q = state.current
            val selected = state.selected
            if (q != null && selected != null) {
                ExplanationSheet(
                    q = q,
                    selected = selected,
                    onBookmark = quizVM::bookmarkCurrent,
                    onNext = quizVM::nextQuestion,
                    isLast = state.index >= state.questions.size - 1,
                    language = language
                )
            }
        }
    }
}

@Composable
private fun QuizContent(
    q: Mcq?,
    progress: Int,
    total: Int,
    selected: Int?,
    answered: Boolean,
    onSelect: (Int) -> Unit
) {
    if (q == null) {
        CenterLoading()
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Question $progress / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { progress.toFloat() / total.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = ElectricBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                q.question,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        q.options.forEachIndexed { idx, opt ->
            OptionButton(letter = ('A' + idx).toString(), text = opt, selected = selected, correct = q.answerIndex, index = idx, answered = answered) {
                onSelect(idx)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OptionButton(
    letter: String,
    text: String,
    selected: Int?,
    correct: Int,
    index: Int,
    answered: Boolean,
    onClick: () -> Unit
) {
    val isRevealed = answered && selected != null
    val isCorrectPick = isRevealed && index == correct
    val isWrongPick = isRevealed && index == selected && index != correct
    val container: Color = when {
        isCorrectPick -> Success
        isWrongPick -> Danger
        else -> MaterialTheme.colorScheme.surface
    }
    val content: Color = when {
        isCorrectPick || isWrongPick -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor: Color = when {
        isCorrectPick -> Success
        isWrongPick -> Danger
        isRevealed && index == selected -> ElectricBlue
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }
    Surface(
        onClick = onClick,
        enabled = !answered,
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (isCorrectPick || isWrongPick) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCorrectPick) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else if (isWrongPick) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(letter, fontWeight = FontWeight.Bold, color = content)
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = content)
        }
    }
}

@Composable
private fun ExplanationSheet(
    q: Mcq,
    selected: Int,
    onBookmark: () -> Unit,
    onNext: () -> Unit,
    isLast: Boolean,
    language: AppLanguage
) {
    val correct = selected == q.answerIndex
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = SaffronGold)
            Spacer(Modifier.width(8.dp))
            Text(
                "RBS Teacher",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            if (correct) {
                if (language == AppLanguage.HINDI) "Sahi jawab! Shabash." else "Correct! Well done."
            } else {
                if (language == AppLanguage.HINDI) "Galat jawab - koi baat nahi, seekh lo." else "Wrong answer - no problem, let's learn."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (correct) Success else Danger,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "${q.question}\n\nSahi answer: ${q.options[q.answerIndex]}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (q.explanation.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    q.explanation,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBookmark, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) {
                Text(if (isLast) "Result dekho" else "Next")
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int, total: Int, onRestart: () -> Unit, onHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.School, contentDescription = null, tint = SaffronGold, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(10.dp))
        Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score / $total", style = MaterialTheme.typography.headlineMedium, color = ElectricBlue, fontWeight = FontWeight.Bold)
                val pct = if (total == 0) 0 else (score * 100) / total
                Text(
                    when {
                        pct >= 80 -> "Excellent!"
                        pct >= 50 -> "Achha hai - aur mehnat"
                        else -> "Practice more"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Text("Phir se try karo")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("Home")
        }
    }
}
