package com.grocerygo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grocerygo.models.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * CartManager - Singleton class to manage shopping cart
 * Handles adding, removing, updating items in cart
 * Persists cart data using SharedPreferences
 */
public class CartManager {
    public static final int MAX_QUANTITY = 10;

    private static CartManager instance;
    private List<CartItem> cartItems;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static final String PREF_NAME = "CartPreferences";
    private static final String CART_ITEMS_KEY = "cart_items";

    private List<CartUpdateListener> listeners = new ArrayList<>();

    private CartManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCartFromPreferences();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    /**
     * Add item to cart or increment quantity if already exists
     */
    public synchronized void addToCart(CartItem item) {
        // Validate
        if (item == null || item.getProductId() == null) {
            return;
        }

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        boolean changed = false;

        // Try to find an existing item by productId (safe lookup)
        CartItem existing = getCartItem(item.getProductId());
        if (existing != null) {
            // Accumulate quantities and cap at MAX_QUANTITY
            int existingQty = existing.getQuantity();
            int incomingQty = item.getQuantity() <= 0 ? 1 : item.getQuantity();
            int newQty = existingQty + incomingQty;
            if (newQty > MAX_QUANTITY) newQty = MAX_QUANTITY;

            if (newQty != existingQty) {
                existing.setQuantity(newQty);
                changed = true;
            }
        } else {
            // New item - ensure quantity is within bounds (1..MAX_QUANTITY)
            int qty = item.getQuantity();
            if (qty <= 0) qty = 1;
            if (qty > MAX_QUANTITY) qty = MAX_QUANTITY;
            item.setQuantity(qty);
            cartItems.add(item);
            changed = true;
        }

        if (changed) {
            saveCartToPreferences();
            notifyListeners();
        }
    }

    /**
     * Remove item from cart
     */
    public synchronized void removeFromCart(String productId) {
        if (cartItems != null) {
            boolean removed = cartItems.removeIf(item -> item.getProductId().equals(productId));
            if (removed) {
                saveCartToPreferences();
                notifyListeners();
            }
        }
    }

    /**
     * Update item quantity
     */
    public synchronized void updateQuantity(String productId, int newQuantity) {
        if (productId == null || cartItems == null) return;

        // Cap the incoming quantity to MAX_QUANTITY
        if (newQuantity > MAX_QUANTITY) newQuantity = MAX_QUANTITY;

        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                int currentQty = item.getQuantity();
                if (newQuantity <= 0) {
                    // remove
                    removeFromCart(productId);
                } else if (newQuantity != currentQty) {
                    item.setQuantity(newQuantity);
                    saveCartToPreferences();
                    notifyListeners();
                }
                return;
            }
        }

        // If item not found and newQuantity > 0, optionally add it (respect cap)
        if (newQuantity > 0) {
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setQuantity(newQuantity);
            cartItems.add(newItem);
            saveCartToPreferences();
            notifyListeners();
        }
    }

    /**
     * Get all cart items
     */
    public List<CartItem> getCartItems() {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        return new ArrayList<>(cartItems);
    }

    /**
     * Get cart item by product ID
     */
    public CartItem getCartItem(String productId) {
        if (productId == null || cartItems == null) return null;
        for (CartItem item : cartItems) {
            String id = item.getProductId();
            if (id != null && id.equals(productId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get total number of items in cart
     */
    public int getCartItemCount() {
        if (cartItems == null) {
            return 0;
        }
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * Get total price of all items in cart
     */
    public double getCartTotal() {
        if (cartItems == null) {
            return 0.0;
        }
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    /**
     * Clear entire cart
     */
    public synchronized void clearCart() {
        if (cartItems != null && !cartItems.isEmpty()) {
            cartItems.clear();
            saveCartToPreferences();
            notifyListeners();
        }
    }

    /**
     * Check if product is in cart
     */
    public boolean isInCart(String productId) {
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                if (item.getProductId().equals(productId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get quantity of specific product in cart
     */
    public int getProductQuantity(String productId) {
        CartItem item = getCartItem(productId);
        return item != null ? item.getQuantity() : 0;
    }

    /**
     * Save cart to SharedPreferences
     */
    private void saveCartToPreferences() {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(CART_ITEMS_KEY, json).apply();
    }

    /**
     * Load cart from SharedPreferences
     */
    private void loadCartFromPreferences() {
        String json = sharedPreferences.getString(CART_ITEMS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            cartItems = gson.fromJson(json, type);
        } else {
            cartItems = new ArrayList<>();
        }
    }

    /**
     * Register listener for cart updates
     */
    public void addCartUpdateListener(CartUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister listener
     */
    public void removeCartUpdateListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of cart update
     */
    private void notifyListeners() {
        // Always invoke listeners on the main (UI) thread so UI code does not need to call runOnUiThread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            for (CartUpdateListener listener : listeners) {
                try {
                    listener.onCartUpdated(getCartItemCount());
                } catch (Exception ignored) {
                    // Avoid one failing listener from preventing others
                }
            }
        });
    }

    /**
     * Interface for cart update callbacks
     */
    public interface CartUpdateListener {
        void onCartUpdated(int itemCount);
    }
}
