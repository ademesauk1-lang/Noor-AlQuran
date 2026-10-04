import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

// ---------------------------------------------------------------------------
// Release signing setup
// ---------------------------------------------------------------------------
// Credentials are NEVER stored in the repository. They are read from the
// environment (CI secrets) or from a local, git-ignored `keystore.properties`:
//
//   KEYSTORE_PATH=/absolute/path/to/upload-keystore.jks
//   STORE_PASSWORD=********
//   KEY_ALIAS=upload
//   KEY_PASSWORD=********
//
// When no keystore is configured we fall back to the debug key so that
// `assembleRelease` (R8 / minified) still works on a fresh clone and in CI.
val localKeystoreProperties = java.util.Properties().apply {
  val file = rootProject.file("keystore.properties")
  if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(envName: String, propertyName: String): String? =
  System.getenv(envName)?.takeIf { it.isNotBlank() }
    ?: localKeystoreProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }

val releaseKeystoreFile: java.io.File? = run {
  val rawPath = signingValue("KEYSTORE_PATH", "KEYSTORE_PATH")
    ?: "${rootDir}/my-upload-key.jks"
  val candidate = file(rawPath)
  candidate.takeIf { it.exists() }
}
val releaseStorePassword = signingValue("STORE_PASSWORD", "STORE_PASSWORD")
val releaseKeyAlias = signingValue("KEY_ALIAS", "KEY_ALIAS") ?: "upload"
val releaseKeyPassword = signingValue("KEY_PASSWORD", "KEY_PASSWORD")
val hasReleaseSigning = releaseKeystoreFile != null &&
  releaseStorePassword != null &&
  releaseKeyPassword != null

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.quranapp.qkrnzk"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Ship only the Uthmani Arabic script + language splits we actually support.
    resourceConfigurations += listOf("en", "ar")

    vectorDrawables { useSupportLibrary = true }
  }

  signingConfigs {
    create("release") {
      if (releaseKeystoreFile != null) {
        storeFile = releaseKeystoreFile
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    release {
      // R8 full-mode minification + resource shrinking for store builds.
      isMinifyEnabled = true
      isShrinkResources = true
      isDebuggable = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Falls back to AGP's default debug signing config so `assembleRelease`
      // (R8 + shrinking) still runs on a fresh clone / CI. Store builds always
      // inject the real keystore through the environment.
      signingConfig = if (hasReleaseSigning) {
        signingConfigs.getByName("release")
      } else {
        logger.warn(
          "NoorAlQuran: no release keystore configured (KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD). " +
            "The release build is signed with the debug key — do not publish it."
        )
        signingConfigs.getByName("debug")
      }
    }
    debug {
      isMinifyEnabled = false
      applicationIdSuffix = ".debug"
      // Uses the standard debug keystore (~/.android/debug.keystore) which AGP
      // creates automatically — no keystore file needs to be committed.
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }

  packaging {
    resources {
      excludes += setOf(
        "/META-INF/{AL2.0,LGPL2.1}",
        "/META-INF/DEPENDENCIES",
        "/META-INF/LICENSE*",
        "/META-INF/NOTICE*",
        "/META-INF/INDEX.LIST",
        "/META-INF/*.kotlin_module",
        "/META-INF/versions/9/OSGI-INF/MANIFEST.MF",
        "DebugProbesKt.bin",
        "kotlin-tooling-metadata.json"
      )
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))

  implementation(platform("io.github.jan-tennert.supabase:bom:3.1.2"))
  implementation("io.github.jan-tennert.supabase:postgrest-kt")
  implementation("io.github.jan-tennert.supabase:auth-kt")
  implementation("io.github.jan-tennert.supabase:compose-auth")
  implementation("io.github.jan-tennert.supabase:compose-auth-ui")
  implementation("io.ktor:ktor-client-android:3.0.3")
  implementation("io.ktor:ktor-client-core:3.0.3")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
  implementation("androidx.work:work-runtime-ktx:2.10.0")
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Media3 (ExoPlayer) — streaming engine + media session / foreground playback service
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.session)
  implementation(libs.androidx.media3.datasource)
  implementation(libs.androidx.media3.database)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.purchases)
  implementation(libs.purchases.ui)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
