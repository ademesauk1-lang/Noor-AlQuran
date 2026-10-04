package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction

object RevenueCatManager {

    var packages by mutableStateOf<List<Package>>(emptyList())
        private set

    var selectedPackage by mutableStateOf<Package?>(null)

    var isLoading by mutableStateOf(false)
        private set

    var isPremiumActive by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    fun ensureConfigured(context: Context? = null): Boolean {
        if (!Purchases.isConfigured && context != null) {
            val apiKey = "" // Replace with actual RevenueCat API Key when configured
            if (apiKey.isNotBlank() && apiKey.startsWith("goog_") && !apiKey.contains("your_revenuecat_api_key")) {
                try {
                    Purchases.configure(
                        com.revenuecat.purchases.PurchasesConfiguration.Builder(
                            context = context.applicationContext,
                            apiKey = apiKey
                        ).build()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return Purchases.isConfigured
    }

    /**
     * Fetch available offerings/packages from RevenueCat Dashboard.
     */
    fun fetchOfferings(
        context: Context? = null,
        onSuccess: ((List<Package>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!ensureConfigured(context)) {
            isLoading = false
            val err = "RevenueCat is not configured."
            errorMessage = err
            onError?.invoke(err)
            return
        }
        isLoading = true
        errorMessage = null

        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    isLoading = false
                    val currentOffering = offerings.current
                    if (currentOffering != null && currentOffering.availablePackages.isNotEmpty()) {
                        packages = currentOffering.availablePackages
                        if (selectedPackage == null && packages.isNotEmpty()) {
                            selectedPackage = packages.firstOrNull { it.packageType.name == "ANNUAL" }
                                ?: packages.first()
                        }
                        onSuccess?.invoke(packages)
                    } else {
                        val err = "No active RevenueCat offerings configured or found."
                        errorMessage = err
                        onError?.invoke(err)
                    }
                }

                override fun onError(error: PurchasesError) {
                    isLoading = false
                    errorMessage = error.message
                    onError?.invoke(error.message)
                }
            })
        } catch (e: Exception) {
            isLoading = false
            val err = e.message ?: "Failed to fetch offerings"
            errorMessage = err
            onError?.invoke(err)
        }
    }

    /**
     * Check current customer entitlements status.
     */
    fun checkSubscriptionStatus(context: Context? = null, onResult: ((Boolean) -> Unit)? = null) {
        if (!ensureConfigured(context)) {
            onResult?.invoke(false)
            return
        }
        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val hasPremium = customerInfo.entitlements["premium"]?.isActive == true
                            || customerInfo.entitlements.active.isNotEmpty()
                    isPremiumActive = hasPremium
                    onResult?.invoke(hasPremium)
                }

                override fun onError(error: PurchasesError) {
                    errorMessage = error.message
                    onResult?.invoke(false)
                }
            })
        } catch (e: Exception) {
            onResult?.invoke(false)
        }
    }

    /**
     * Trigger purchase flow for selected package using Google Play Billing via RevenueCat.
     */
    fun purchasePackage(
        activity: Activity,
        pkg: Package,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!ensureConfigured(activity)) {
            onError("RevenueCat is not configured.")
            return
        }
        isLoading = true
        errorMessage = null

        try {
            val purchaseParams = PurchaseParams.Builder(activity, pkg).build()

            Purchases.sharedInstance.purchase(
                purchaseParams = purchaseParams,
                callback = object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        isLoading = false
                        val hasPremium = customerInfo.entitlements["premium"]?.isActive == true
                                || customerInfo.entitlements.active.isNotEmpty()
                        isPremiumActive = hasPremium
                        onSuccess(customerInfo)
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        isLoading = false
                        if (!userCancelled) {
                            errorMessage = error.message
                            onError(error.message)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isLoading = false
            onError(e.message ?: "Purchase error")
        }
    }

    /**
     * Restore previous Google Play / App Store purchases.
     */
    fun restorePurchases(
        context: Context? = null,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!ensureConfigured(context)) {
            onError("RevenueCat is not configured.")
            return
        }
        isLoading = true
        errorMessage = null

        try {
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    isLoading = false
                    val hasPremium = customerInfo.entitlements["premium"]?.isActive == true
                            || customerInfo.entitlements.active.isNotEmpty()
                    isPremiumActive = hasPremium
                    onSuccess(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    isLoading = false
                    errorMessage = error.message
                    onError(error.message)
                }
            })
        } catch (e: Exception) {
            isLoading = false
            onError(e.message ?: "Restore error")
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
