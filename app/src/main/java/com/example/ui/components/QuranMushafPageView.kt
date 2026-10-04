package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.data.repository.MushafPageContent
import com.example.data.repository.QuranData
import com.example.ui.theme.AmiriFontFamily
import com.example.util.QuranUtils
import kotlin.math.cos
import kotlin.math.sin

// Classic Signature Mushaf Colors for fallback / accents
val MushafTealPrimary = Color(0xFF006775)
val MushafTealDark = Color(0xFF004D5A)
val MushafTealBorder = Color(0xFF006775)
val MushafPaperWhite = Color(0xFFFFFFFF)
val MushafTextBlack = Color(0xFF151515)

/**
 * Authentic 8-petal scalloped rosette badge as seen on the left & right of the Surah header.
 * Seamlessly adapts to Light and Dark mode.
 */
@Composable
fun ScallopedRosetteBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    textColor: Color? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bg = backgroundColor ?: if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val border = borderColor ?: MaterialTheme.colorScheme.primary
    val txt = textColor ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(38.dp)
            .drawBehind {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val path = Path()
                val numPetals = 8
                val innerR = radius * 0.76f
                val outerR = radius * 0.98f
                for (i in 0 until numPetals * 2) {
                    val angle = (i * Math.PI / numPetals).toFloat() - (Math.PI / 2).toFloat()
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = bg)
                drawPath(path, color = border, style = Stroke(width = 1.5.dp.toPx()))
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = txt,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Center oval / pill plaque displaying the Surah title in Arabic calligraphy
 */
@Composable
fun SurahCenterPlaque(
    surahNameArabic: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    textColor: Color? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bg = backgroundColor ?: if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val border = borderColor ?: MaterialTheme.colorScheme.primary
    val txt = textColor ?: MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .border(1.5.dp, border, RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "سُورَةُ $surahNameArabic",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = txt,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Distinctive Surah Header Bar:
 * Adapts container, rosettes, and plaque to match Light or Dark theme.
 */
@Composable
fun SurahHeaderBar(
    surah: Surah,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val headerContainerBg = if (isDark) {
        Color(0xFF1B382D) // Dark emerald container for dark mode
    } else {
        MushafTealPrimary // Authentic teal/emerald container for light mode
    }

    val headerBorderColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    } else {
        MushafTealDark
    }

    val elementBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val elementBorder = MaterialTheme.colorScheme.primary
    val elementText = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(headerContainerBg)
            .border(1.5.dp, headerBorderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Rosette: Verses count in Arabic digits (e.g. ۱۹ for Al-Alaq)
            ScallopedRosetteBadge(
                text = toArabicDigits(surah.versesCount),
                backgroundColor = elementBg,
                borderColor = elementBorder,
                textColor = elementText
            )

            // Center Plaque: Surah Title (e.g. سُورَةُ العَلَق)
            SurahCenterPlaque(
                surahNameArabic = surah.nameArabic,
                backgroundColor = elementBg,
                borderColor = elementBorder,
                textColor = elementText
            )

            // Right Rosette: Surah Number in Arabic digits (e.g. ٩٦ for Al-Alaq)
            ScallopedRosetteBadge(
                text = toArabicDigits(surah.id),
                backgroundColor = elementBg,
                borderColor = elementBorder,
                textColor = elementText
            )
        }
    }
}

/**
 * Full Quran Page component implementing the exact visual design synchronized to Light and Dark Mode:
 * - Backdrop: Soft warm parchment in Light Mode, deep midnight emerald in Dark Mode
 * - Page Sheet: Crisp white in Light Mode, deep eye-safe charcoal in Dark Mode
 * - Frame: Authentic Teal / Emerald double border in both modes
 * - Typography: High-contrast Uthmani Arabic typography (Black in Light, Off-white in Dark)
 * - Ornamental circular Ayah end markers & Sajdah indicators
 */
@Composable
fun QuranMushafPageView(
    pageContent: MushafPageContent,
    fontSizeSp: Float = 25f,
    onAyahTap: ((Ayah) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Dynamic Backdrop colors based on Light/Dark mode
    val backdropColors = if (isDark) {
        listOf(
            Color(0xFF14241E),
            Color(0xFF0F1A15),
            Color(0xFF0A120F)
        )
    } else {
        listOf(
            Color(0xFFEBE4D8),
            Color(0xFFE2D9CB),
            Color(0xFFD8CEBD)
        )
    }

    val minaretSilhouetteColor = if (isDark) {
        Color(0x33000000)
    } else {
        Color(0x140F523B)
    }

    // Dynamic Page sheet colors
    val pageSheetBg = if (isDark) {
        Color(0xFF15221D)
    } else {
        Color(0xFFFFFFFF)
    }

    val pageBorderColor = MaterialTheme.colorScheme.primary

    val arabicTextColor = if (isDark) {
        Color(0xFFF3F4F6)
    } else {
        Color(0xFF151515)
    }

    val accentMarkerColor = MaterialTheme.colorScheme.primary

    // Outer atmospheric background container
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = backdropColors))
            .drawBehind {
                // Draw subtle decorative minaret & dome silhouettes at the bottom corners
                val w = size.width
                val h = size.height
                val minaretW = 32.dp.toPx()
                val minaretH = 140.dp.toPx()

                // Left minaret silhouette
                val leftPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h - minaretH)
                    lineTo(minaretW * 0.4f, h - minaretH - 15.dp.toPx())
                    lineTo(minaretW * 0.5f, h - minaretH - 30.dp.toPx())
                    lineTo(minaretW * 0.6f, h - minaretH - 15.dp.toPx())
                    lineTo(minaretW, h - minaretH)
                    lineTo(minaretW, h)
                    close()
                }
                drawPath(leftPath, color = minaretSilhouetteColor)

                // Right minaret silhouette
                val rightPath = Path().apply {
                    moveTo(w, h)
                    lineTo(w, h - minaretH)
                    lineTo(w - minaretW * 0.4f, h - minaretH - 15.dp.toPx())
                    lineTo(w - minaretW * 0.5f, h - minaretH - 30.dp.toPx())
                    lineTo(w - minaretW * 0.6f, h - minaretH - 15.dp.toPx())
                    lineTo(w - minaretW, h - minaretH)
                    lineTo(w - minaretW, h)
                    close()
                }
                drawPath(rightPath, color = minaretSilhouetteColor)
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Page Card Container with Double Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, pageBorderColor, RoundedCornerShape(16.dp))
                .padding(4.dp)
                .border(0.75.dp, pageBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(pageSheetBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pageContent.ayahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading Quran Text...",
                            fontFamily = AmiriFontFamily,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Grouping + text shaping are pure functions of the page content,
                    // so they are cached instead of being re-run on every recomposition.
                    val ayahsBySurah = remember(pageContent.ayahs) {
                        pageContent.ayahs.groupBy { it.surahNumber }
                    }

                    ayahsBySurah.forEach { (surahId, ayahs) ->
                        key(surahId) {
                            val surah = remember(surahId, pageContent.primarySurah.id) {
                                QuranData.surahById(surahId) ?: pageContent.primarySurah
                            }

                            // Check if this is the start of the Surah or contains the first verse
                            if (ayahs.any { it.ayahNumber == 1 }) {
                                // Authentic Surah Header Bar matching reference image
                                SurahHeaderBar(surah = surah)

                                // Centered Calligraphic Bismillah (except Surah 1 & Surah 9)
                                if (surah.id != 1 && surah.id != 9) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = (fontSizeSp + 3f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = arabicTextColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    )
                                }
                            }

                            // Build authentic justified Quranic text flow. The styled
                            // text is memoised per (ayahs, font size, accent colour):
                            // re-shaping Uthmani script on every recomposition was one
                            // of the most expensive operations on this screen.
                            val annotatedText = remember(ayahs, fontSizeSp, accentMarkerColor) {
                                buildAnnotatedString {
                                    ayahs.forEach { ayah ->
                                        val rawText = QuranUtils.formatAyahText(ayah.surahNumber, ayah.ayahNumber, ayah.textArabic)
                                
                                        val isSajdahAyah = isSajdahVerse(ayah.surahNumber, ayah.ayahNumber)
                                        if (isSajdahAyah) {
                                            // Highlight Sajdah portion with underline / marker
                                            withStyle(SpanStyle(textDecoration = TextDecoration.None)) {
                                                append(rawText)
                                            }
                                            // Add Sajdah Mihrab symbol
                                            withStyle(
                                                SpanStyle(
                                                    color = accentMarkerColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = (fontSizeSp * 0.95f).sp
                                                )
                                            ) {
                                                append(" ۩ ")
                                            }
                                        } else {
                                            append(rawText)
                                        }

                                        // Ornamental Circular Ayah End Medallion with Arabic Digits inside
                                        withStyle(
                                            SpanStyle(
                                                color = accentMarkerColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (fontSizeSp * 0.85f).sp
                                            )
                                        ) {
                                            append(" ۝${toArabicDigits(ayah.ayahNumber)} ")
                                        }
                                    }
                                }
                            }

                            // High-contrast, authentic justified text rendering
                            Text(
                                text = annotatedText,
                                fontFamily = AmiriFontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 2.05f).sp,
                                textAlign = TextAlign.Justify,
                                color = arabicTextColor,
                                style = TextStyle(textDirection = TextDirection.Rtl),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Page Divider & Page Number
                HorizontalDivider(
                    color = pageBorderColor.copy(alpha = 0.35f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "—  ${toArabicDigits(pageContent.pageNumber)}  —",
                    fontFamily = AmiriFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentMarkerColor,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * Checks if a given Surah and Ayah is one of the 15 authentic Sajdah (prostration) verses.
 */
fun isSajdahVerse(surah: Int, ayah: Int): Boolean {
    return when (surah) {
        7 -> ayah == 206
        13 -> ayah == 15
        16 -> ayah == 50
        17 -> ayah == 109
        19 -> ayah == 58
        22 -> ayah == 18 || ayah == 77
        25 -> ayah == 60
        27 -> ayah == 26
        32 -> ayah == 15
        38 -> ayah == 24
        41 -> ayah == 38
        53 -> ayah == 62
        84 -> ayah == 21
        96 -> ayah == 19
        else -> false
    }
}

/**
 * Converts Western digits (0-9) to Eastern Arabic-Indic numerals (٠-٩)
 */
fun toArabicDigits(number: Int): String {
    val western = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder()
    for (ch in western) {
        if (ch in '0'..'9') {
            builder.append(arabicDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}
