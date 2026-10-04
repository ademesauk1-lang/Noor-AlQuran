package com.example.ui.screens

import com.example.ui.components.QuranLocationPickerBottomSheet
import com.example.ui.components.SurahHeaderBar
import com.example.ui.components.ScallopedRosetteBadge
import com.example.ui.components.MushafTealPrimary
import com.example.ui.components.toArabicDigits
import com.example.ui.components.isSajdahVerse
import com.example.data.repository.MushafPageMapper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.TranslationLanguage
import com.example.data.repository.QuranData
import com.example.ui.state.QuranUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    uiState: QuranUiState,
    onSelectSurah: (Int) -> Unit,
    onPlayAudio: (Int, Int) -> Unit,
    onToggleBookmark: (Int, Int) -> Unit,
    onUpdateFontSize: (Float) -> Unit,
    onSelectTranslation: (TranslationLanguage) -> Unit,
    onUpdateLastReadAyah: (Int) -> Unit,
    onExplainAyah: (Int, Int) -> Unit,
    onCloseExplanation: () -> Unit,
    onNavigateToPremium: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // O(1) lookup, memoised per selected surah instead of scanning all 114 on
    // every recomposition.
    val currentSurah = remember(uiState.selectedSurahId) {
        QuranData.surahById(uiState.selectedSurahId) ?: QuranData.surahs[0]
    }
    val languageId = uiState.translationLanguage.id
    // Start from the in-memory cache when available so switching back to a surah
    // renders instantly instead of flashing a spinner.
    val ayahs by produceState<List<com.example.data.model.Ayah>>(
        initialValue = remember(currentSurah.id, languageId) {
            QuranData.cachedAyahsForSurah(currentSurah.id, languageId).orEmpty()
        },
        currentSurah.id,
        languageId
    ) {
        value = QuranData.fetchAyahsForSurah(currentSurah.id, languageId)
    }
    
    var showSurahJuzPickerSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Reader Header Bar with navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reader_header_card")
        ) {
            // Surah Navigation and action tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        enabled = currentSurah.id > 1,
                        onClick = { onSelectSurah(currentSurah.id - 1) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Surah"
                        )
                    }
                    Text(
                        text = "${currentSurah.id}. ${currentSurah.nameEnglish}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showSurahJuzPickerSheet = true },
                        modifier = Modifier.testTag("reader_location_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Jump to Location",
                            tint = MushafTealPrimary
                        )
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reader Settings",
                            tint = MushafTealPrimary
                        )
                    }
                    IconButton(
                        enabled = currentSurah.id < 114,
                        onClick = { onSelectSurah(currentSurah.id + 1) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Surah"
                        )
                    }
                }
            }

            // Authentic Surah Header Bar matching reference image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSurahJuzPickerSheet = true }
            ) {
                SurahHeaderBar(surah = currentSurah)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bismillah Header Card (For all except Surah At-Tawbah)
        if (currentSurah.id != 9) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MushafTealPrimary.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontFamily = AmiriFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Verse List
        val listState = rememberLazyListState()

        // Runs once content is available (and again when the target ayah
        // changes), scrolling to the requested verse.
        LaunchedEffect(uiState.selectedSurahId, uiState.selectedAyahNumber, ayahs) {
            if (ayahs.isEmpty()) return@LaunchedEffect
            val targetIndex = ayahs.indexOfFirst { it.ayahNumber == uiState.selectedAyahNumber }
            if (targetIndex >= 0 && listState.firstVisibleItemIndex != targetIndex) {
                listState.scrollToItem(targetIndex)
            }
        }

        // Persist the reading position, throttled to distinct verses so normal
        // scrolling does not write to storage/prefs on every frame.
        LaunchedEffect(listState, ayahs) {
            if (ayahs.isEmpty()) return@LaunchedEffect
            var lastReportedAyah = -1
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { index ->
                    if (index in ayahs.indices) {
                        val ayahNumber = ayahs[index].ayahNumber
                        if (ayahNumber != lastReportedAyah) {
                            lastReportedAyah = ayahNumber
                            onUpdateLastReadAyah(ayahNumber)
                        }
                    }
                }
        }

        if (ayahs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = ayahs,
                    key = { ayah -> "${ayah.surahNumber}:${ayah.ayahNumber}" },
                    contentType = { "ayah" }
                ) { ayah ->
                    val isBookmarked = uiState.bookmarkedAyahs.contains("${ayah.surahNumber}:${ayah.ayahNumber}")
                    AyahCardItem(
                        ayah = ayah,
                        fontSizeSp = uiState.arabicFontSizeSp,
                        translationLang = uiState.translationLanguage,
                        isBookmarked = isBookmarked,
                        isPremiumUser = uiState.isPremiumUser,
                        onPlayAudio = { onPlayAudio(ayah.surahNumber, ayah.ayahNumber) },
                        onToggleBookmark = { onToggleBookmark(ayah.surahNumber, ayah.ayahNumber) },
                        onExplainAyah = {
                            if (!uiState.isPremiumUser) {
                                onNavigateToPremium?.invoke()
                            } else {
                                onExplainAyah(ayah.surahNumber, ayah.ayahNumber)
                            }
                        },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Ayah Text", "${ayah.textArabic}\n\n${ayah.translations[uiState.translationLanguage.id] ?: ""}\n[Quran ${ayah.surahNumber}:${ayah.ayahNumber}]")
                            clipboard.setPrimaryClip(clip)
                            val toast = Toast.makeText(context, "Ayah copied to clipboard", Toast.LENGTH_SHORT)
                            toast.setGravity(android.view.Gravity.BOTTOM, 0, 240)
                            toast.show()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(96.dp)) }
            }
        }
    }

    // Integrated Shared Location Picker Bottom Sheet
    if (showSurahJuzPickerSheet) {
        val currentPage = MushafPageMapper.getPageForSurah(currentSurah.id)
        QuranLocationPickerBottomSheet(
            currentPage = currentPage,
            initialTab = 1,
            onDismiss = { showSurahJuzPickerSheet = false },
            onJumpToPage = { targetPage ->
                val pageInfo = MushafPageMapper.getPageInfo(targetPage)
                onSelectSurah(pageInfo.surahStartId)
                showSurahJuzPickerSheet = false
            },
            onJumpToSurah = { surahId ->
                onSelectSurah(surahId)
                showSurahJuzPickerSheet = false
            },
            onJumpToJuz = { juzNum ->
                val juzInfo = QuranData.juzList.find { it.juzNumber == juzNum }
                if (juzInfo != null) {
                    val startSurahId = juzInfo.surahIdsIncluded.firstOrNull() ?: 1
                    onSelectSurah(startSurahId)
                }
                showSurahJuzPickerSheet = false
            }
        )
    }

    // Reader Settings BottomSheet
    if (showSettingsSheet) {
        val darkSheetBg = androidx.compose.ui.graphics.Color(0xFF1B1B22)
        val darkItemBg = androidx.compose.ui.graphics.Color(0xFF2A2A35)
        val darkIconBg = androidx.compose.ui.graphics.Color(0xFF353642)
        
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = darkSheetBg,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(darkIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Reader Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                HorizontalDivider(color = darkIconBg)

                // Arabic Font Size
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatSize, 
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFA0A0AB)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Arabic Font Size",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFFEBEBEB)
                            )
                        }
                        Text(
                            text = "${uiState.arabicFontSizeSp.toInt()} sp", 
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = darkItemBg
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("A", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = androidx.compose.ui.graphics.Color(0xFFA0A0AB))
                            Slider(
                                value = uiState.arabicFontSizeSp,
                                onValueChange = onUpdateFontSize,
                                valueRange = 20f..42f,
                                steps = 11,
                                modifier = Modifier.weight(1f)
                            )
                            Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFA0A0AB))
                        }
                    }
                }

                // Translation Language Selection
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Translate, 
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFFA0A0AB)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Translation Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = androidx.compose.ui.graphics.Color(0xFFEBEBEB)
                        )
                    }
                    
                    TranslationLanguage.entries.forEachIndexed { index, lang ->
                        val isLocked = lang != TranslationLanguage.ENGLISH && !uiState.isPremiumUser
                        val isSelected = uiState.translationLanguage == lang
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { 
                                    if (isLocked) {
                                        showSettingsSheet = false
                                        onNavigateToPremium?.invoke()
                                    } else {
                                        onSelectTranslation(lang) 
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) darkItemBg else androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(darkIconBg)
                                            .then(if (isLocked) Modifier.alpha(0.5f) else Modifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = androidx.compose.ui.graphics.Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLocked) com.example.ui.theme.GoldAccentLight else (if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFFEBEBEB)),
                                        modifier = if (isLocked) Modifier.alpha(0.5f) else Modifier
                                    )
                                }
                                if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Premium Language",
                                        tint = com.example.ui.theme.GoldAccentLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(androidx.compose.ui.graphics.Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = darkItemBg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // AI Explanation BottomSheet
    if (uiState.showAiExplanation) {
        ModalBottomSheet(
            onDismissRequest = onCloseExplanation,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "AI Tafsir (Explanation)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (uiState.isAiLoading) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (uiState.aiExplanation != null) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        item {
                            Text(
                                text = uiState.aiExplanation,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCardItem(
    ayah: Ayah,
    fontSizeSp: Float,
    translationLang: TranslationLanguage,
    isBookmarked: Boolean,
    isPremiumUser: Boolean = false,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onExplainAyah: () -> Unit,
    onCopyText: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.surahNumber}_${ayah.ayahNumber}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Verse Header Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Rosette Badge & Sajdah indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScallopedRosetteBadge(
                        text = toArabicDigits(ayah.ayahNumber),
                        modifier = Modifier.size(36.dp)
                    )

                    if (isSajdahVerse(ayah.surahNumber, ayah.ayahNumber)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MushafTealPrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MushafTealPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "۩ سَجْدَة",
                                fontFamily = AmiriFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MushafTealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Verse Audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Verse",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onExplainAyah) {
                        if (!isPremiumUser) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Advanced Tafsir (Premium)",
                                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                )
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Locked",
                                    tint = GoldAccentLight,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Explain Verse",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    IconButton(onClick = onCopyText) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Verse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pure function of the ayah text — memoised so scrolling does not
            // re-run the regex cleanup for every visible card.
            val formattedArabicText = remember(ayah.surahNumber, ayah.ayahNumber, ayah.textArabic) {
                com.example.util.QuranUtils.formatAyahText(ayah.surahNumber, ayah.ayahNumber, ayah.textArabic)
            }

            // Arabic Text
            Text(
                text = formattedArabicText,
                fontFamily = AmiriFontFamily,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.7f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Translation Text
            val activeTranslation = ayah.translations[translationLang.id] ?: ayah.translations["en"] ?: ""

            Text(
                text = activeTranslation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            // Inline Tafsir
            if (ayah.tafsir != null) {
                var showTafsir by remember { mutableStateOf(false) }
                val isTafsirLocked = ayah.surahNumber > 3 && !isPremiumUser
                
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp), 
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick = { 
                        if (isTafsirLocked) {
                            onExplainAyah() // This already navigates to Premium if locked in the parent callback
                        } else {
                            showTafsir = !showTafsir 
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("tafsir_toggle_button")
                ) {
                    if (isTafsirLocked) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Premium Feature",
                            tint = com.example.ui.theme.GoldAccentLight,
                            modifier = Modifier.size(16.dp).padding(end = 6.dp)
                        )
                        Text(
                            text = "Unlock Tafsir (Premium)",
                            color = com.example.ui.theme.GoldAccentLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = if (showTafsir) "Hide Tafsir" else "Read Tafsir",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = showTafsir && !isTafsirLocked,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = ayah.tafsir,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("tafsir_text")
                    )
                }
            }
        }
    }
}
