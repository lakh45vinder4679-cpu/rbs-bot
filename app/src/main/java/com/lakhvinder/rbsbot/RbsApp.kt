package com.lakhvinder.rbsbot

import android.app.Application
import com.lakhvinder.rbsbot.data.local.AppDatabase
import com.lakhvinder.rbsbot.data.local.ChatDao
import com.lakhvinder.rbsbot.data.local.SettingsRepository
import com.lakhvinder.rbsbot.data.remote.AiClient

class RbsApp : Application() {

    private val database by lazy { AppDatabase.get(this) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val chatDao: ChatDao by lazy { database.chatDao() }
    val bookmarkDao: com.lakhvinder.rbsbot.data.local.BookmarkDao by lazy { database.bookmarkDao() }

    val aiClient: AiClient by lazy { AiClient }
}
