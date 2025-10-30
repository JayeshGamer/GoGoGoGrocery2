package com.grocerygo.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.grocerygo.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private final FirebaseFirestore db;
    private static final String COLLECTION_ORDERS = "orders";

    public OrderRepository() {
        try {
            this.db = FirebaseManager.getInstance().getDb();
            Log.d(TAG, "OrderRepository initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OrderRepository", e);
            throw new RuntimeException("Failed to initialize OrderRepository", e);
        }
    }

    // Create a new order
    public Task<String> createOrder(Order order) {
        try {
            if (order == null) {
                Log.e(TAG, "Order is null");
                return Tasks.forException(new IllegalArgumentException("Order cannot be null"));
            }

            String orderId = db.collection(COLLECTION_ORDERS).document().getId();
            order.setOrderId(orderId);

            Log.d(TAG, "Creating order with ID: " + orderId);

            return db.collection(COLLECTION_ORDERS)
                    .document(orderId)
                    .set(order)
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Order created successfully: " + orderId);
                            return orderId;
                        } else {
                            Exception e = task.getException();
                            Log.e(TAG, "Error creating order", e);
                            if (e != null) {
                                throw e;
                            }
                            throw new Exception("Unknown error creating order");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in createOrder", e);
            return Tasks.forException(e);
        }
    }

    // Get all orders for a user
    public Task<List<Order>> getUserOrders(String userId) {
        return db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Order> orders = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        orders = task.getResult().toObjects(Order.class);
                        Log.d(TAG, "Orders fetched for user: " + orders.size());
                    } else {
                        Log.e(TAG, "Error getting orders", task.getException());
                    }
                    return orders;
                });
    }

    // Get order by ID
    public Task<Order> getOrderById(String orderId) {
        return db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Order.class);
                    }
                    return null;
                });
    }

    // Update order status
    public Task<Void> updateOrderStatus(String orderId, String status) {
        return db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update("status", status);
    }

    // Cancel order
    public Task<Void> cancelOrder(String orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    // Update payment status
    public Task<Void> updatePaymentStatus(String orderId, boolean isPaid) {
        return db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update("isPaid", isPaid);
    }
}
