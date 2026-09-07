package com.lakhvinder.rbsbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lakhvinder.rbsbot.RbsApp
import com.lakhvinder.rbsbot.data.local.BookmarkEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<RbsApp>()

    val bookmarks: StateFlow<List<BookmarkEntity>> = app.bookmarkDao.observeAll().stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val count: StateFlow<Int> = app.bookmarkDao.observeCount().stateIn(
        viewModelScope, SharingStarted.Eagerly, 0
    )

    fun delete(bookmark: BookmarkEntity) {
        viewModelScope.launch { app.bookmarkDao.deleteById(bookmark.id) }
    }
}
