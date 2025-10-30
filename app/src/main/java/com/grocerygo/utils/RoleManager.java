package com.grocerygo.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to manage user roles in Firestore
 * Use this to set users as admin, delivery partner, or customer
 */
public class RoleManager {
    private static final String TAG = "RoleManager";
    private final FirebaseFirestore db;

    public RoleManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Set a user's role to admin
     * @param userId The user ID to update
     * @return Task for the update operation
     */
    public Task<Void> setUserAsAdmin(String userId) {
        return setUserRole(userId, "admin");
    }

    /**
     * Set a user's role to delivery partner
     * @param userId The user ID to update
     * @return Task for the update operation
     */
    public Task<Void> setUserAsDeliveryPartner(String userId) {
        return setUserRole(userId, "delivery");
    }

    /**
     * Set a user's role to customer (default)
     * @param userId The user ID to update
     * @return Task for the update operation
     */
    public Task<Void> setUserAsCustomer(String userId) {
        return setUserRole(userId, "customer");
    }

    /**
     * Set a user's role in Firestore
     * @param userId The user ID to update
     * @param role The role to set (admin, delivery, or customer)
     * @return Task for the update operation
     */
    private Task<Void> setUserRole(String userId, String role) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);

        return db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                    Log.d(TAG, "User " + userId + " role updated to: " + role))
                .addOnFailureListener(e ->
                    Log.e(TAG, "Failed to update user role", e));
    }

    /**
     * Get a user's current role
     * @param userId The user ID to check
     * @return Task that returns the role string
     */
    public Task<String> getUserRole(String userId) {
        return db.collection("users")
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String role = task.getResult().getString("role");
                        return role != null ? role : "customer";
                    }
                    return "customer";
                });
    }
}

