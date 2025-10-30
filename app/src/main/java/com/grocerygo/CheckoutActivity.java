package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.adapters.CheckoutCartAdapter;
import com.grocerygo.app.R;
import com.grocerygo.firebase.OrderRepository;
import com.grocerygo.models.CartItem;
import com.grocerygo.models.Address;
import com.grocerygo.models.Order;
import com.grocerygo.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private static final String TAG = "CheckoutActivity";

    // New layout views
    private Toolbar toolbar;
    private TextView tvAddressDetails, tvPhoneNumber, tvAddressName;
    private MaterialButton btnChangeAddress;
    private RecyclerView recyclerViewOrderItems;
    private TextView tvItemCount, tvItemTotal, tvDeliveryFee, tvTaxes, tvDiscount, tvTotalAmount, tvBottomTotal;
    private RadioButton radioCashOnDelivery, radioCard, radioUPI;
    private TextInputEditText etInstructions;
    private MaterialButton btnPlaceOrder, btnViewMore;
    private FrameLayout loadingOverlay;
    private LinearLayout layoutDiscount;

    // View More state
    private boolean isItemsExpanded = false;
    private static final int COLLAPSED_ITEM_COUNT = 3;

    private FirebaseFirestore db;
    private OrderRepository orderRepository;
    private FirebaseAuth firebaseAuth;

    private CheckoutCartAdapter checkoutCartAdapter;

    // Order data
    private String productId, productName, productImage;
    private int quantity;
    private double productPrice, subtotal, deliveryFee, tax, totalAmount, couponDiscount;
    private String selectedPaymentMethod = "Cash on Delivery";
    private String deliveryInstructions = "";
    private boolean isCartCheckout = false;
    private List<CartItem> cartItems;

    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_checkout);

            // Initialize Firebase
            firebaseAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            orderRepository = new OrderRepository();

            Log.d(TAG, "Firebase initialized");

            // Initialize CartManager
            cartManager = CartManager.getInstance(this);

            // Get data from intent
            getOrderDataFromIntent();

            // Initialize views
            initViews();

            // Load user's default address (if signed in)
            fetchAndDisplayDefaultAddress();

            // Setup UI
            setupToolbar();
            setupOrderSummary();
            setupPaymentMethods();
            setupClickListeners();

            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading checkout page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for cart updates so the checkout totals and list stay in sync
        if (cartManager != null) cartManager.addCartUpdateListener(this);
        // If this is a cart checkout, refresh the cart items to ensure latest state
        if (isCartCheckout && cartManager != null) {
            cartItems = cartManager.getCartItems();
            if (checkoutCartAdapter != null) {
                checkoutCartAdapter = new CheckoutCartAdapter(this, cartItems);
                recyclerViewOrderItems.setAdapter(checkoutCartAdapter);
            }
            // Recompute totals
            recomputeTotalsFromCart();
            updatePriceDisplay();
        }

        // Refresh address in case user changed it in AddressBookActivity
        fetchAndDisplayDefaultAddress();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cartManager != null) cartManager.removeCartUpdateListener(this);
    }

    @Override
    public void onCartUpdated(int itemCount) {
        // Refresh checkout UI when underlying cart changes
        runOnUiThread(() -> {
            try {
                if (isCartCheckout && cartManager != null) {
                    cartItems = cartManager.getCartItems();
                    if (checkoutCartAdapter == null) {
                        checkoutCartAdapter = new CheckoutCartAdapter(this, cartItems);
                        recyclerViewOrderItems.setAdapter(checkoutCartAdapter);
                    } else {
                        // Recreate adapter with new list to ensure correct data binding
                        checkoutCartAdapter = new CheckoutCartAdapter(this, cartItems);
                        recyclerViewOrderItems.setAdapter(checkoutCartAdapter);
                    }

                    // Update item count and totals
                    tvItemCount.setText(String.format(Locale.getDefault(), "%d items", cartItems.size()));
                    recomputeTotalsFromCart();
                    updatePriceDisplay();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating checkout UI on cart change", e);
            }
        });
    }

    private void recomputeTotalsFromCart() {
        subtotal = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                subtotal += item.getProductPrice() * item.getQuantity();
            }
        }
        tax = subtotal * 0.05;
        totalAmount = subtotal + deliveryFee + tax - couponDiscount;
    }

    private void getOrderDataFromIntent() {
        try {
            Intent intent = getIntent();
            isCartCheckout = intent.getBooleanExtra("is_cart_checkout", false);

            if (isCartCheckout) {
                // Cart checkout - get cart totals
                subtotal = intent.getDoubleExtra("cart_subtotal", 0.0);
                deliveryFee = intent.getDoubleExtra("cart_delivery_fee", 40.0);
                int itemCount = intent.getIntExtra("cart_item_count", 0);

                // Get cart items from CartManager
                cartItems = CartManager.getInstance(this).getCartItems();

                // Calculate tax and total
                couponDiscount = 0.0;
                tax = subtotal * 0.05;
                totalAmount = subtotal + deliveryFee + tax - couponDiscount;

                Log.d(TAG, "Cart Checkout - Subtotal: " + subtotal + ", Delivery: " + deliveryFee +
                        ", Tax: " + tax + ", Total: " + totalAmount + ", Items: " + itemCount);
            } else {
                // Single product checkout
                productId = intent.getStringExtra("product_id");
                productName = intent.getStringExtra("product_name");
                productPrice = intent.getDoubleExtra("product_price", 0.0);
                productImage = intent.getStringExtra("product_image");
                quantity = intent.getIntExtra("quantity", 1);

                Log.d(TAG, "Product Data - ID: " + productId + ", Name: " + productName +
                      ", Price: " + productPrice + ", Qty: " + quantity);

                // Calculate prices
                calculatePrices();
            }

            Log.d(TAG, "Final totals - Subtotal: " + subtotal + ", Total: " + totalAmount);
        } catch (Exception e) {
            Log.e(TAG, "Error getting order data from intent", e);
            throw e;
        }
    }

    private void calculatePrices() {
        subtotal = productPrice * Math.min(quantity, CartManager.MAX_QUANTITY);
        deliveryFee = subtotal > 500 ? 0 : 40.0;
        couponDiscount = 0.0;
        tax = subtotal * 0.05;
        totalAmount = subtotal + deliveryFee - couponDiscount + tax;
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            tvAddressDetails = findViewById(R.id.tvAddressDetails);
            tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
            tvAddressName = findViewById(R.id.tvAddressName);
            btnChangeAddress = findViewById(R.id.btnChangeAddress);
            recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
            tvItemCount = findViewById(R.id.tvItemCount);
            tvItemTotal = findViewById(R.id.tvItemTotal);
            tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
            tvTaxes = findViewById(R.id.tvTaxes);
            tvDiscount = findViewById(R.id.tvDiscount);
            layoutDiscount = findViewById(R.id.layoutDiscount);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);
            tvBottomTotal = findViewById(R.id.tvBottomTotal);
            radioCashOnDelivery = findViewById(R.id.radioCashOnDelivery);
            radioCard = findViewById(R.id.radioCard);
            radioUPI = findViewById(R.id.radioUPI);
            etInstructions = findViewById(R.id.etInstructions);
            btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
            btnViewMore = findViewById(R.id.btnViewMore);
            loadingOverlay = findViewById(R.id.loadingOverlay);

            // Setup RecyclerView for cart items
            recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewOrderItems.setHasFixedSize(true);

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            Log.d(TAG, "Toolbar setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }

    private void setupOrderSummary() {
        try {
            if (isCartCheckout && cartItems != null && !cartItems.isEmpty()) {
                // For cart checkout, use the adapter to display all cart items
                checkoutCartAdapter = new CheckoutCartAdapter(this, cartItems);
                recyclerViewOrderItems.setAdapter(checkoutCartAdapter);
                recyclerViewOrderItems.setVisibility(View.VISIBLE);

                // Update item count
                tvItemCount.setText(String.format(Locale.getDefault(), "%d items", cartItems.size()));

                // Show/hide "View More" button based on item count
                if (checkoutCartAdapter.shouldShowExpandButton()) {
                    btnViewMore.setVisibility(View.VISIBLE);
                } else {
                    btnViewMore.setVisibility(View.GONE);
                }

                // Update subtotal from cart items
                subtotal = 0;
                for (CartItem item : cartItems) {
                    subtotal += item.getProductPrice() * item.getQuantity();
                }

                // Recalculate totals
                tax = subtotal * 0.05;
                totalAmount = subtotal + deliveryFee + tax - couponDiscount;
            } else {
                // Single product checkout - create a single-item list
                if (productName != null) {
                    int cappedQty = Math.min(quantity, CartManager.MAX_QUANTITY);
                    CartItem singleItem = new CartItem(productId, productName, productImage, productPrice, "gm", cappedQty);
                    cartItems = new ArrayList<>();
                    cartItems.add(singleItem);

                    checkoutCartAdapter = new CheckoutCartAdapter(this, cartItems);
                    recyclerViewOrderItems.setAdapter(checkoutCartAdapter);
                    recyclerViewOrderItems.setVisibility(View.VISIBLE);

                    tvItemCount.setText(String.format(Locale.getDefault(), "1 item"));

                    // Hide "View More" button for single item
                    btnViewMore.setVisibility(View.GONE);
                }
            }

            // Update price display
            updatePriceDisplay();

            Log.d(TAG, "Order summary setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up order summary", e);
        }
    }

    private void updatePriceDisplay() {
        tvItemTotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));

        if (deliveryFee == 0) {
            tvDeliveryFee.setText("FREE");
            tvDeliveryFee.setTextColor(getResources().getColor(R.color.success_green, null));
        } else {
            tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryFee));
            tvDeliveryFee.setTextColor(getResources().getColor(R.color.text_primary, null));
        }

        tvTaxes.setText(String.format(Locale.getDefault(), "₹%.2f", tax));

        if (couponDiscount > 0) {
            tvDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", couponDiscount));
            layoutDiscount.setVisibility(View.VISIBLE);
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
        tvBottomTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
    }

    private void setupPaymentMethods() {
        try {
            // Set default payment method
            radioCashOnDelivery.setChecked(true);
            selectedPaymentMethod = "Cash on Delivery";

            // Add click listeners to each radio button for manual selection control
            radioCashOnDelivery.setOnClickListener(v -> {
                selectPaymentMethod(R.id.radioCashOnDelivery, "Cash on Delivery");
            });

            radioCard.setOnClickListener(v -> {
                selectPaymentMethod(R.id.radioCard, "Credit/Debit Card");
            });

            radioUPI.setOnClickListener(v -> {
                selectPaymentMethod(R.id.radioUPI, "UPI Payment");
            });

            Log.d(TAG, "Payment methods setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up payment methods", e);
        }
    }

    private void selectPaymentMethod(int selectedId, String paymentMethod) {
        // Uncheck all radio buttons first
        radioCashOnDelivery.setChecked(false);
        radioCard.setChecked(false);
        radioUPI.setChecked(false);

        // Check only the selected one
        if (selectedId == R.id.radioCashOnDelivery) {
            radioCashOnDelivery.setChecked(true);
        } else if (selectedId == R.id.radioCard) {
            radioCard.setChecked(true);
        } else if (selectedId == R.id.radioUPI) {
            radioUPI.setChecked(true);
        }

        // Update the selected payment method
        selectedPaymentMethod = paymentMethod;
        Log.d(TAG, "Payment method selected: " + paymentMethod);
    }

    private void setupClickListeners() {
        try {
            btnChangeAddress.setOnClickListener(v -> {
                // Open the address book so the user can change/select address
                try {
                    Intent intent = new Intent(CheckoutActivity.this, AddressBookActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening AddressBookActivity", e);
                    Toast.makeText(this, "Unable to open address book", Toast.LENGTH_SHORT).show();
                }
            });

            btnPlaceOrder.setOnClickListener(v -> {
                Log.d(TAG, "Place Order button clicked");

                // Get delivery instructions
                if (etInstructions.getText() != null) {
                    deliveryInstructions = etInstructions.getText().toString().trim();
                }

                validateAndProceedToPayment();
            });

            // Use the adapter's toggle functionality
            if (btnViewMore != null) {
                btnViewMore.setOnClickListener(v -> {
                    if (checkoutCartAdapter != null) {
                        checkoutCartAdapter.toggleExpand();
                        updateViewMoreButtonText();
                    }
                });
            }

            Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void updateViewMoreButtonText() {
        if (btnViewMore == null) return;

        if (checkoutCartAdapter != null && checkoutCartAdapter.isExpanded()) {
            btnViewMore.setText("View less");
            try {
                btnViewMore.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_expand_less));
            } catch (Exception e) {
                Log.e(TAG, "Error setting expand less icon", e);
            }
        } else {
            btnViewMore.setText("View all items");
            try {
                btnViewMore.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_expand_more));
            } catch (Exception e) {
                Log.e(TAG, "Error setting expand more icon", e);
            }
        }
    }

    private void validateAndProceedToPayment() {
        // Show confirmation dialog
        showPaymentConfirmationDialog();
    }

    private void showPaymentConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Order");

        String message = String.format(Locale.getDefault(),
                "Total Amount: ₹%.2f\nPayment Method: %s",
                totalAmount, selectedPaymentMethod);

        if (!deliveryInstructions.isEmpty()) {
            message += "\nDelivery Instructions: " + deliveryInstructions;
        }

        builder.setMessage(message + "\n\nDo you want to place this order?");

        builder.setPositiveButton("Place Order", (dialog, which) -> {
            dialog.dismiss();
            placeOrder();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Log.d(TAG, "Order cancelled by user");
        });

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void placeOrder() {
        try {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            showLoading(true);

            // Create order items
            List<Order.OrderItem> items = new ArrayList<>();

            if (isCartCheckout) {
                // Cart checkout - convert cart items to order items
                for (CartItem cartItem : cartItems) {
                    Order.OrderItem orderItem = new Order.OrderItem(
                            cartItem.getProductId(),
                            cartItem.getProductName(),
                            cartItem.getQuantity(),
                            cartItem.getProductPrice(),
                            cartItem.getProductImage()
                    );
                    items.add(orderItem);
                }
            } else {
                // Single product checkout
                Order.OrderItem item = new Order.OrderItem(
                        productId,
                        productName,
                        quantity,
                        productPrice,
                        productImage
                );
                items.add(item);
            }

            // Get delivery address (make it final for lambda)
            String addressText = tvAddressDetails.getText().toString();
            final String deliveryAddress = addressText.isEmpty()
                    ? "123 Main Street, Apartment 4B\nNew York, NY 10001"
                    : addressText;

            // Create order
            Order order = new Order(
                    null, // Will be auto-generated
                    currentUser.getUid(),
                    items,
                    totalAmount,
                    deliveryAddress,
                    selectedPaymentMethod
            );

            order.setStatus("pending");
            order.setPaid(selectedPaymentMethod.equals("UPI Payment") ||
                          selectedPaymentMethod.equals("Credit/Debit Card"));

            // Save order to Firebase
            orderRepository.createOrder(order)
                    .addOnSuccessListener(orderId -> {
                        showLoading(false);
                        Log.d(TAG, "Order created successfully: " + orderId);

                        // Clear cart if this was a cart checkout
                        if (isCartCheckout) {
                            CartManager.getInstance(this).clearCart();
                            Log.d(TAG, "Cart cleared after order placement");
                        }

                        // Navigate to order confirmation
                        Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                        intent.putExtra("order_id", orderId);
                        intent.putExtra("total_amount", totalAmount);
                        intent.putExtra("payment_method", selectedPaymentMethod);
                        intent.putExtra("delivery_address", deliveryAddress);
                        intent.putExtra("item_count", items.size());
                        intent.putExtra("user_id", currentUser.getUid());

                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Log.e(TAG, "Error creating order", e);
                        Toast.makeText(this, "Failed to place order: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error in placeOrder", e);
            Toast.makeText(this, "Error placing order: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void fetchAndDisplayDefaultAddress() {
        try {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                Log.d(TAG, "No authenticated user - leaving displayed address as-is");
                return;
            }

            // Show a quick placeholder while we fetch the real address
            if (tvAddressDetails != null) tvAddressDetails.setText("Loading address...");

            String uid = currentUser.getUid();
            Log.d(TAG, "Loading default address for user: " + uid);

            // Query addresses collection for default address
            db.collection("addresses")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("isDefault", true)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(qds -> {
                        if (!qds.isEmpty()) {
                            Address addr = qds.getDocuments().get(0).toObject(Address.class);
                            if (addr != null) {
                                String formatted = addr.getFormattedAddress();
                                if (tvAddressDetails != null) tvAddressDetails.setText(formatted);
                                if (tvAddressName != null && addr.getAddressType() != null)
                                    tvAddressName.setText(addr.getAddressType());
                                if (tvPhoneNumber != null && addr.getPhoneNumber() != null)
                                    tvPhoneNumber.setText(addr.getPhoneNumber());
                                Log.d(TAG, "Default address loaded from addresses collection: " + formatted);
                                return;
                            }
                        }

                        // Fallback to user document fields
                        Log.d(TAG, "No default address in addresses collection, falling back to users document");
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        String addr = doc.getString("address");
                                        String city = doc.getString("city");
                                        String state = doc.getString("state");
                                        String pincode = doc.getString("pincode");
                                        String phone = doc.getString("phone");

                                        StringBuilder full = new StringBuilder();
                                        if (addr != null && !addr.isEmpty()) full.append(addr);
                                        if (city != null && !city.isEmpty()) {
                                            if (full.length() > 0) full.append(", ");
                                            full.append(city);
                                        }
                                        if (state != null && !state.isEmpty()) {
                                            if (full.length() > 0) full.append(", ");
                                            full.append(state);
                                        }
                                        if (pincode != null && !pincode.isEmpty()) {
                                            if (full.length() > 0) full.append(" - ");
                                            full.append(pincode);
                                        }

                                        if (full.length() > 0) {
                                            if (tvAddressDetails != null) tvAddressDetails.setText(full.toString());
                                            if (tvPhoneNumber != null && phone != null) tvPhoneNumber.setText(phone);
                                            Log.d(TAG, "Loaded address from user document: " + full.toString());
                                        } else {
                                            Log.d(TAG, "No address fields in user document; leaving existing display");
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error reading users document", e));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error querying addresses collection", e);
                        // Try the user document as final fallback
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        String addr = doc.getString("address");
                                        String city = doc.getString("city");
                                        String state = doc.getString("state");
                                        String pincode = doc.getString("pincode");

                                        StringBuilder full = new StringBuilder();
                                        if (addr != null && !addr.isEmpty()) full.append(addr);
                                        if (city != null && !city.isEmpty()) {
                                            if (full.length() > 0) full.append(", ");
                                            full.append(city);
                                        }
                                        if (state != null && !state.isEmpty()) {
                                            if (full.length() > 0) full.append(", ");
                                            full.append(state);
                                        }
                                        if (pincode != null && !pincode.isEmpty()) {
                                            if (full.length() > 0) full.append(" - ");
                                            full.append(pincode);
                                        }

                                        if (full.length() > 0) {
                                            if (tvAddressDetails != null) tvAddressDetails.setText(full.toString());
                                            Log.d(TAG, "Loaded address from user document after failure: " + full.toString());
                                        }
                                    }
                                })
                                .addOnFailureListener(err -> Log.e(TAG, "Error reading user document in fallback", err));
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchAndDisplayDefaultAddress", e);
        }
    }
}
