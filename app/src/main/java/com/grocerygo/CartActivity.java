package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerygo.adapters.CartItemAdapter;
import com.grocerygo.app.R;
import com.grocerygo.models.CartItem;
import com.grocerygo.utils.CartManager;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private static final String TAG = "CartActivity";

    // Static flag to track if cart needs refresh
    private static boolean needsRefresh = false;

    private RecyclerView rvCartItems;
    private LinearLayout llEmptyCart;
    private TextView tvSubtotal, tvDeliveryFee, tvDiscount, tvTotal;
    private CardView btnBack, btnCheckout, cvOrderSummary;

    private CartManager cartManager;
    private CartItemAdapter cartItemAdapter;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize cart manager
        cartManager = CartManager.getInstance(this);

        // Initialize views
        initViews();

        // Load cart items
        loadCartItems();

        // Setup click listeners
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Refresh cart when activity starts
        loadCartItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.addCartUpdateListener(this);
        // Force refresh cart items
        loadCartItems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cartManager.removeCartUpdateListener(this);
    }

    @Override
    public void onCartUpdated(int itemCount) {
        runOnUiThread(this::loadCartItems);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCartItems = findViewById(R.id.rvCartItems);
        llEmptyCart = findViewById(R.id.llEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        cvOrderSummary = findViewById(R.id.cvOrderSummary);

        // Set default visibility for discount (hide initially)
        if (tvDiscount != null) {
            tvDiscount.setVisibility(View.GONE);
        }

        // Verify critical views are not null
        if (rvCartItems == null || llEmptyCart == null) {
            Log.e(TAG, "Critical views are null! Check activity_cart.xml layout");
            Toast.makeText(this, "Error loading cart layout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void loadCartItems() {
        try {
            cartItems = cartManager.getCartItems();

            if (cartItems == null || cartItems.isEmpty()) {
                showEmptyCart();
            } else {
                showCartItems();
                updateOrderSummary();
            }
        } catch (Exception e) {
            // Handle any errors gracefully
            Log.e(TAG, "Error loading cart items", e);
            showEmptyCart();
            Toast.makeText(this, "Error loading cart items", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyCart() {
        try {
            if (llEmptyCart != null) {
                llEmptyCart.setVisibility(View.VISIBLE);
            }
            if (rvCartItems != null) {
                rvCartItems.setVisibility(View.GONE);
            }
            if (cvOrderSummary != null) {
                cvOrderSummary.setVisibility(View.GONE);
            }
            if (btnCheckout != null) {
                btnCheckout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty cart", e);
        }
    }

    private void showCartItems() {
        try {
            if (llEmptyCart != null) {
                llEmptyCart.setVisibility(View.GONE);
            }
            if (rvCartItems != null) {
                rvCartItems.setVisibility(View.VISIBLE);
            }
            if (cvOrderSummary != null) {
                cvOrderSummary.setVisibility(View.VISIBLE);
            }
            if (btnCheckout != null) {
                btnCheckout.setVisibility(View.VISIBLE);
            }

            // Setup RecyclerView with adapter
            try {
                if (cartItemAdapter == null) {
                    cartItemAdapter = new CartItemAdapter(this, cartItems, cartManager);
                    if (rvCartItems != null) {
                        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
                        rvCartItems.setAdapter(cartItemAdapter);
                    }
                } else {
                    cartItemAdapter.updateItems(cartItems);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up cart adapter", e);
                Toast.makeText(this, "Error displaying cart items", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing cart items", e);
        }
    }

    private void updateOrderSummary() {
        try {
            double subtotal = cartManager.getCartTotal();
            double deliveryFee = subtotal >= 500 ? 0.0 : 40.0;
            double discount = 0.0; // Can be implemented later
            double total = subtotal + deliveryFee - discount;

            if (tvSubtotal != null) {
                tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
            }
            if (tvDeliveryFee != null) {
                if (deliveryFee == 0) {
                    tvDeliveryFee.setText(getString(R.string.free_delivery));
                } else {
                    tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryFee));
                }
            }
            if (tvDiscount != null && discount > 0) {
                tvDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", discount));
                tvDiscount.setVisibility(View.VISIBLE);
            }
            if (tvTotal != null) {
                tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", total));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating totals", e);
            Toast.makeText(this, "Error calculating totals", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                if (cartItems != null && !cartItems.isEmpty()) {
                    // Calculate totals
                    double subtotal = cartManager.getCartTotal();
                    double deliveryFee = subtotal >= 500 ? 0.0 : 40.0;
                    double total = subtotal + deliveryFee;

                    // Navigate to checkout with cart data
                    Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                    intent.putExtra("is_cart_checkout", true);
                    intent.putExtra("cart_subtotal", subtotal);
                    intent.putExtra("cart_delivery_fee", deliveryFee);
                    intent.putExtra("cart_total", total);
                    intent.putExtra("cart_item_count", cartItems.size());

                    Log.d(TAG, "Navigating to checkout - Subtotal: " + subtotal + ", Total: " + total);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void setNeedsRefresh() {
        needsRefresh = true;
    }
}
