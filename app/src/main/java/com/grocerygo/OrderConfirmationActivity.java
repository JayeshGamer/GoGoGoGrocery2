package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.app.R;
import com.grocerygo.models.Address;

import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {
    private static final String TAG = "OrderConfirmation";

    private TextView tvOrderId, tvDeliveryTime, tvOrderAmount, tvPaymentMethod;
    private TextView tvDeliveryAddress, tvOrderItemsSummary;
    private CardView btnTrackOrder, cvOrderItems;
    private Button btnContinueShopping;

    private String orderId;
    private double totalAmount;
    private String paymentMethod;
    private String deliveryAddress;
    private int itemCount;
    private String userId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_order_confirmation);
            Log.d(TAG, "Layout set successfully");

            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            // Get data from intent
            getOrderDataFromIntent();

            // Initialize views
            initViews();

            // Fetch delivery address (prefer default address from addresses collection)
            fetchDeliveryAddressFromFirebase();

            // Setup UI
            setupOrderDetails();
            setupClickListeners();

            Log.d(TAG, "OrderConfirmationActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading confirmation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Navigate to home as fallback
            navigateToHome();
        }
    }

    private void getOrderDataFromIntent() {
        try {
            Intent intent = getIntent();
            orderId = intent.getStringExtra("order_id");
            totalAmount = intent.getDoubleExtra("total_amount", 0.0);
            paymentMethod = intent.getStringExtra("payment_method");
            deliveryAddress = intent.getStringExtra("delivery_address");
            itemCount = intent.getIntExtra("item_count", 0);
            userId = intent.getStringExtra("user_id");

            Log.d(TAG, "Order data - ID: " + orderId + ", Amount: " + totalAmount +
                    ", Payment: " + paymentMethod + ", Address: " + deliveryAddress +
                    ", Items: " + itemCount + ", UserId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data", e);
            orderId = "Unknown";
            totalAmount = 0.0;
            paymentMethod = "Cash on Delivery";
            deliveryAddress = "Address not available";
            itemCount = 0;
        }
    }

    private void initViews() {
        try {
            tvOrderId = findViewById(R.id.tvOrderId);
            tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
            tvOrderAmount = findViewById(R.id.tvOrderAmount);
            tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
            tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
            tvOrderItemsSummary = findViewById(R.id.tvOrderItemsSummary);
            btnTrackOrder = findViewById(R.id.btnTrackOrder);
            btnContinueShopping = findViewById(R.id.btnContinueShopping);
            cvOrderItems = findViewById(R.id.cvOrderItems);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void fetchDeliveryAddressFromFirebase() {
        // Determine userId to use: prefer explicit intent userId, else current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String effectiveUserId = (userId != null && !userId.isEmpty()) ? userId :
                (currentUser != null ? currentUser.getUid() : null);

        if (effectiveUserId == null || effectiveUserId.isEmpty()) {
            Log.w(TAG, "No user ID available; using address from intent or fallback");
            // Use whatever was passed in intent (deliveryAddress) — will be applied in setupOrderDetails
            return;
        }

        Log.d(TAG, "Attempting to load default address from 'addresses' collection for user: " + effectiveUserId);

        // First try to fetch the default address from the 'addresses' collection (preferred)
        db.collection("addresses")
                .whereEqualTo("userId", effectiveUserId)
                .whereEqualTo("isDefault", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Address mainAddress = queryDocumentSnapshots.getDocuments().get(0).toObject(Address.class);
                        if (mainAddress != null) {
                            String formatted = mainAddress.getFormattedAddress();
                            deliveryAddress = formatted;
                            if (tvDeliveryAddress != null) tvDeliveryAddress.setText(formatted);
                            Log.d(TAG, "Loaded default address from addresses collection: " + formatted);
                            return; // done
                        }
                    }

                    // If no default address in addresses collection, fall back to user document fields
                    Log.d(TAG, "No default address found in addresses collection; falling back to user document");
                    loadAddressFromUserDocument(effectiveUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading default address from addresses collection", e);
                    // On failure, fall back to user document
                    loadAddressFromUserDocument(effectiveUserId);
                });
    }

    private void loadAddressFromUserDocument(String effectiveUserId) {
        // Attempt to read address fields from users/<userId> (legacy/fallback)
        db.collection("users").document(effectiveUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String addr = documentSnapshot.getString("address");
                        String city = documentSnapshot.getString("city");
                        String state = documentSnapshot.getString("state");
                        String pincode = documentSnapshot.getString("pincode");

                        StringBuilder fullAddress = new StringBuilder();
                        if (addr != null && !addr.isEmpty()) {
                            fullAddress.append(addr);
                        }
                        if (city != null && !city.isEmpty()) {
                            if (fullAddress.length() > 0) fullAddress.append(", ");
                            fullAddress.append(city);
                        }
                        if (state != null && !state.isEmpty()) {
                            if (fullAddress.length() > 0) fullAddress.append(", ");
                            fullAddress.append(state);
                        }
                        if (pincode != null && !pincode.isEmpty()) {
                            if (fullAddress.length() > 0) fullAddress.append(" - ");
                            fullAddress.append(pincode);
                        }

                        if (fullAddress.length() > 0) {
                            deliveryAddress = fullAddress.toString();
                            if (tvDeliveryAddress != null) tvDeliveryAddress.setText(deliveryAddress);
                            Log.d(TAG, "Loaded delivery address from user document: " + deliveryAddress);
                        } else {
                            Log.w(TAG, "User document had no address fields; leaving intent address as-is");
                        }
                    } else {
                        Log.w(TAG, "User document does not exist; leaving intent address as-is");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching delivery address from user document", e);
                });
    }

    private void setupOrderDetails() {
        try {
            // Display order ID (show last 6 characters for readability)
            if (tvOrderId != null) {
                String displayOrderId = orderId != null && orderId.length() > 6
                        ? orderId.substring(orderId.length() - 6)
                        : (orderId != null ? orderId : "N/A");
                tvOrderId.setText(String.format("#%s", displayOrderId.toUpperCase()));
                Log.d(TAG, "Order ID set: " + displayOrderId);
            }

            // Display estimated delivery time
            if (tvDeliveryTime != null) {
                tvDeliveryTime.setText("30-45 minutes");
            }

            // Display order amount
            if (tvOrderAmount != null) {
                tvOrderAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
                Log.d(TAG, "Amount set: ₹" + totalAmount);
            }

            // Display payment method
            if (tvPaymentMethod != null) {
                String displayPaymentMethod = paymentMethod != null ? paymentMethod : "Cash on Delivery";
                tvPaymentMethod.setText(displayPaymentMethod);
                Log.d(TAG, "Payment method set: " + displayPaymentMethod);
            }

            // Display delivery address
            if (tvDeliveryAddress != null) {
                // If we have a logged in user (or a userId passed), prefer the address fetched from Firestore.
                // Show a loading placeholder until the async fetch updates the view.
                boolean hasUserContext = (userId != null && !userId.isEmpty()) ||
                        (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null);

                if (hasUserContext) {
                    tvDeliveryAddress.setText("Loading address...");
                    Log.d(TAG, "Waiting to load address from Firestore for user context");
                } else {
                    String address = deliveryAddress != null && !deliveryAddress.isEmpty()
                            ? deliveryAddress
                            : "Address not available";
                    tvDeliveryAddress.setText(address);
                    Log.d(TAG, "Address set (no user context): " + address);
                }
            }

            // Display order items summary with correct count
            if (tvOrderItemsSummary != null && cvOrderItems != null) {
                if (itemCount > 0) {
                    tvOrderItemsSummary.setText(String.format(Locale.getDefault(),
                            "%d %s", itemCount, itemCount == 1 ? "item" : "items"));
                    cvOrderItems.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Item count set: " + itemCount);
                } else {
                    cvOrderItems.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up order details", e);
        }
    }

    private void setupClickListeners() {
        try {
            if (btnTrackOrder != null) {
                btnTrackOrder.setOnClickListener(v -> {
                    Log.d(TAG, "Track Order clicked");
                    try {
                        Intent intent = new Intent(OrderConfirmationActivity.this, OrdersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to Orders", e);
                        Toast.makeText(this, "Orders page not available", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    }
                });
            }

            if (btnContinueShopping != null) {
                btnContinueShopping.setOnClickListener(v -> {
                    Log.d(TAG, "Continue Shopping clicked");
                    navigateToHome();
                });
            }

            // Handle back press with OnBackPressedDispatcher
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Log.d(TAG, "Back pressed - navigating to home");
                    navigateToHome();
                }
            });

            Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void navigateToHome() {
        try {
            Intent intent = new Intent(OrderConfirmationActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to home", e);
        }
    }
}
