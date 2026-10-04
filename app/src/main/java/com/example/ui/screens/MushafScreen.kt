package com.example.ui.screens

import com.example.ui.components.QuranMushafPageView
import com.example.ui.components.QuranLocationPickerBottomSheet
import com.example.ui.components.toArabicDigits

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.data.repository.MushafPageContent
import com.example.data.repository.MushafPageMapper
import com.example.data.repository.QuranData
import com.example.ui.state.MushafViewModel
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.QuranBorderGold
import com.example.ui.theme.QuranParchmentBg
import com.example.util.QuranUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MushafScreen(
    onNavigateBack: (() -> Unit)? = null,
    mushafViewModel: MushafViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // The state holder is never read directly from the composition scope: every
    // value goes through `derivedStateOf`, so a change to one field (e.g. caching
    // a pre-loaded page in the background) recomposes only the widgets that
    // actually display that field instead of the whole mushaf screen.
    val uiStateState = mushafViewModel.uiState.collectAsState()
    val pageInfo by remember(uiStateState) { derivedStateOf { uiStateState.value.pageInfo } }
    val currentPageNumber by remember(uiStateState) { derivedStateOf { uiStateState.value.currentPageNumber } }
    val fontSizeSp by remember(uiStateState) { derivedStateOf { uiStateState.value.fontSizeSp } }
    val bookmarkedPages by remember(uiStateState) { derivedStateOf { uiStateState.value.bookmarkedPages } }
    val isJumpDialogOpen by remember(uiStateState) { derivedStateOf { uiStateState.value.isJumpDialogOpen } }

    // 1. Keep Screen On while reading
    DisposableEffect(Unit) {
        val activity = findActivity(context)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 2. HorizontalPager State (1..604 pages, 0-indexed)
    val initialPage = (currentPageNumber - 1).coerceIn(0, 603)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 604 }
    )

    // Sync Pager swipes back to ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { pageIndex ->
            val pageNum = pageIndex + 1
            if (pageNum != currentPageNumber) {
                mushafViewModel.loadPage(pageNum)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "سُورَةُ ${pageInfo.primarySurahNameArabic}",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "•  Juz ${pageInfo.juzNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Surah ${pageInfo.primarySurahNameEnglish}  |  Page $currentPageNumber of 604",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("mushaf_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // Quick Jump to Page / Surah Dialog Button
                    IconButton(
                        onClick = { mushafViewModel.setJumpDialogOpen(true) },
                        modifier = Modifier.testTag("mushaf_jump_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Jump to Page or Surah",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark Page Button
                    IconButton(
                        onClick = { mushafViewModel.toggleBookmarkPage() },
                        modifier = Modifier.testTag("mushaf_bookmark_button")
                    ) {
                        val isBookmarked = bookmarkedPages.contains(currentPageNumber)
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Page",
                            tint = if (isBookmarked) GoldAccentLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        // 3. Right-to-Left (RTL) HorizontalPager for 604 pages
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(
                state = pagerState,
                // Keeps a neighbour page composed on each side so swiping never
                // has to wait for a cold page load.
                beyondBoundsPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("mushaf_horizontal_pager")
            ) { pageIndex ->
                val pageNum = pageIndex + 1

                // Read inside the page scope: pre-loading another page only
                // recomposes the page whose content changed.
                val cachedContent by remember(pageNum) {
                    derivedStateOf { uiStateState.value.cachedPages[pageNum] }
                }

                val content = cachedContent
                if (content != null) {
                    QuranMushafPageView(
                        pageContent = content,
                        fontSizeSp = fontSizeSp,
                        onAyahTap = { ayah ->
                            val label = "Surah ${ayah.surahNumber}, Ayah ${ayah.ayahNumber}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(label, ayah.textArabic))
                            Toast.makeText(context, "Copied: $label", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Page $pageNum...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // Jump to Page / Surah / Juz Modal BottomSheet
    if (isJumpDialogOpen) {
        QuranLocationPickerBottomSheet(
            currentPage = currentPageNumber,
            onDismiss = { mushafViewModel.setJumpDialogOpen(false) },
            onJumpToPage = { targetPage ->
                mushafViewModel.setJumpDialogOpen(false)
                val targetIndex = (targetPage - 1).coerceIn(0, 603)
                coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
            },
            onJumpToSurah = { surahId ->
                mushafViewModel.setJumpDialogOpen(false)
                mushafViewModel.jumpToSurah(surahId)
                val targetPage = MushafPageMapper.getPageForSurah(surahId)
                coroutineScope.launch { pagerState.scrollToPage((targetPage - 1).coerceIn(0, 603)) }
            },
            onJumpToJuz = { juzNum ->
                mushafViewModel.setJumpDialogOpen(false)
                mushafViewModel.jumpToJuz(juzNum)
                val targetPage = MushafPageMapper.getPageForJuz(juzNum)
                coroutineScope.launch { pagerState.scrollToPage((targetPage - 1).coerceIn(0, 603)) }
            }
        )
    }
}

/**
 * Helper to safely extract Activity from Context
 */
private fun findActivity(context: Context): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
