package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.ui.state.QuranUiState
import com.example.ui.state.PlaybackProgress
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppTab
import com.example.data.repository.QuranData
import com.example.ui.state.QuranViewModel
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    viewModel: QuranViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiStateState = viewModel.uiState.collectAsStateWithLifecycle()

    // Each value is observed individually through `derivedStateOf`. Collecting
    // the aggregate state and reading it directly would invalidate this whole
    // screen (top bar + mini player + active screen) on every single state
    // change — the main cause of the navigation/scroll stutter.
    val currentTab by remember(uiStateState) { derivedStateOf { uiStateState.value.currentTab } }
    val isDataLoading by remember(uiStateState) { derivedStateOf { uiStateState.value.isDataLoading } }
    val isOnline by remember(uiStateState) { derivedStateOf { uiStateState.value.isOnline } }
    val offlineNotice by remember(uiStateState) { derivedStateOf { uiStateState.value.offlineNotice } }
    val userMessage by remember(uiStateState) { derivedStateOf { uiStateState.value.userMessage } }
    val isPremiumUser by remember(uiStateState) { derivedStateOf { uiStateState.value.isPremiumUser } }

    androidx.activity.compose.BackHandler(
        enabled = currentTab != AppTab.HOME && currentTab != AppTab.WELCOME
    ) {
        viewModel.popBackStack()
    }

    androidx.compose.runtime.LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    if (isDataLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            MainTopBar(
                visible = currentTab in TOP_BAR_TABS,
                isPremiumUser = isPremiumUser,
                isMushafTab = currentTab == AppTab.MUSHAF,
                isRecitersTab = currentTab == AppTab.RECITERS,
                onPremiumClick = { viewModel.navigateToPremium() },
                onMushafClick = { viewModel.selectTab(AppTab.MUSHAF) },
                onRecitersClick = { viewModel.selectTab(AppTab.RECITERS) }
            )
        },
        bottomBar = {
            Column {
                // Persistent mini player — its own composable so playback
                // changes never recompose the screen content.
                MiniAudioPlayerBar(
                    visible = currentTab != AppTab.PLAYER,
                    uiStateState = uiStateState,
                    onOpenPlayer = { viewModel.selectTab(AppTab.PLAYER) },
                    onTogglePlayPause = { viewModel.togglePlayPause() }
                )
                if (currentTab in BOTTOM_BAR_TABS) {
                    AppBottomNavigationBar(
                        currentTab = currentTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!isOnline) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Mode",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Offline Mode — Local Quran text & downloaded audio active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (offlineNotice != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = offlineNotice ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissOfflineNotice() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    TabHost(
                        uiStateState = uiStateState,
                        playbackProgressFlow = viewModel.playbackProgress,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/** Screens that display the shared top app bar. */
private val TOP_BAR_TABS = listOf(AppTab.HOME, AppTab.SURAHS, AppTab.JUZ, AppTab.RECITERS, AppTab.PROFILE)

/** Screens that display the bottom navigation bar. */
private val BOTTOM_BAR_TABS = listOf(
    AppTab.HOME, AppTab.SURAHS, AppTab.JUZ, AppTab.MUSHAF,
    AppTab.RECITERS, AppTab.PROFILE, AppTab.PLAYER, AppTab.READER, AppTab.INSPIRATION
)

/**
 * Renders the currently selected screen.
 *
 * The aggregate state is read here — in its own recomposition scope — so
 * screen-specific updates only invalidate the active screen, never the bars,
 * banners or navigation chrome.
 */
@Composable
private fun TabHost(
    uiStateState: androidx.compose.runtime.State<QuranUiState>,
    playbackProgressFlow: kotlinx.coroutines.flow.StateFlow<PlaybackProgress>,
    viewModel: QuranViewModel
) {
    val uiState = uiStateState.value
    when (uiState.currentTab) {
        AppTab.WELCOME -> WelcomeScreen(onGetStarted = { viewModel.completeWelcome() })
        AppTab.HOME -> HomeScreen(
            uiState = uiState,
            onOpenReader = { surahId, ayahNum -> viewModel.openReaderForSurah(surahId, ayahNum) },
            onPlayAudio = { surahId, ayahNum -> viewModel.playAudio(surahId, ayahNum) },
            onToggleBookmark = { surahId, ayahNum -> viewModel.toggleBookmark(surahId, ayahNum) },
            onSelectTab = { viewModel.selectTab(it) },
            onSelectQari = { viewModel.selectQari(it) }
        )
        AppTab.SURAHS -> SurahListScreen(
            uiState = uiState,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onFilterChange = { viewModel.setFilterType(it) },
            onOpenReader = { viewModel.openReaderForSurah(it) },
            onPlayAudio = { viewModel.playAudio(it) }
        )
        AppTab.JUZ -> JuzScreen(
            onOpenReaderForJuz = { surahId, ayahNum -> viewModel.openReaderForSurah(surahId, ayahNum) }
        )
        AppTab.MUSHAF -> MushafScreen(
            onNavigateBack = { viewModel.selectTab(AppTab.HOME) }
        )
        AppTab.READER -> ReaderScreen(
            uiState = uiState,
            onSelectSurah = { viewModel.openReaderForSurah(it) },
            onPlayAudio = { surahId, ayahNum -> viewModel.playAudio(surahId, ayahNum) },
            onToggleBookmark = { surahId, ayahNum -> viewModel.toggleBookmark(surahId, ayahNum) },
            onUpdateFontSize = { viewModel.updateFontSize(it) },
            onSelectTranslation = { viewModel.setTranslationLanguage(it) },
            onUpdateLastReadAyah = { viewModel.updateLastReadAyah(it) },
            onExplainAyah = { surahId, ayahNum -> viewModel.explainAyah(surahId, ayahNum) },
            onCloseExplanation = { viewModel.closeAiExplanation() },
            onNavigateToPremium = { viewModel.navigateToPremium() }
        )
        AppTab.PLAYER -> PlayerScreen(
            uiState = uiState,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNextTrack = { viewModel.nextAudioTrack() },
            onPreviousTrack = { viewModel.previousAudioTrack() },
            onSeekAudio = { viewModel.seekAudio(it) },
            onSelectQari = { viewModel.selectQari(it) },
            onToggleLoop = { viewModel.toggleLoopSurah() },
            onToggleSpeed = { viewModel.togglePlaybackSpeed() },
            onSelectRepeatMode = { viewModel.setRepeatMode(it) },
            onSelectPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
            onSelectBackgroundSound = { viewModel.setBackgroundSound(it) },
            onDownloadAudio = { surahId, qariId -> viewModel.downloadAudio(surahId, qariId) },
            onNavigateToPremium = { viewModel.selectTab(AppTab.PREMIUM) },
            onPlaySurah = { surahId -> viewModel.playAudio(surahId, 1) },
            playbackProgressFlow = playbackProgressFlow
        )
        AppTab.RECITERS -> RecitersScreen(
            uiState = uiState,
            onSearchQueryChange = { viewModel.updateReciterSearchQuery(it) },
            onSelectQari = { viewModel.selectQari(it) },
            onPlayAudioPreview = { qari ->
                viewModel.selectQari(qari)
                viewModel.playAudio(1, 1)
            }
        )
        AppTab.PROFILE -> ProfileScreen(
            uiState = uiState,
            onUpdateFontSize = { viewModel.updateFontSize(it) },
            onSelectDarkMode = { viewModel.setDarkModeOption(it) },
            onSelectTranslationLanguage = { viewModel.setTranslationLanguage(it) },
            onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
            onToggleDailyAyah = { viewModel.setDailyAyahReminders(it) },
            onToggleAudioAlerts = { viewModel.setAudioDownloadAlerts(it) },
            onSignOut = { viewModel.signOut() },
            onTriggerBackup = { viewModel.triggerCloudBackup() },
            onRestoreBackup = { viewModel.restoreFromCloudBackup() },
            onNavigateToTab = { tab ->
                when (tab) {
                    AppTab.PREMIUM -> viewModel.navigateToPremium()
                    AppTab.AUTH -> viewModel.navigateToAuth()
                    AppTab.ABOUT -> viewModel.navigateToAbout()
                    AppTab.PRIVACY_POLICY -> viewModel.navigateToPrivacyPolicy()
                    AppTab.TERMS_OF_SERVICE -> viewModel.navigateToTermsOfService()
                    else -> viewModel.selectTab(tab)
                }
            }
        )
        AppTab.PREMIUM -> PremiumUnlockScreen(
            uiState = uiState,
            onSelectPlan = { viewModel.selectSubscriptionPlan(it) },
            onStartTrial = { viewModel.unlockPremium() },
            onDismiss = { viewModel.popBackStack() }
        )
        AppTab.AUTH_CALLBACK -> AuthCallbackScreen(
            onNavigateHome = { viewModel.popBackStack() }
        )
        AppTab.AUTH -> AuthScreen(onNavigateHome = { viewModel.popBackStack() })
        AppTab.ABOUT -> AboutScreen(
            onNavigateBack = { viewModel.popBackStack() },
            onNavigateToPrivacyPolicy = { viewModel.navigateToPrivacyPolicy() },
            onNavigateToTermsOfService = { viewModel.navigateToTermsOfService() }
        )
        AppTab.PRIVACY_POLICY -> PrivacyPolicyScreen(onNavigateBack = { viewModel.popBackStack() })
        AppTab.TERMS_OF_SERVICE -> TermsOfServiceScreen(onNavigateBack = { viewModel.popBackStack() })
        AppTab.THEME_LAB -> ThemeLabScreen(
            uiState = uiState,
            onSelectDarkMode = { viewModel.setDarkModeOption(it) },
            onNavigateHome = { viewModel.selectTab(AppTab.HOME) }
        )
        AppTab.INSPIRATION -> InspirationScreen(
            onPlayAudio = { surahId, ayahNum -> viewModel.playAudio(surahId, ayahNum) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    visible: Boolean,
    isPremiumUser: Boolean,
    isMushafTab: Boolean,
    isRecitersTab: Boolean,
    onPremiumClick: () -> Unit,
    onMushafClick: () -> Unit,
    onRecitersClick: () -> Unit
) {
    if (!visible) return
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "القرآن الكريم",
                    fontFamily = AmiriFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Text(
                    text = "Noor Al-Quran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        },
        actions = {
            IconButton(
                onClick = onPremiumClick,
                modifier = Modifier.testTag("top_bar_premium_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Unlock Premium",
                    tint = if (isPremiumUser) GoldAccentLight else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onMushafClick,
                modifier = Modifier.testTag("top_bar_mushaf_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Full Mushaf 604",
                    tint = if (isMushafTab) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onRecitersClick,
                modifier = Modifier.testTag("top_bar_reciters_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Reciters",
                    tint = if (isRecitersTab) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Persistent mini audio player. Observes only the handful of playback fields it
 * actually renders, so a change in any other part of the state cannot
 * invalidate it (and vice versa).
 */
@Composable
private fun MiniAudioPlayerBar(
    visible: Boolean,
    uiStateState: androidx.compose.runtime.State<QuranUiState>,
    onOpenPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit
) {
    val showMiniPlayer by remember(uiStateState) {
        derivedStateOf { uiStateState.value.showAudioMiniPlayer }
    }
    if (!visible || !showMiniPlayer) return

    val audioSurahId by remember(uiStateState) { derivedStateOf { uiStateState.value.audioSurahId } }
    val isPlaying by remember(uiStateState) { derivedStateOf { uiStateState.value.isPlaying } }
    val selectedQari by remember(uiStateState) { derivedStateOf { uiStateState.value.selectedQari } }

    val currentSurah = remember(audioSurahId) {
        QuranData.surahById(audioSurahId) ?: QuranData.surahs[0]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onOpenPlayer() }
            .testTag("mini_audio_player_bar"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val miniQariModel: Any = selectedQari.imageResId
                    ?: if (selectedQari.photoUrl.isNotBlank()) selectedQari.photoUrl
                    else "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80"
                AsyncImage(
                    model = miniQariModel,
                    contentDescription = selectedQari.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Surah ${currentSurah.nameEnglish} • ${currentSurah.nameArabic}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                    Text(
                        text = "${selectedQari.name} • ${if (isPlaying) "Playing" else "Paused"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause Audio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavigationBar(
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NAV_ITEMS.forEach { item ->
            val selected = currentTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.tab.label
                    )
                },
                label = {
                    Text(
                        text = item.tab.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

/** Static navigation model — allocated once for the whole process. */
private val NAV_ITEMS = listOf(
    TabNavItem(AppTab.HOME, Icons.Default.Home, "home_tab"),
    TabNavItem(AppTab.READER, Icons.AutoMirrored.Filled.List, "reader_tab"),
    TabNavItem(AppTab.INSPIRATION, Icons.Filled.Star, "inspiration_tab"),
    TabNavItem(AppTab.PLAYER, Icons.Default.Headphones, "player_tab"),
    TabNavItem(AppTab.PROFILE, Icons.Default.Person, "profile_tab")
)

private data class TabNavItem(
    val tab: AppTab,
    val icon: ImageVector,
    val testTag: String
)