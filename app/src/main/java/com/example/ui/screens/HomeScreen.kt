package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.model.Qari
import com.example.R
import com.example.data.model.AppTab
import com.example.data.repository.QuranData
import com.example.ui.state.QuranUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

/**
 * Bundled fallback used as the initial value of the daily-verse fetch so the
 * home screen never blocks on network/disk I/O.
 */
private val FallbackDailyVerse = com.example.data.model.Ayah(
    surahNumber = 1,
    ayahNumber = 1,
    textArabic = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
    translations = mapOf(
        "en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful."
    )
)

@Composable
fun HomeScreen(
    uiState: QuranUiState,
    onOpenReader: (Int, Int) -> Unit,
    onPlayAudio: (Int, Int) -> Unit,
    onToggleBookmark: (Int, Int) -> Unit,
    onSelectTab: (AppTab) -> Unit,
    onSelectQari: ((Qari) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var dayOffset by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    // Never blocks: the screen renders immediately with the bundled
    // [FallbackDailyVerse] and swaps in the resolved verse once the
    // (IO-dispatched) fetch completes.
    val dailyAyah by produceState(
        initialValue = FallbackDailyVerse,
        key1 = uiState.translationLanguage.id,
        key2 = dayOffset
    ) {
        val (surahId, ayahNum) = QuranData.getDailyVerseInfo(dayOffset)
        val ayahs = QuranData.fetchAyahsForSurah(surahId, uiState.translationLanguage.id)
        val rawAyah = ayahs.find { it.ayahNumber == ayahNum } ?: ayahs.firstOrNull()
        if (rawAyah != null) {
            value = rawAyah.copy(
                textArabic = com.example.util.QuranUtils.formatAyahText(
                    rawAyah.surahNumber,
                    rawAyah.ayahNumber,
                    rawAyah.textArabic
                )
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Hero Banner Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("home_hero_banner"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F382C),
                                    Color(0xFF1B4D3E),
                                    Color(0xFF0D281E)
                                )
                            )
                        )
                ) {
                    AsyncImage(
                        model = R.drawable.app_logo,
                        contentDescription = "Quran Hero Banner",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xEE0B1E17)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccentLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "القرآن الكريم",
                                fontFamily = AmiriFontFamily,
                                fontSize = 22.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Noor Al-Quran",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Read, reflect, and listen to the Holy Quran",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickNavButton(
                        icon = Icons.Default.GridView,
                        label = "Surahs",
                        onClick = { onSelectTab(AppTab.SURAHS) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    QuickNavButton(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "Reader",
                        onClick = { onSelectTab(AppTab.READER) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    QuickNavButton(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        label = "Full Quran",
                        onClick = { onSelectTab(AppTab.MUSHAF) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    QuickNavButton(
                        icon = Icons.Default.RecordVoiceOver,
                        label = "Reciters",
                        onClick = { onSelectTab(AppTab.RECITERS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Featured Reciters Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("featured_reciters_section"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Featured Reciters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Featured Reciters (Qaris)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectTab(AppTab.RECITERS) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Stable, remembered collections: allocating these on every
                // recomposition invalidated the whole lazy list.
                val topQaris = remember { QuranData.qariList.take(8) }
                val selectedQariId = uiState.selectedQari.id
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = topQaris,
                        key = { qari -> qari.id },
                        contentType = { "qari" }
                    ) { qari ->
                        val isSelected = selectedQariId == qari.id
                        val imageModel: Any = qari.imageResId ?: qari.photoUrl

                        Card(
                            modifier = Modifier
                                .width(135.dp)
                                .clickable { onSelectQari?.invoke(qari) }
                                .testTag("featured_qari_${qari.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1B382B) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(18.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccentLight) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = qari.name,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2A2A35)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(GoldAccentLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = qari.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = qari.country,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GoldAccentLight else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isSelected) "Active Qari" else "Select Qari",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Resume Reading Card
        item {
            val lastRead = uiState.lastRead
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenReader(lastRead.surahId, lastRead.ayahNumber) }
                    .testTag("resume_reading_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Continue Reading",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${lastRead.surahNameEnglish} (${lastRead.surahNameArabic})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ayah ${lastRead.ayahNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = "Continue",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Verse of the Day
        item {
            val verse = dailyAyah
            val isBookmarked = uiState.bookmarkedAyahs.contains("${verse.surahNumber}:${verse.ayahNumber}")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verse_of_the_day_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccentLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verse of the Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(onClick = { onSelectTab(AppTab.INSPIRATION) }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Inspiration Screen",
                                    tint = GoldAccentLight
                                )
                            }
                            IconButton(onClick = { dayOffset++ }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Next Verse",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onPlayAudio(verse.surahNumber, verse.ayahNumber) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen Verse",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onToggleBookmark(verse.surahNumber, verse.ayahNumber) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark Verse",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val cleanVerseArabic = remember(verse) {
                        com.example.util.QuranUtils.formatAyahText(
                            verse.surahNumber,
                            verse.ayahNumber,
                            verse.textArabic
                        )
                    }
                    val dailySurahInfo = remember(verse) {
                        QuranData.surahById(verse.surahNumber)
                    }

                    Text(
                        text = cleanVerseArabic,
                        fontFamily = AmiriFontFamily,
                        fontSize = 24.sp,
                        lineHeight = 42.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Text(
                        text = verse.translations[uiState.translationLanguage.id] ?: verse.translations["en"] ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "— Surah ${dailySurahInfo?.nameEnglish ?: ""} [${verse.surahNumber}:${verse.ayahNumber}]",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // Popular Surahs Quick Jump
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Popular Surahs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val popularSurahs = remember {
                    val popularIds = setOf(1, 36, 18, 55, 67, 112)
                    QuranData.surahs.filter { it.id in popularIds }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = popularSurahs,
                        key = { surah -> surah.id },
                        contentType = { "surah" }
                    ) { surah ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { onOpenReader(surah.id, 1) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = surah.nameArabic,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = surah.nameEnglish,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${surah.versesCount} Verses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // App Overview Stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "114",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Surahs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "30",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Juz Parts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.bookmarkedAyahs.size}",
                            style = MaterialTheme.typography.displayMedium,
                            color = GoldAccentLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickNavButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
