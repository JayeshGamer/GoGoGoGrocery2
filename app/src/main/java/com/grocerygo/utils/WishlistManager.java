package com.grocerygo.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton manager for handling wishlist operations with real-time synchronization
 * across all activities and UI components.
 */
public class WishlistManager {
    private static final String TAG = "WishlistManager";
    private static WishlistManager instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Set<String> wishlistProductIds;
    private final List<WishlistUpdateListener> listeners;
    private ListenerRegistration wishlistListener;

    private WishlistManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        wishlistProductIds = new HashSet<>();
        listeners = new ArrayList<>();

        // Immediately load wishlist data, then start listener
        loadWishlistImmediately();

        // Start listening to wishlist changes
        startWishlistListener();
    }

    public static synchronized WishlistManager getInstance() {
        if (instance == null) {
            instance = new WishlistManager();
        }
        return instance;
    }

    /**
     * Immediately load wishlist data from Firestore (one-time fetch)
     */
    private void loadWishlistImmediately() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, cannot load wishlist");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> wishlist = (List<String>) documentSnapshot.get("wishlist");
                        if (wishlist != null) {
                            wishlistProductIds.clear();
                            wishlistProductIds.addAll(wishlist);
                            Log.d(TAG, "Wishlist loaded immediately: " + wishlist.size() + " items");
                            notifyListeners();
                        } else {
                            Log.d(TAG, "Wishlist field is null, initializing empty wishlist");
                            // Initialize the wishlist field if it doesn't exist
                            initializeWishlistField(currentUser.getUid());
                        }
                    } else {
                        Log.w(TAG, "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading wishlist immediately", e);
                });
    }

    /**
     * Initialize wishlist field in user document if it doesn't exist
     */
    private void initializeWishlistField(String userId) {
        db.collection("users")
                .document(userId)
                .update("wishlist", new ArrayList<String>())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Wishlist field initialized successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error initializing wishlist field", e);
                });
    }

    /**
     * Start listening to real-time wishlist changes from Firestore
     */
    private void startWishlistListener() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, cannot start wishlist listener");
            return;
        }

        // Remove existing listener if any
        if (wishlistListener != null) {
            wishlistListener.remove();
        }

        // Listen to user document for wishlist changes
        wishlistListener = db.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to wishlist changes", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> wishlist = (List<String>) documentSnapshot.get("wishlist");
                        if (wishlist != null) {
                            wishlistProductIds.clear();
                            wishlistProductIds.addAll(wishlist);
                            Log.d(TAG, "Wishlist updated from Firestore: " + wishlist.size() + " items");
                            notifyListeners();
                        }
                    }
                });
    }

    /**
     * Check if a product is in the wishlist
     */
    public boolean isInWishlist(String productId) {
        return productId != null && wishlistProductIds.contains(productId);
    }

    /**
     * Add a product to the wishlist
     */
    public Task<Void> addToWishlist(String productId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new Exception("User not logged in"));
        }

        if (productId == null || productId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                new Exception("Invalid product ID"));
        }

        Log.d(TAG, "Adding product to wishlist: " + productId);

        // Optimistically update local cache
        wishlistProductIds.add(productId);
        notifyListeners();

        // Update Firestore
        return db.collection("users")
                .document(currentUser.getUid())
                .update("wishlist", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product added to wishlist successfully: " + productId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add product to wishlist", e);
                    // Revert optimistic update on failure
                    wishlistProductIds.remove(productId);
                    notifyListeners();
                });
    }

    /**
     * Remove a product from the wishlist
     */
    public Task<Void> removeFromWishlist(String productId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return com.google.android.gms.tasks.Tasks.forException(
                new Exception("User not logged in"));
        }

        if (productId == null || productId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(
                new Exception("Invalid product ID"));
        }

        Log.d(TAG, "Removing product from wishlist: " + productId);

        // Optimistically update local cache
        wishlistProductIds.remove(productId);
        notifyListeners();

        // Update Firestore
        return db.collection("users")
                .document(currentUser.getUid())
                .update("wishlist", FieldValue.arrayRemove(productId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product removed from wishlist successfully: " + productId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove product from wishlist", e);
                    // Revert optimistic update on failure
                    wishlistProductIds.add(productId);
                    notifyListeners();
                });
    }

    /**
     * Toggle wishlist status for a product
     */
    public Task<Void> toggleWishlist(String productId) {
        if (isInWishlist(productId)) {
            return removeFromWishlist(productId);
        } else {
            return addToWishlist(productId);
        }
    }

    /**
     * Get all product IDs in the wishlist
     */
    public List<String> getWishlistProductIds() {
        return new ArrayList<>(wishlistProductIds);
    }

    /**
     * Get the number of items in the wishlist
     */
    public int getWishlistCount() {
        return wishlistProductIds.size();
    }

    /**
     * Clear the wishlist (for logout or other purposes)
     */
    public void clearWishlist() {
        wishlistProductIds.clear();
        if (wishlistListener != null) {
            wishlistListener.remove();
            wishlistListener = null;
        }
        notifyListeners();
    }

    /**
     * Reload wishlist from Firestore (useful after login)
     */
    public void reloadWishlist() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, cannot reload wishlist");
            clearWishlist();
            return;
        }

        // Restart listener to force reload
        startWishlistListener();
    }

    /**
     * Add a listener for wishlist updates
     */
    public void addWishlistUpdateListener(WishlistUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            // Immediately notify new listener of current state
            listener.onWishlistUpdated(wishlistProductIds.size());
        }
    }

    /**
     * Remove a listener
     */
    public void removeWishlistUpdateListener(WishlistUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of wishlist changes
     */
    private void notifyListeners() {
        int count = wishlistProductIds.size();
        for (WishlistUpdateListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onWishlistUpdated(count);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying wishlist listener", e);
            }
        }
    }

    /**
     * Interface for components to listen to wishlist changes
     */
    public interface WishlistUpdateListener {
        void onWishlistUpdated(int itemCount);
    }
}

