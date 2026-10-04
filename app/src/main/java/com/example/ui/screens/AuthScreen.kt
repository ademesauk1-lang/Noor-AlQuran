package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.api.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import com.example.data.model.UserProfile
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.Google
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight

private fun parseAuthError(e: Throwable): String {
    val msg = e.message?.lowercase() ?: ""
    return when {
        msg.contains("timeout") || msg.contains("timed out") || e is kotlinx.coroutines.TimeoutCancellationException ->
            "Network timeout. Please check your internet connection and try again."
        msg.contains("unable to resolve host") || msg.contains("connect") || msg.contains("failed to connect") || e is java.net.UnknownHostException || e is java.io.IOException ->
            "Network connection error. Please ensure you have internet access."
        msg.contains("invalid login credentials") || msg.contains("invalid_grant") || msg.contains("bad_credentials") ->
            "Invalid email or password. Please check your details."
        msg.contains("user already registered") || msg.contains("email_exists") ->
            "An account with this email already exists. Please sign in."
        msg.contains("password") && msg.contains("short") ->
            "Password must be at least 6 characters."
        else ->
            e.localizedMessage?.takeIf { it.isNotBlank() && !it.contains("io.github.jan") } ?: "Authentication service unavailable. Please try again."
    }
}

@Composable
fun AuthScreen(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignIn by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateHome,
                    modifier = Modifier.testTag("auth_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onSurfaceColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نور القرآن",
                    fontFamily = AmiriFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header Icon
            Surface(
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignIn) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Text(
                text = if (isSignIn) "Sign in to sync your reading bookmarks & progress" else "Join to save your Quran journey across devices",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Tab Switcher (Sign In vs Register)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_tab_selector")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sign In Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSignIn) surfaceColor else Color.Transparent,
                        shadowElevation = if (isSignIn) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { isSignIn = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignIn) primaryColor else onSurfaceVariantColor
                            )
                        }
                    }

                    // Sign Up Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isSignIn) surfaceColor else Color.Transparent,
                        shadowElevation = if (!isSignIn) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { isSignIn = false }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (!isSignIn) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSignIn) primaryColor else onSurfaceVariantColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isSignIn) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Email Submit Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isSignIn && name.isBlank()) {
                                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    if (isSignIn) {
                                        SupabaseClient.client.auth.signInWith(Email) {
                                            this.email = email.trim()
                                            this.password = password
                                        }
                                    } else {
                                        SupabaseClient.client.auth.signUpWith(Email) {
                                            this.email = email.trim()
                                            this.password = password
                                        }
                                        val userId = SupabaseClient.client.auth.currentSessionOrNull()?.user?.id
                                        if (userId != null && name.isNotBlank()) {
                                            try {
                                                SupabaseClient.client.postgrest["profiles"].insert(UserProfile(id = userId, full_name = name.trim()))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    Toast.makeText(context, if (isSignIn) "Welcome back!" else "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    onNavigateHome()
                                } catch (e: Throwable) {
                                    val errStr = parseAuthError(e)
                                    Toast.makeText(context, errStr, Toast.LENGTH_LONG).show()
                                    snackbarHostState.showSnackbar(errStr)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isSignIn) "Sign In with Email" else "Create Free Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clean "OR" Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Single, Official Google Sign-In Button
            OutlinedButton(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val webClientId = context.getString(R.string.default_web_client_id)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(webClientId)
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val credentialManager = CredentialManager.create(context)
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )

                            val credential = result.credential
                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = googleIdTokenCredential.idToken

                                SupabaseClient.client.auth.signInWith(IDToken) {
                                    this.idToken = idToken
                                    this.provider = Google
                                }
                                Toast.makeText(context, "Signed in with Google!", Toast.LENGTH_SHORT).show()
                                onNavigateHome()
                            } else {
                                Toast.makeText(context, "Google Sign-In failed to return credentials. Please try email login.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Throwable) {
                            if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                val errStr = parseAuthError(e)
                                Toast.makeText(context, errStr, Toast.LENGTH_SHORT).show()
                                snackbarHostState.showSnackbar(errStr)
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = surfaceColor
                ),
                enabled = !isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Footer Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignIn) "Don't have an account? " else "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariantColor
                )
                Text(
                    text = if (isSignIn) "Sign Up" else "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isSignIn = !isSignIn }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
