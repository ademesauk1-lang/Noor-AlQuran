package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.model.Qari
import com.example.data.repository.QuranData
import com.example.ui.state.QuranUiState
import androidx.compose.ui.graphics.Color

@Composable
fun RecitersScreen(
    uiState: QuranUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectQari: (Qari) -> Unit,
    onPlayAudioPreview: (Qari) -> Unit,
    onNavigateToPremium: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val searchQuery = uiState.reciterSearchQuery
    val activeQari = uiState.selectedQari

    val filteredReciters = QuranData.qariList.filter { qari ->
        searchQuery.isBlank() ||
                qari.name.contains(searchQuery, ignoreCase = true) ||
                qari.country.contains(searchQuery, ignoreCase = true)
    }

    val darkSheetBg = Color(0xFF1B1B22)
    val darkItemBg = Color(0xFF2A2A35)
    val darkIconBg = Color(0xFF353642)
    val textWhite = Color.White
    val textGray = Color(0xFFA0A0AB)
    val textLight = Color(0xFFEBEBEB)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(darkSheetBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Quran Reciters (القراء)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textWhite
                )
                Text(
                    text = "Select your preferred Qari",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textGray
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reciter_search_bar"),
                placeholder = { Text("Search by reciter name or country...", color = textGray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Reciters",
                        tint = textGray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search", tint = textGray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = textGray,
                    unfocusedBorderColor = darkIconBg,
                    focusedContainerColor = darkItemBg,
                    unfocusedContainerColor = darkItemBg,
                    focusedTextColor = textWhite,
                    unfocusedTextColor = textWhite,
                    cursorColor = textWhite
                )
            )
        }

        // Section Title
        item {
            Text(
                text = "All Reciters (${filteredReciters.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textWhite,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Reciters List
        if (filteredReciters.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reciters match '$searchQuery'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textGray
                    )
                }
            }
        } else {
            itemsIndexed(filteredReciters, key = { _, it -> it.id }) { _, qari ->
                val isSelected = qari.id == activeQari.id
                val originalIndex = QuranData.qariList.indexOfFirst { it.id == qari.id }
                val isLocked = originalIndex >= 10 && !uiState.isPremiumUser

                ReciterItemCard(
                    qari = qari,
                    index = originalIndex,
                    isSelected = isSelected,
                    isLocked = isLocked,
                    onSelect = { 
                        if (isLocked) onNavigateToPremium?.invoke() else onSelectQari(qari) 
                    },
                    onPlayPreview = { 
                        if (isLocked) onNavigateToPremium?.invoke() else onPlayAudioPreview(qari) 
                    },
                    darkItemBg = darkItemBg,
                    darkIconBg = darkIconBg,
                    textWhite = textWhite,
                    textLight = textLight,
                    textGray = textGray
                )
            }
        }
    }
}

@Composable
private fun ReciterItemCard(
    qari: Qari,
    index: Int,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelect: () -> Unit,
    onPlayPreview: () -> Unit,
    darkItemBg: Color,
    darkIconBg: Color,
    textWhite: Color,
    textLight: Color,
    textGray: Color
) {
    val avatarModel: Any = qari.imageResId 
        ?: if (qari.photoUrl.isNotBlank()) qari.photoUrl 
        else "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("reciter_card_${qari.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) darkItemBg else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .then(if (isLocked) Modifier.alpha(0.6f) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Reciter Image - Always showing real image or guaranteed portrait
                AsyncImage(
                    model = avatarModel,
                    contentDescription = qari.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(darkIconBg)
                        .then(if (isSelected) Modifier.border(2.dp, textWhite, CircleShape) else Modifier)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = qari.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) textWhite else textLight
                    )
                    Text(
                        text = "${qari.country} • Murattal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Premium Reciter",
                    tint = com.example.ui.theme.GoldAccentLight,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected Reciter",
                    tint = textWhite,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select Reciter",
                    tint = textGray,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}