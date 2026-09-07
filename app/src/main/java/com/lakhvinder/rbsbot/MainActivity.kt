package com.lakhvinder.rbsbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.ui.screens.BookmarksScreen
import com.lakhvinder.rbsbot.ui.screens.ChatScreen
import com.lakhvinder.rbsbot.ui.screens.DashboardScreen
import com.lakhvinder.rbsbot.ui.screens.QuizScreen
import com.lakhvinder.rbsbot.ui.screens.SettingsScreen
import com.lakhvinder.rbsbot.ui.theme.RbsTheme
import com.lakhvinder.rbsbot.viewmodel.SettingsViewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RbsTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RbsNav()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun RbsNav(settingsVM: SettingsViewModel = viewModel()) {
    val nav = rememberNavController()
    val settings by settingsVM.settings.collectAsStateWithLifecycle()
    val language = settings.language

    fun toggleLanguage() {
        settingsVM.setLanguage(
            if (language == AppLanguage.HINDI) AppLanguage.ENGLISH else AppLanguage.HINDI
        )
    }

    NavHost(navController = nav, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                language = language,
                onToggleLanguage = { toggleLanguage() },
                onSettings = { nav.navigate("settings") },
                onOpenSubject = { subject ->
                    nav.navigate("quiz/${android.net.Uri.encode(subject)}")
                },
                onOpenChat = { nav.navigate("chat") },
                onOpenBookmarks = { nav.navigate("bookmarks") }
            )
        }
        composable(
            route = "quiz/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { entry ->
            val subject = entry.arguments?.getString("subject")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: "General"
            QuizScreen(
                subject = subject,
                language = language,
                onToggleLanguage = { toggleLanguage() },
                onSettings = { nav.navigate("settings") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("chat") {
            ChatScreen(
                language = language,
                onToggleLanguage = { toggleLanguage() },
                onSettings = { nav.navigate("settings") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("bookmarks") {
            BookmarksScreen(
                language = language,
                onToggleLanguage = { toggleLanguage() },
                onSettings = { nav.navigate("settings") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                language = language,
                onToggleLanguage = { toggleLanguage() },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
