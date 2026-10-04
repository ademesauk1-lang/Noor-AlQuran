package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DarkModeOption
import com.example.data.model.TranslationLanguage
import com.example.ui.state.QuranUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build

@Composable
fun ProfileScreen(
    uiState: QuranUiState,
    onUpdateFontSize: (Float) -> Unit,
    onSelectDarkMode: (DarkModeOption) -> Unit,
    onSelectTranslationLanguage: (TranslationLanguage) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDailyAyah: (Boolean) -> Unit,
    onToggleAudioAlerts: (Boolean) -> Unit,
    onSignOut: () -> Unit = {},
    onTriggerBackup: (() -> Unit)? = null,
    onRestoreBackup: (() -> Unit)? = null,
    onNavigateToTab: ((com.example.data.model.AppTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardContainerColor = MaterialTheme.colorScheme.surface
    val innerSurfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val textColorPrimary = MaterialTheme.colorScheme.onSurface
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onToggleDailyAyah(isGranted)
        if (isGranted) {
            com.example.util.DailyInspirationWorker.scheduleDailyNotification(context)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Settings & Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
                )
                Text(
                    text = "Manage your preferences, display mode, and cloud backup",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColorSecondary
                )
            }
        }

        // Account Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!uiState.isUserSignedIn) {
                            onNavigateToTab?.invoke(com.example.data.model.AppTab.AUTH)
                        }
                    }
                    .testTag("profile_account_card"),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isUserSignedIn) Color(0xFF2E7D32) else primaryColor,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isUserSignedIn) (uiState.currentUserEmail ?: "Account & Sync") else "Sign In / Register",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColorPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (uiState.isUserSignedIn) "Signed in • Reading progress synced" else "Sign in to backup bookmarks and reading progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColorSecondary
                            )
                        }
                    }
                    if (uiState.isUserSignedIn) {
                        TextButton(onClick = onSignOut) {
                            Text(
                                text = "Sign Out",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { onNavigateToTab?.invoke(com.example.data.model.AppTab.AUTH) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Supabase Cloud Sync & Backup Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Cloud Sync",
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cloud Backup & Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColorPrimary
                            )
                            Text(
                                text = "Backup reading position & bookmarks securely",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColorSecondary
                            )
                        }
                    }

                    // Sync Status Banner
                    Surface(
                        color = innerSurfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Last Read: Surah ${uiState.lastRead.surahId} • ${uiState.bookmarkedAyahs.size} Bookmarks",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColorPrimary
                                )
                                if (uiState.lastSyncTimeFormatted != null) {
                                    Text(
                                        text = "Synced: ${uiState.lastSyncTimeFormatted}",
                                        fontSize = 11.sp,
                                        color = GoldAccentLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (uiState.syncStatusMessage != null) {
                                Text(
                                    text = uiState.syncStatusMessage ?: "",
                                    fontSize = 11.sp,
                                    color = primaryColor
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onTriggerBackup?.invoke() },
                            enabled = !uiState.isSyncing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Backup",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onRestoreBackup?.invoke() },
                            enabled = !uiState.isSyncing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, primaryColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Restore",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Data", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Premium Subscription Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Premium Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab?.invoke(com.example.data.model.AppTab.PREMIUM) }
                        .testTag("profile_premium_card"),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (uiState.isPremiumUser) GoldAccentLight else primaryColor.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isPremiumUser) GoldAccentLight.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPremiumUser) Icons.Default.Check else Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (uiState.isPremiumUser) GoldAccentLight else primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (uiState.isPremiumUser) "Noor Al-Quran Premium Active" else "Premium Subscription",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isPremiumUser) GoldAccentLight else textColorPrimary
                                    )
                                    Text(
                                        text = if (uiState.isPremiumUser) "All 30+ Qaris & AI Tafsir Unlocked" else "Unlock 30+ Qaris, Offline recitations & AI Tafsir",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColorSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.isPremiumUser) GoldAccentLight else primaryColor
                            ) {
                                Text(
                                    text = if (uiState.isPremiumUser) "Active" else "Manage Plan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isPremiumUser) Color.Black else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                Text("30+ World Reciters", fontSize = 11.sp, color = textColorSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                Text("Offline Audio", fontSize = 11.sp, color = textColorSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                Text("AI Tafsir", fontSize = 11.sp, color = textColorSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Section: General & Display
        item {
            Text(
                text = "General & Display",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Settings: Font Size
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = primaryColor)
                        Text("Arabic Font Size", fontWeight = FontWeight.Bold, color = textColorPrimary)
                    }
                    
                    Surface(
                        color = innerSurfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = AmiriFontFamily,
                            fontSize = uiState.arabicFontSizeSp.sp,
                            textAlign = TextAlign.Center,
                            color = textColorPrimary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("A", fontSize = 16.sp, color = textColorSecondary)
                        Slider(
                            value = uiState.arabicFontSizeSp,
                            onValueChange = onUpdateFontSize,
                            valueRange = 24f..48f,
                            steps = 6,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor,
                                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Text("A", fontSize = 24.sp, color = textColorSecondary)
                    }
                }
            }
        }

        // Settings: Translation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = primaryColor)
                        Text("Translation Language", fontWeight = FontWeight.Bold, color = textColorPrimary)
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TranslationLanguage.values().forEach { lang ->
                            val isLocked = !uiState.isPremiumUser && lang != TranslationLanguage.ENGLISH
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { 
                                        if (isLocked) {
                                            onNavigateToTab?.invoke(com.example.data.model.AppTab.PREMIUM)
                                        } else {
                                            onSelectTranslationLanguage(lang) 
                                        }
                                    }
                                    .background(if (uiState.translationLanguage == lang) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = lang.displayName,
                                        color = if (uiState.translationLanguage == lang) primaryColor else textColorPrimary,
                                        fontWeight = if (uiState.translationLanguage == lang) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isLocked) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Premium Feature",
                                            tint = GoldAccentLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = uiState.translationLanguage == lang,
                                    onClick = { 
                                        if (isLocked) {
                                            onNavigateToTab?.invoke(com.example.data.model.AppTab.PREMIUM)
                                        } else {
                                            onSelectTranslationLanguage(lang) 
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = primaryColor,
                                        unselectedColor = textColorSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings: Theme / Display Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = primaryColor)
                            Text("Theme / Display Mode", fontWeight = FontWeight.Bold, color = textColorPrimary)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionCard(
                            title = "System",
                            icon = Icons.Default.SettingsSuggest,
                            isSelected = uiState.darkModeOption == DarkModeOption.SYSTEM,
                            onClick = { onSelectDarkMode(DarkModeOption.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            title = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = uiState.darkModeOption == DarkModeOption.LIGHT,
                            onClick = { onSelectDarkMode(DarkModeOption.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            title = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = uiState.darkModeOption == DarkModeOption.DARK,
                            onClick = { onSelectDarkMode(DarkModeOption.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Notifications
        item {
            Text(
                text = "Notifications & Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("General Notifications", fontWeight = FontWeight.Bold, color = textColorPrimary)
                                Text("App updates & reading goals", style = MaterialTheme.typography.bodySmall, color = textColorSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = primaryColor)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Daily Ayah Reminders", fontWeight = FontWeight.Bold, color = textColorPrimary)
                                Text("Receive daily inspirational Quran verses", style = MaterialTheme.typography.bodySmall, color = textColorSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.dailyAyahReminders,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggleDailyAyah(enabled)
                                    if (enabled) {
                                        com.example.util.DailyInspirationWorker.scheduleDailyNotification(context)
                                    } else {
                                        com.example.util.DailyInspirationWorker.cancelDailyNotification(context)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = primaryColor)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Audio Download Alerts", fontWeight = FontWeight.Bold, color = textColorPrimary)
                                Text("Alert when downloading offline recitations", style = MaterialTheme.typography.bodySmall, color = textColorSecondary)
                            }
                        }
                        Switch(
                            checked = uiState.audioDownloadAlerts,
                            onCheckedChange = onToggleAudioAlerts,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = primaryColor)
                        )
                    }
                }
            }
        }

        // About & Support Section
        item {
            Text(
                text = "About & Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    AboutRow(icon = Icons.Default.Info, title = "About Noor Al-Quran", description = "Version 1.0.0 • Madani Script", onClick = { onNavigateToTab?.invoke(com.example.data.model.AppTab.ABOUT) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    AboutRow(icon = Icons.Default.Share, title = "Share with Friends", description = "Share the rewards of Quran reading", onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "Read and listen to the Holy Quran with Noor Al-Quran app! https://quran.aistudio.com")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    AboutRow(icon = Icons.AutoMirrored.Filled.Help, title = "Help & Support", description = "Contact our support team", onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@quran.aistudio.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Support Request: Noor Al-Quran App")
                        }
                        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                    })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    AboutRow(icon = Icons.Default.Info, title = "Privacy Policy & Terms", description = "Read our terms of service and privacy policy", onClick = {
                        onNavigateToTab?.invoke(com.example.data.model.AppTab.PRIVACY_POLICY)
                    })
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}

@Composable
private fun AboutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
