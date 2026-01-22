package com.simats.digitallibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final String TAG = "SubscriptionActivity";

    private BillingClient billingClient;
    private TextView btnUpgradePremium;
    private TextView btnSkipForNow;

    // 🔑 One-Time Premium Product ID - Change this to match your Play Console
    // product ID
    private static final String PREMIUM_PRODUCT_SKU = "digitallibrary_premium_subscription";

    // SharedPreferences key for premium status
    private static final String PREF_NAME = "DigitalLibraryPrefs";
    private static final String PREF_IS_PREMIUM = "is_premium_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initializeViews();
        setupBillingClient();
        setupClickListeners();
    }

    private void initializeViews() {
        btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        btnSkipForNow = findViewById(R.id.btnSkipForNow);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client Ready");
                    // Check for existing purchases
                    checkExistingPurchases();
                } else {
                    Log.e(TAG, "Billing Setup Failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Service Disconnected");
                Toast.makeText(SubscriptionActivity.this,
                        "Billing service disconnected. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkExistingPurchases() {
        if (billingClient == null || !billingClient.isReady()) {
            return;
        }

        // Check for one-time purchases (INAPP)
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (Purchase purchase : purchases) {
                    if (purchase.getProducts().contains(PREMIUM_PRODUCT_SKU)) {
                        // User already has premium
                        setPremiumUser(true);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "You already have Premium access!",
                                    Toast.LENGTH_SHORT).show();
                            navigateToNextScreen();
                        });
                        return;
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        // Upgrade to Premium - Launch purchase flow
        btnUpgradePremium.setOnClickListener(v -> {
            if (billingClient != null && billingClient.isReady()) {
                launchPurchase();
            } else {
                Toast.makeText(this, "Billing is not ready. Please wait...",
                        Toast.LENGTH_SHORT).show();
                setupBillingClient();
            }
        });

        // Skip for now - Go to Reader/Admin selection page
        btnSkipForNow.setOnClickListener(v -> navigateToNextScreen());
    }

    private void launchPurchase() {
        // Create product list to query - ONE-TIME PURCHASE (INAPP)
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_PRODUCT_SKU)
                        .setProductType(BillingClient.ProductType.INAPP) // One-time purchase
                        .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        // Query product details
        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && productDetailsList != null
                    && !productDetailsList.isEmpty()) {

                ProductDetails productDetails = productDetailsList.get(0);

                // Build billing flow params for one-time purchase
                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                productDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build());

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                // Launch billing flow on UI thread
                runOnUiThread(() -> {
                    BillingResult launchResult = billingClient.launchBillingFlow(
                            SubscriptionActivity.this, billingFlowParams);

                    if (launchResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Failed to launch billing flow: " +
                                launchResult.getDebugMessage());
                        Toast.makeText(this, "Failed to start purchase. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Log.e(TAG, "Product not found. Response: " + billingResult.getDebugMessage());
                    Toast.makeText(this,
                            "Premium not available. Please try again later.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult,
            @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled the purchase");
            Toast.makeText(this, "Purchase cancelled", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
            Toast.makeText(this, "Purchase failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Check if it's our premium product
            if (purchase.getProducts().contains(PREMIUM_PRODUCT_SKU)) {
                Log.d(TAG, "Premium purchase successful!");

                // Save premium status
                setPremiumUser(true);

                // Acknowledge the purchase if not already acknowledged
                if (!purchase.isAcknowledged()) {
                    acknowledgePurchase(purchase);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "🎉 Premium Activated! Enjoy unlimited access forever!",
                            Toast.LENGTH_LONG).show();
                    navigateToNextScreen();
                });
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this, "Purchase pending. Please complete payment.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        com.android.billingclient.api.AcknowledgePurchaseParams acknowledgeParams = com.android.billingclient.api.AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgeParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully");
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
            }
        });
    }

    private void setPremiumUser(boolean isPremium) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_IS_PREMIUM, isPremium).apply();
    }

    /**
     * Static method to check premium status from anywhere in the app
     */
    public static boolean isPremiumUser(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_IS_PREMIUM, false);
    }

    private void navigateToNextScreen() {
        startActivity(new Intent(this, SelectAccountTypeActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToNextScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}
