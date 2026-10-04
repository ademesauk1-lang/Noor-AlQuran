package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import kotlinx.coroutines.launch

private const val ITEM_ANIM_MS = 320

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Static onboarding copy — declared once for the process instead of being
 * re-allocated on every recomposition of the screen.
 */
private val OnboardingPages = listOf(
    OnboardingPageData(
        title = "Noor Al-Quran",
        subtitle = "Authentic Madani Mushaf",
        description = "Read crystal-clear Uthmani Arabic script with verse-by-verse translations, Tajweed guidance, and bookmarking.",
        icon = Icons.Default.MenuBook
    ),
    OnboardingPageData(
        title = "Melodious Recitations",
        subtitle = "World-Class Qaris",
        description = "Listen to high-quality audio recitations from 30+ legendary Qaris including Alafasy, AbdulBaset, and Abdul Rahman Mossad.",
        icon = Icons.Default.Headphones
    ),
    OnboardingPageData(
        title = "Spiritual Insights",
        subtitle = "AI Tafsir & Translations",
        description = "Deepen your understanding with instant AI-powered verse explanations, context, and multi-language translations.",
        icon = Icons.Default.AutoAwesome
    ),
    OnboardingPageData(
        title = "Your Daily Companion",
        subtitle = "Cloud Sync & Offline Access",
        description = "Download audio for offline listening, set daily reading goals, and save your last read position across devices.",
        icon = Icons.Default.BookmarkBorder
    )
)

/**
 * First-run onboarding.
 *
 * Shown only on the very first launch: completing or skipping it persists the
 * `onboarding_completed` flag in DataStore through
 * [com.example.data.preferences.AppPreferencesRepository], and the launch gate
 * in `QuranViewModel` routes every later start straight to the home screen.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBackground = Color(0xFF0D1812)
    val cardBackground = Color(0xFF13231A)
    val goldAccent = Color(0xFFD4AF37)
    val goldAccentGlow = Color(0xFFF3E5AB)
    val textWhite = Color.White
    val textGray = Color(0xFFA0B2A6)

    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    // Observed through derivedStateOf so scrolling does not recompose the whole
    // screen twice per frame — only the indicators/button labels that change.
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val isLastPage = currentPage == OnboardingPages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Ambient background pattern
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.07f
        )

        // Gradient overlay keeps the copy readable on top of the artwork.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            darkBackground.copy(alpha = 0.75f),
                            darkBackground.copy(alpha = 0.92f),
                            darkBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Header ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Noor Al-Quran",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "نور القرآن",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldAccent,
                        fontFamily = AmiriFontFamily
                    )
                }

                if (!isLastPage) {
                    Text(
                        text = "Skip",
                        color = textGray,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onGetStarted)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("welcome_skip_button")
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }

            // ---------- Pages ----------
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("welcome_pager")
            ) { pageIndex ->
                val page = OnboardingPages[pageIndex]
                val isActive = pageIndex == currentPage

                // Settle-time animation: values only change when the page snaps,
                // not on every scroll frame, so this stays perfectly smooth.
                val pageAlpha by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.45f,
                    animationSpec = tween(ITEM_ANIM_MS),
                    label = "pageAlpha"
                )
                val pageScale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.92f,
                    animationSpec = tween(ITEM_ANIM_MS),
                    label = "pageScale"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = pageAlpha
                            scaleX = pageScale
                            scaleY = pageScale
                        }
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Feature icon medallion
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(cardBackground)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            goldAccent.copy(alpha = 0.28f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = goldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(34.dp))

                    Text(
                        text = page.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textWhite,
                        fontFamily = AmiriFontFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = page.subtitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = goldAccentGlow,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        color = textGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // ---------- Page indicator ----------
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 18.dp)
                    .testTag("welcome_page_indicator")
            ) {
                OnboardingPages.indices.forEach { index ->
                    val isSelected = index == currentPage
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 30f else 9f,
                        animationSpec = tween(ITEM_ANIM_MS),
                        label = "indicatorWidth"
                    )
                    val color by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.3f,
                        animationSpec = tween(ITEM_ANIM_MS),
                        label = "indicatorColor"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(9.dp)
                            .width(width.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (isSelected) goldAccent else textGray.copy(alpha = color)
                            )
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                            .semantics {
                                contentDescription = "Page ${index + 1} of ${OnboardingPages.size}"
                            }
                    )
                }
            }

            // ---------- Primary action ----------
            Button(
                onClick = {
                    if (isLastPage) {
                        onGetStarted()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_primary_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldAccent,
                    contentColor = Color(0xFF10231A)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = if (isLastPage) Icons.Default.Check
                        else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = if (isLastPage) {
                    "You can change these settings anytime."
                } else {
                    "Page ${currentPage + 1} of ${OnboardingPages.size}"
                },
                fontSize = 12.sp,
                color = textGray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }
    }
}
