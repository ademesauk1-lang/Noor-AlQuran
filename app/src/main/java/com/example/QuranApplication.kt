package com.example

import android.app.Application
import android.util.Log
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class QuranApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Verbose RevenueCat logging only in development builds.
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARN

        // Configure RevenueCat only when a real public Google API key is supplied.
        // Replace this constant (or inject it from BuildConfig/secrets) before
        // shipping store builds with subscriptions enabled.
        val apiKey = ""
        if (apiKey.isNotBlank() && apiKey.startsWith("goog_") && !apiKey.contains("your_revenuecat_api_key")) {
            try {
                Purchases.configure(
                    PurchasesConfiguration.Builder(
                        context = this,
                        apiKey = apiKey
                    ).build()
                )
            } catch (e: Exception) {
                Log.e(TAG, "RevenueCat configuration failed: ${e.message}")
            }
        } else if (BuildConfig.DEBUG) {
            Log.i(TAG, "RevenueCat not configured (no public SDK key supplied).")
        }
    }

    private companion object {
        const val TAG = "QuranApplication"
    }
}
