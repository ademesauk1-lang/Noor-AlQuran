# ===========================================================================
# Noor Al-Quran — R8 / ProGuard rules for release (minified + shrunk) builds.
#
# R8 full mode is enabled by default in AGP 9 (proguard-android-optimize.txt).
# The rules below keep the reflection-based entry points used by the libraries
# in this project: kotlinx.serialization (Supabase models), Moshi + Retrofit
# (Quran API / Gemini API), Room, Media3, RevenueCat and Firebase.
# ===========================================================================

# Preserve stack-trace quality so Play Console crash reports stay actionable.
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Kotlin
# ---------------------------------------------------------------------------
-dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.coroutines.Continuation { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ---------------------------------------------------------------------------
# kotlinx.serialization (Supabase payloads, Gemini payloads)
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keep,allowobfuscation class <1>$$serializer { *; }
-keepclassmembers class **$$serializer {
    *** INSTANCE;
}
-keep,includedescriptorclasses class com.example.data.model.** { *; }
-keep,includedescriptorclasses class com.example.data.repository.**$$serializer { *; }
-keepclassmembers class com.example.data.repository.** {
    *** Companion;
}

# ---------------------------------------------------------------------------
# Moshi (reflective adapters via KotlinJsonAdapterFactory)
# ---------------------------------------------------------------------------
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    synthetic <methods>;
    *** Companion;
}
-if @com.squareup.moshi.JsonClass class **$*
-keep class <1>_<2>
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep,includedescriptorclasses class com.example.data.**JsonAdapter { *; }
-keep class com.example.data.GenerateContentRequest { *; }
-keep class com.example.data.Content { *; }
-keep class com.example.data.Part { *; }
-keep class com.example.data.GenerationConfig { *; }
-keep class com.example.data.ThinkingConfig { *; }
-keep class com.example.data.GenerateContentResponse { *; }
-keep class com.example.data.Candidate { *; }
-keep class com.example.data.api.** { *; }

# ---------------------------------------------------------------------------
# Retrofit / OkHttp / Okio
# ---------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keepclasseswithmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**
-keep class com.example.data.local.** { *; }

# ---------------------------------------------------------------------------
# Media3 / ExoPlayer (background playback service + media session)
# ---------------------------------------------------------------------------
-dontwarn androidx.media3.**
-keep class com.example.audio.** { *; }
-keep class * extends androidx.media3.session.MediaSessionService { *; }
-keep class * extends androidx.media3.session.MediaSession { *; }
-keep class * implements androidx.media3.exoplayer.source.MediaSource { *; }
-keepclassmembers class androidx.media3.session.MediaSessionService {
    <init>(...);
}

# ---------------------------------------------------------------------------
# AndroidX / Jetpack Compose / DataStore / WorkManager
# ---------------------------------------------------------------------------
-dontwarn androidx.**
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends android.app.Activity {
    <init>(...);
}

# ---------------------------------------------------------------------------
# Firebase (AI / AppCheck)
# ---------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ---------------------------------------------------------------------------
# Supabase / Ktor
# ---------------------------------------------------------------------------
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**
-dontwarn io.lettuce.**
-dontwarn org.jetbrains.annotations.**
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.client.** { *; }

# ---------------------------------------------------------------------------
# RevenueCat (Purchases)
# ---------------------------------------------------------------------------
-dontwarn com.revenuecat.purchases.**
-dontwarn com.android.billingclient.**
-keep class com.revenuecat.purchases.** { *; }
-keep class com.revenuecat.purchases.ui.** { *; }

# ---------------------------------------------------------------------------
# App domain models referenced reflectively by name
# ---------------------------------------------------------------------------
-keep class com.example.data.model.** { *; }
-keep class com.example.BuildConfig { *; }
