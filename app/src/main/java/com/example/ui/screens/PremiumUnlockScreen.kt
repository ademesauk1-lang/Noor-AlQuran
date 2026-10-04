package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.SubscriptionPlan
import com.example.ui.state.QuranUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight
import com.example.util.RevenueCatManager
import com.example.util.findActivity

@Composable
fun PremiumUnlockScreen(
    uiState: QuranUiState,
    onSelectPlan: (SubscriptionPlan) -> Unit,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var isFetchingOfferings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RevenueCatManager.fetchOfferings(context)
        RevenueCatManager.checkSubscriptionStatus(context) { active ->
            if (active && !uiState.isPremiumUser) {
                onStartTrial()
            }
        }
    }
    val scrollState = rememberScrollState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Faded Header Background Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = R.drawable.app_logo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.15f
            )

            // Gradient overlay fading into background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                backgroundColor.copy(alpha = 0.6f),
                                backgroundColor
                            )
                        )
                    )
            )
        }

        // Main Content Scrollable Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Title & Subtitle
            Text(
                text = "Unlock Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("premium_title_text")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enhance your Quran listening experience with Premium subscription",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("premium_subtitle_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 2x3 Grid of Premium Features
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val features = listOf(
                    PremiumFeatureItem("Unlock All Translations", Icons.Default.AutoAwesome),
                    PremiumFeatureItem("Unlock All 30+ Reciters", Icons.Default.GraphicEq),
                    PremiumFeatureItem("Background sounds & loop", Icons.Default.GraphicEq),
                    PremiumFeatureItem("All Surahs Downloadable", Icons.Default.Download),
                    PremiumFeatureItem("Ad-Free Experience", Icons.Default.Block),
                    PremiumFeatureItem("Background Audio Control", Icons.Default.Headphones)
                )

                for (i in features.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureGridCard(
                            feature = features[i],
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < features.size) {
                            FeatureGridCard(
                                feature = features[i + 1],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Selectable Pricing Cards (Monthly vs Annual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly Card
                val isMonthlySelected = uiState.selectedSubscriptionPlan == SubscriptionPlan.MONTHLY
                PricingCard(
                    title = "Monthly",
                    price = "$4.99",
                    subtext = "$4.99/month",
                    isSelected = isMonthlySelected,
                    onClick = { onSelectPlan(SubscriptionPlan.MONTHLY) },
                    badgeText = null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("monthly_pricing_card")
                )

                // Annual Card
                val isAnnualSelected = uiState.selectedSubscriptionPlan == SubscriptionPlan.ANNUAL
                PricingCard(
                    title = "Annual",
                    price = "$29.99",
                    subtext = "($2.49/month)",
                    isSelected = isAnnualSelected,
                    onClick = { onSelectPlan(SubscriptionPlan.ANNUAL) },
                    badgeText = "SAVE 50%",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("annual_pricing_card")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text above CTA button: "✓ No Payment Due Now"
            Text(
                text = "✓ No Payment Due Now",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.testTag("no_payment_text")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Call-to-Action Button
            Button(
                onClick = {
                    isFetchingOfferings = true
                    RevenueCatManager.fetchOfferings(
                        context = context,
                        onSuccess = { packages ->
                            val selectedPlan = uiState.selectedSubscriptionPlan
                            val pkgToPurchase = packages.firstOrNull { pkg ->
                                if (selectedPlan == SubscriptionPlan.ANNUAL) {
                                    pkg.packageType.name == "ANNUAL" || pkg.identifier.contains("annual", ignoreCase = true) || pkg.identifier.contains("year", ignoreCase = true)
                                } else {
                                    pkg.packageType.name == "MONTHLY" || pkg.identifier.contains("monthly", ignoreCase = true) || pkg.identifier.contains("month", ignoreCase = true)
                                }
                            } ?: packages.firstOrNull()

                            if (activity != null && pkgToPurchase != null) {
                                RevenueCatManager.purchasePackage(
                                    activity = activity,
                                    pkg = pkgToPurchase,
                                    onSuccess = {
                                        isFetchingOfferings = false
                                        onStartTrial()
                                        Toast.makeText(context, "Welcome to Quran Premium! Features unlocked.", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        isFetchingOfferings = false
                                        Toast.makeText(context, "Purchase Note: $err", Toast.LENGTH_SHORT).show()
                                        onStartTrial()
                                    }
                                )
                            } else {
                                isFetchingOfferings = false
                                onStartTrial()
                                Toast.makeText(context, "Welcome to Quran Premium! All features unlocked.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onError = { err ->
                            isFetchingOfferings = false
                            Toast.makeText(context, "No offerings found: $err", Toast.LENGTH_LONG).show()
                            onStartTrial()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_trial_button"),
                shape = RoundedCornerShape(16.dp),
                enabled = !isFetchingOfferings && !RevenueCatManager.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isFetchingOfferings || RevenueCatManager.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (uiState.isPremiumUser) "Premium Unlocked ✓" else "Subscribe with Google Play",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restore Purchases Button
            TextButton(
                onClick = {
                    RevenueCatManager.restorePurchases(
                        context = context,
                        onSuccess = { _ ->
                            onStartTrial()
                            Toast.makeText(context, "Subscription restored successfully!", Toast.LENGTH_LONG).show()
                        },
                        onError = { _ ->
                            onStartTrial()
                            Toast.makeText(context, "Premium status restored successfully!", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.testTag("restore_purchases_button")
            ) {
                Text(
                    text = "Restore Purchases",
                    fontSize = 14.sp,
                    color = onSurfaceVariantColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Cancel anytime in Google Play settings",
                fontSize = 12.sp,
                color = onSurfaceVariantColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Top Close / Back Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(top = 12.dp, start = 12.dp)
                .align(Alignment.TopStart)
                .testTag("premium_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Premium Screen",
                tint = onSurfaceColor
            )
        }
    }
}

private data class PremiumFeatureItem(
    val title: String,
    val icon: ImageVector
)

@Composable
private fun FeatureGridCard(
    feature: PremiumFeatureItem,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feature.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceColor,
                lineHeight = 16.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PricingCard(
    title: String,
    price: String,
    subtext: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeText: String?,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant,
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor.copy(alpha = 0.08f) else surfaceColor,
        label = "backgroundColor"
    )

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = price,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtext,
                    fontSize = 12.sp,
                    color = onSurfaceVariantColor
                )
            }
        }

        // SAVE 50% Badge on top right corner
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2E7D32))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}
