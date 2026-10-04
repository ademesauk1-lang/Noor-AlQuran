package com.example.ui.state

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.data.repository.MushafPageContent
import com.example.data.repository.MushafPageInfo
import com.example.data.repository.MushafPageMapper
import com.example.data.repository.QuranData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MushafUiState(
    val currentPageNumber: Int = 1,
    val totalPages: Int = 604,
    val fontSizeSp: Float = 26f,
    val keepScreenOn: Boolean = true,
    val currentPageContent: MushafPageContent? = null,
    val pageInfo: MushafPageInfo = MushafPageMapper.getPageInfo(1),
    val isLoadingPage: Boolean = false,
    val isJumpDialogOpen: Boolean = false,
    val bookmarkedPages: Set<Int> = emptySet(),
    val cachedPages: Map<Int, MushafPageContent> = emptyMap()
)

class MushafViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("mushaf_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    /**
     * Pages currently being loaded. Prevents the pager from starting the same
     * (expensive) page parse twice when the user swipes quickly.
     */
    private val inFlightPages: MutableSet<Int> =
        java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<Int, Boolean>())

    init {
        // Restore last read page and bookmarks
        val savedPage = prefs.getInt("mushaf_last_read_page", 1).coerceIn(1, 604)
        val savedFontSize = prefs.getFloat("mushaf_font_size", 26f)
        val savedBookmarks = prefs.getStringSet("mushaf_bookmarks", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

        _uiState.update {
            it.copy(
                currentPageNumber = savedPage,
                fontSizeSp = savedFontSize,
                bookmarkedPages = savedBookmarks,
                pageInfo = MushafPageMapper.getPageInfo(savedPage)
            )
        }

        loadPage(savedPage)
    }

    fun loadPage(pageNumber: Int) {
        val page = pageNumber.coerceIn(1, 604)

        // Persist the reading position (apply() writes on a background thread).
        prefs.edit().putInt("mushaf_last_read_page", page).apply()

        val info = MushafPageMapper.getPageInfo(page)
        val cached = _uiState.value.cachedPages[page]
        _uiState.update {
            it.copy(
                currentPageNumber = page,
                pageInfo = info,
                currentPageContent = cached ?: it.currentPageContent,
                isLoadingPage = cached == null
            )
        }

        if (cached != null) {
            // Instant hit: refresh neighbours and return without any I/O.
            preloadAdjacentPages(page)
            return
        }

        loadPageContent(page, preloadAdjacent = true)
    }

    /**
     * Resolves the page content off the main thread (Room query + network/asset
     * JSON parsing) and publishes it once available.
     */
    private fun loadPageContent(pageNumber: Int, preloadAdjacent: Boolean = false) {
        if (!inFlightPages.add(pageNumber)) return

        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    MushafPageMapper.loadPageContent(pageNumber)
                }
                _uiState.update { state ->
                    state.copy(
                        currentPageContent = if (state.currentPageNumber == pageNumber) content else state.currentPageContent,
                        isLoadingPage = if (state.currentPageNumber == pageNumber) false else state.isLoadingPage,
                        cachedPages = state.cachedPages.withPage(pageNumber, content)
                    )
                }
                if (preloadAdjacent) preloadAdjacentPages(pageNumber)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { state ->
                    if (state.currentPageNumber == pageNumber) state.copy(isLoadingPage = false) else state
                }
            } finally {
                inFlightPages.remove(pageNumber)
            }
        }
    }

    /** Warms the previous and next page so swiping is instantly fluid. */
    private fun preloadAdjacentPages(pageNumber: Int) {
        preloadPage(pageNumber - 1)
        preloadPage(pageNumber + 1)
    }

    private fun preloadPage(pageNumber: Int) {
        if (pageNumber !in 1..604) return
        if (_uiState.value.cachedPages.containsKey(pageNumber)) return
        loadPageContent(pageNumber)
    }

    /**
     * Keeps the page cache bounded (adjacent pages only) so the reader cannot
     * grow the heap without limit while a user reads for a long time.
     */
    private fun Map<Int, MushafPageContent>.withPage(
        pageNumber: Int,
        content: MushafPageContent
    ): Map<Int, MushafPageContent> {
        val updated = LinkedHashMap<Int, MushafPageContent>(minOf(size + 1, MAX_CACHED_PAGES + 1))
        updated.putAll(this)
        updated[pageNumber] = content
        while (updated.size > MAX_CACHED_PAGES) {
            val oldest = updated.keys.first()
            updated.remove(oldest)
        }
        return updated
    }

    fun updateFontSize(newSize: Float) {
        val clampedSize = newSize.coerceIn(18f, 42f)
        prefs.edit().putFloat("mushaf_font_size", clampedSize).apply()
        _uiState.update { it.copy(fontSizeSp = clampedSize) }
    }

    fun toggleBookmarkPage(pageNumber: Int = _uiState.value.currentPageNumber) {
        val currentBookmarks = _uiState.value.bookmarkedPages.toMutableSet()
        if (currentBookmarks.contains(pageNumber)) {
            currentBookmarks.remove(pageNumber)
        } else {
            currentBookmarks.add(pageNumber)
        }
        prefs.edit().putStringSet("mushaf_bookmarks", currentBookmarks.map { it.toString() }.toSet()).apply()
        _uiState.update { it.copy(bookmarkedPages = currentBookmarks) }
    }

    fun jumpToSurah(surahId: Int) {
        val page = MushafPageMapper.getPageForSurah(surahId)
        loadPage(page)
    }

    fun jumpToJuz(juzNumber: Int) {
        val page = MushafPageMapper.getPageForJuz(juzNumber)
        loadPage(page)
    }

    fun setJumpDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isJumpDialogOpen = isOpen) }
    }

    companion object {
        /** Current page + one page either side (warm-up neighbours included). */
        private const val MAX_CACHED_PAGES = 6
    }
}
