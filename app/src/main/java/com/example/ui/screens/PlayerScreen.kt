package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.R
import com.example.data.model.Qari
import com.example.data.repository.QuranData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.state.PlaybackProgress
import com.example.ui.state.QuranUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import com.example.data.model.Surah
import com.example.ui.state.RepeatMode
import com.example.util.QuranUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: QuranUiState,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSeekAudio: (Float) -> Unit,
    onSelectQari: (Qari) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleSpeed: () -> Unit,
    onSelectRepeatMode: (RepeatMode) -> Unit = {},
    onSelectPlaybackSpeed: (Float) -> Unit = {},
    onSelectBackgroundSound: (com.example.data.model.BackgroundSound) -> Unit = {},
    onDownloadAudio: (Int, String) -> Unit = { _, _ -> },
    onNavigateToPremium: (() -> Unit)? = null,
    onPlaySurah: (Int) -> Unit = {},
    playbackProgressFlow: StateFlow<PlaybackProgress> = EmptyPlaybackProgress,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSurah = QuranData.surahById(uiState.audioSurahId) ?: QuranData.surahs[0]
    var showQariDropdown by remember { mutableStateOf(false) }
    var showRepeatSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showPlaylistSheet by remember { mutableStateOf(false) }
    var playlistSearchQuery by remember { mutableStateOf("") }
    var fallbackDialogSurah by remember { mutableStateOf<Surah?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Qari Selection Header
        Box {
            OutlinedButton(
                onClick = { showQariDropdown = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("qari_selector_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.selectedQari.name,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (showQariDropdown) {
                val darkSheetBg = androidx.compose.ui.graphics.Color(0xFF1B1B22)
                val darkItemBg = androidx.compose.ui.graphics.Color(0xFF2A2A35)
                val darkIconBg = androidx.compose.ui.graphics.Color(0xFF353642)
                
                ModalBottomSheet(
                    onDismissRequest = { showQariDropdown = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = darkSheetBg,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Select Reciter",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 32.dp)
                        ) {
                            itemsIndexed(
                                items = QuranData.qariList,
                                key = { _, qari -> qari.id },
                                contentType = { _, _ -> "qari" }
                            ) { index, qari ->
                                val isSelected = qari.id == uiState.selectedQari.id
                                val isLocked = index >= 10 && !uiState.isPremiumUser
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            if (isLocked) {
                                                showQariDropdown = false
                                                onNavigateToPremium?.invoke()
                                            } else {
                                                showQariDropdown = false
                                                onSelectQari(qari)
                                            }
                                        }
                                        .background(
                                            if (isSelected) darkItemBg else androidx.compose.ui.graphics.Color.Transparent
                                        )
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val qariImageModel: Any = qari.imageResId 
                                        ?: if (qari.photoUrl.isNotBlank()) qari.photoUrl 
                                        else "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80"
                                    AsyncImage(
                                        model = qariImageModel,
                                        contentDescription = qari.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(darkIconBg)
                                            .then(if (isLocked) Modifier.alpha(0.5f) else Modifier)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f).then(if (isLocked) Modifier.alpha(0.5f) else Modifier)) {
                                        Text(
                                            text = qari.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFFEBEBEB)
                                        )
                                        Text(
                                            text = "${qari.country} • Murattal",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = androidx.compose.ui.graphics.Color(0xFFA0A0AB)
                                        )
                                    }
                                    if (isLocked) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Premium Reciter",
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
                                                imageVector = androidx.compose.material.icons.Icons.Filled.PlayArrow,
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
                }
            }
        }

        // Center Album Artwork
        Card(
            modifier = Modifier
                .size(220.dp)
                .testTag("quran_audio_artwork"),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A35)),
            border = BorderStroke(3.dp, GoldAccentLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val selectedQariModel: Any = uiState.selectedQari.imageResId
                    ?: if (uiState.selectedQari.photoUrl.isNotBlank()) uiState.selectedQari.photoUrl
                    else "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80"
                AsyncImage(
                    model = selectedQariModel,
                    contentDescription = "Reciter Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Title Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = currentSurah.nameArabic,
                fontFamily = AmiriFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Surah ${currentSurah.nameEnglish}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${currentSurah.englishTranslation} • ${currentSurah.versesCount} Ayahs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Download Audio Action Button (Locked / Premium state)
            val audioId = "${currentSurah.id}_${uiState.selectedQari.id}"
            val isDownloaded = uiState.downloadedAudios.contains(audioId)
            OutlinedButton(
                onClick = {
                    if (!uiState.isPremiumUser) {
                        onNavigateToPremium?.invoke()
                    } else if (!isDownloaded && !uiState.isAudioDownloading) {
                        onDownloadAudio(currentSurah.id, uiState.selectedQari.id)
                        Toast.makeText(context, "Downloading Surah ${currentSurah.nameEnglish}...", Toast.LENGTH_SHORT).show()
                    } else if (isDownloaded) {
                        Toast.makeText(context, "Audio already saved offline!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("download_audio_button")
            ) {
                if (!uiState.isPremiumUser) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Locked Feature",
                        tint = GoldAccentLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Audio (Premium)",
                        color = GoldAccentLight,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Audio Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Audio Saved Offline ✓",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else if (uiState.isAudioDownloading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Downloading... ${uiState.audioDownloadProgress}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Audio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Audio",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Seek Bar Progress (isolated: only this widget recomposes on ticks)
        PlaybackProgressSection(
            playbackProgressFlow = playbackProgressFlow,
            onSeekAudio = onSeekAudio
        )

        // Audio Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showRepeatSheet = true },
                modifier = Modifier.size(48.dp)
            ) {
                val icon = when (uiState.repeatMode) {
                    RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                    RepeatMode.SHUFFLE -> Icons.Default.Shuffle
                    else -> Icons.Default.Repeat
                }
                val isTinted = uiState.repeatMode != RepeatMode.REPEAT_OFF
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat Mode",
                    tint = if (isTinted) GoldAccentLight else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onPreviousTrack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Surah",
                    modifier = Modifier.size(36.dp)
                )
            }

            FloatingActionButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("player_play_pause_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause Audio",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = onNextTrack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Surah",
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { showPlaylistSheet = true },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("playlist_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = "Searchable Playlist",
                    tint = if (showPlaylistSheet) GoldAccentLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = { showSpeedSheet = true },
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = "${uiState.playbackSpeed}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Repeat / Shuffle Mode Modal Sheet
        if (showRepeatSheet) {
            val darkSheetBg = Color(0xFF1B1B22)
            ModalBottomSheet(
                onDismissRequest = { showRepeatSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = darkSheetBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF353642))
                    )

                    Text(
                        text = "Repeat & Playback Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Configure loop or shuffle preferences for your recitation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA0A0AB),
                        textAlign = TextAlign.Center
                    )

                    val modes = listOf(
                        RepeatMode.REPEAT_OFF to Pair("Repeat Off", "Play through Surahs continuously"),
                        RepeatMode.REPEAT_ALL to Pair("Repeat Surah", "Loop current Surah continuously"),
                        RepeatMode.REPEAT_ONE to Pair("Repeat Ayah", "Loop single Ayah for memorization"),
                        RepeatMode.SHUFFLE to Pair("Shuffle Mode", "Play random Surahs in playlist")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        modes.forEach { (mode, descPair) ->
                            val isSelected = uiState.repeatMode == mode
                            val (title, sub) = descPair
                            val modeIcon = when (mode) {
                                RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                RepeatMode.SHUFFLE -> Icons.Default.Shuffle
                                else -> Icons.Default.Repeat
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        onSelectRepeatMode(mode)
                                        showRepeatSheet = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF2A2A35) else Color(0xFF22222B)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, GoldAccentLight) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) GoldAccentLight.copy(alpha = 0.2f) else Color(0xFF353642)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = modeIcon,
                                                contentDescription = title,
                                                tint = if (isSelected) GoldAccentLight else Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFEBEBEB)
                                            )
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFA0A0AB)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = GoldAccentLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Playback Speed Modal Sheet
        if (showSpeedSheet) {
            val darkSheetBg = Color(0xFF1B1B22)
            ModalBottomSheet(
                onDismissRequest = { showSpeedSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = darkSheetBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF353642))
                    )

                    Text(
                        text = "Playback Speed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Select recitation speed for optimal listening & memorization",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA0A0AB),
                        textAlign = TextAlign.Center
                    )

                    val speedOptions = listOf(
                        0.5f to "0.5x — Very Slow Recitation",
                        0.75f to "0.75x — Slow Recitation",
                        1.0f to "1.0x — Normal Speed",
                        1.25f to "1.25x — Fast Recitation",
                        1.5f to "1.5x — Very Fast Recitation"
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        speedOptions.forEach { (speed, label) ->
                            val isSelected = uiState.playbackSpeed == speed
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        onSelectPlaybackSpeed(speed)
                                        onToggleSpeed()
                                        showSpeedSheet = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF2A2A35) else Color(0xFF22222B)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, GoldAccentLight) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) GoldAccentLight.copy(alpha = 0.2f) else Color(0xFF353642)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) GoldAccentLight else Color.White
                                            )
                                        }

                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFEBEBEB)
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected Speed",
                                            tint = GoldAccentLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Searchable Playlist Bottom Sheet
        if (showPlaylistSheet) {
            val darkSheetBg = Color(0xFF1B1B22)
            val darkItemBg = Color(0xFF22222B)
            val darkSelectedBg = Color(0xFF2A2A35)

            val filteredSurahs = remember(playlistSearchQuery) {
                QuranData.surahs.filter { surah ->
                    playlistSearchQuery.isBlank() ||
                        surah.nameEnglish.contains(playlistSearchQuery, ignoreCase = true) ||
                        surah.nameArabic.contains(playlistSearchQuery, ignoreCase = true) ||
                        surah.englishTranslation.contains(playlistSearchQuery, ignoreCase = true) ||
                        surah.id.toString() == playlistSearchQuery.trim()
                }
            }

            ModalBottomSheet(
                onDismissRequest = { showPlaylistSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = darkSheetBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF353642))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Surah Playlist",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Reciter: ${uiState.selectedQari.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0A0AB)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A2A35))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${filteredSurahs.size} / 114 Surahs",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccentLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = playlistSearchQuery,
                        onValueChange = { playlistSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playlist_search_field"),
                        placeholder = {
                            Text(
                                text = "Search surah name or number...",
                                color = Color(0xFFA0A0AB),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Surahs",
                                tint = Color(0xFFA0A0AB)
                            )
                        },
                        trailingIcon = {
                            if (playlistSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { playlistSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFFA0A0AB)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccentLight,
                            unfocusedBorderColor = Color(0xFF353642),
                            focusedContainerColor = Color(0xFF22222B),
                            unfocusedContainerColor = Color(0xFF22222B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldAccentLight
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredSurahs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Surahs found for \"$playlistSearchQuery\"",
                                color = Color(0xFFA0A0AB),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp)
                        ) {
                            items(filteredSurahs, key = { it.id }) { surah ->
                                val isCurrent = surah.id == uiState.audioSurahId
                                val isRecorded = QuranUtils.isSurahRecorded(uiState.selectedQari, surah.id)
                                val isDownloaded = uiState.downloadedAudios.contains("${surah.id}_${uiState.selectedQari.id}")

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .then(if (!isRecorded) Modifier.alpha(0.4f) else Modifier)
                                        .clickable {
                                            if (isRecorded) {
                                                onPlaySurah(surah.id)
                                                showPlaylistSheet = false
                                            } else {
                                                fallbackDialogSurah = surah
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrent) darkSelectedBg else darkItemBg
                                    ),
                                    border = if (isCurrent) BorderStroke(1.5.dp, GoldAccentLight) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCurrent) GoldAccentLight.copy(alpha = 0.2f)
                                                        else Color(0xFF353642)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${surah.id}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCurrent) GoldAccentLight else Color.White
                                                )
                                            }

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = surah.nameEnglish,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isCurrent) GoldAccentLight else Color.White
                                                    )
                                                    if (isCurrent) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "• Playing",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = GoldAccentLight,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else if (isDownloaded) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.OfflinePin,
                                                            contentDescription = "Cached Offline",
                                                            tint = Color(0xFF4CAF50),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "${surah.englishTranslation} • ${surah.versesCount} Ayahs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFA0A0AB)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = surah.nameArabic,
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) GoldAccentLight else Color.White
                                            )

                                            if (!isRecorded) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF3F2B2B))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Unrecorded",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFFFF8A8A)
                                                    )
                                                }
                                            } else if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Currently Playing",
                                                    tint = GoldAccentLight,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Fallback Dialog for Unrecorded Surahs
        if (fallbackDialogSurah != null) {
            val fallbackSurah = fallbackDialogSurah!!
            val misharyQari = remember { QuranData.qariList.find { it.id == "mishary" } ?: QuranData.qariList[0] }

            AlertDialog(
                onDismissRequest = { fallbackDialogSurah = null },
                containerColor = Color(0xFF1B1B22),
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = GoldAccentLight,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Recitation Unavailable",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Sheikh ${uiState.selectedQari.name} has not recorded Surah ${fallbackSurah.nameEnglish} (${fallbackSurah.nameArabic}).\n\nWould you like to play this Surah recited by Sheikh Mishary Rashid Alafasy instead?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD1D1D6),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            onSelectQari(misharyQari)
                            onPlaySurah(fallbackSurah.id)
                            fallbackDialogSurah = null
                            showPlaylistSheet = false
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = GoldAccentLight,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Play with Sheikh Mishary",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { fallbackDialogSurah = null },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF353642))
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFFA0A0AB)
                        )
                    }
                }
            )
        }
    }
}

/**
 * Default (idle) progress used by previews/tests that do not wire a real player.
 */
private val EmptyPlaybackProgress: StateFlow<PlaybackProgress> =
    MutableStateFlow(PlaybackProgress())

/**
 * Seek bar + elapsed/remaining labels.
 *
 * Only this composable collects the high-frequency [PlaybackProgress] flow, so
 * the twice-per-second position updates recompose ~40 lines of UI instead of the
 * entire player screen.
 */
@Composable
private fun PlaybackProgressSection(
    playbackProgressFlow: StateFlow<PlaybackProgress>,
    onSeekAudio: (Float) -> Unit
) {
    val progress by playbackProgressFlow.collectAsStateWithLifecycle()
    // While the user drags, the slider follows the finger instead of snapping
    // back to the last emitted position.
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val displayed = dragValue ?: progress.progress

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Slider(
            value = displayed.coerceIn(0f, 1f),
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let(onSeekAudio)
                dragValue = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_progress_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = QuranUtils.formatAudioDuration(
                    if (dragValue != null) {
                        (displayed * progress.durationSeconds).toInt()
                    } else {
                        progress.currentSeconds
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = QuranUtils.formatAudioDuration(progress.durationSeconds),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
