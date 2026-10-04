package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.QuranData
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

/**
 * Shared Jump to Page, Surah, or Juz Location Picker Bottom Sheet
 * Fully synced to Light and Dark mode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuranLocationPickerBottomSheet(
    currentPage: Int = 1,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onJumpToPage: ((Int) -> Unit)? = null,
    onJumpToSurah: (Int) -> Unit,
    onJumpToJuz: (Int) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var surahSearchQuery by remember { mutableStateOf("") }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = surfaceColor,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Title & Current Page / Surah Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Jump to Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "Select Page, Surah, or Juz",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Page $currentPage / 604",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Custom Sleek TabRow
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = surfaceVariantColor,
                contentColor = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val tabs = listOf("Page", "Surah", "Juz")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) primaryColor else onSurfaceVariantColor
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                // Page Input Tab
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = pageInput,
                            onValueChange = { pageInput = it },
                            label = { Text("Enter Page Number (1 - 604)", color = onSurfaceVariantColor) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = outlineVariantColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("jump_page_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Quick Page Shortcuts",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariantColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val quickPages = listOf(
                            1 to "Al-Fatiha (1)",
                            50 to "Page 50",
                            100 to "Page 100",
                            200 to "Page 200",
                            300 to "Page 300",
                            400 to "Page 400",
                            500 to "Page 500",
                            604 to "An-Nas (604)"
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickPages.forEach { (page, label) ->
                                val isSelected = pageInput == page.toString()
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) primaryColor else surfaceVariantColor,
                                    modifier = Modifier.clickable { pageInput = page.toString() }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurfaceColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val target = pageInput.toIntOrNull()
                                if (target != null && target in 1..604) {
                                    onJumpToPage?.invoke(target)
                                    onDismiss()
                                }
                            },
                            enabled = pageInput.toIntOrNull() in 1..604,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Go to Page",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Surah Selector Tab
                1 -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = surahSearchQuery,
                            onValueChange = { surahSearchQuery = it },
                            placeholder = { Text("Search Surah by name or number...", color = onSurfaceVariantColor) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = primaryColor)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = outlineVariantColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )

                        val filteredSurahs = remember(surahSearchQuery) {
                            if (surahSearchQuery.isBlank()) QuranData.surahs
                            else QuranData.surahs.filter {
                                it.nameEnglish.contains(surahSearchQuery, ignoreCase = true) ||
                                it.nameArabic.contains(surahSearchQuery, ignoreCase = true) ||
                                it.id.toString() == surahSearchQuery.trim()
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(filteredSurahs, key = { it.id }) { surah ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onJumpToSurah(surah.id)
                                                onDismiss()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = surah.id.toString(),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Text(
                                                    text = surah.nameEnglish,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = onSurfaceColor
                                                )
                                                Text(
                                                    text = "${surah.revelationType} • ${surah.versesCount} Ayahs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = onSurfaceVariantColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = surah.nameArabic,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    }
                                    HorizontalDivider(
                                        color = outlineVariantColor.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Juz Selector Tab
                2 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(QuranData.juzList, key = { it.juzNumber }) { juz ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onJumpToJuz(juz.juzNumber)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = GoldAccentLight.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, GoldAccentLight.copy(alpha = 0.6f)),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = juz.juzNumber.toString(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccentLight
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Juz ${juz.juzNumber}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = onSurfaceColor
                                            )
                                            Text(
                                                text = "Starts at ${juz.startSurahName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = onSurfaceVariantColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = juz.nameArabic,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccentLight
                                    )
                                }
                                HorizontalDivider(
                                    color = outlineVariantColor.copy(alpha = 0.5f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sticky Pill-Shaped Close Button
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, outlineVariantColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
