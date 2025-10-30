package com.grocerygo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.app.R;
import com.grocerygo.firebase.AuthRepository;
import com.grocerygo.models.Order;
import com.grocerygo.models.User;
import com.grocerygo.adapters.OrderAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView rvAdminOrders;
    private OrderAdapter orderAdapter;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private List<User> deliveryPartners;

    private TextView tvTotalOrders, tvPendingOrders, tvConfirmedOrders, tvDeliveredOrders;
    private TabLayout tabLayout;
    private FrameLayout progressOverlay;
    private LinearLayout llEmptyState;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AuthRepository authRepository;
    private String currentFilter = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authRepository = new AuthRepository();

        // Verify user is admin
        verifyAdminAccess();

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        loadDeliveryPartners();
        loadAllOrders();
    }

    private void verifyAdminAccess() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if user has admin role in Firestore
        authRepository.getUserData(currentUser.getUid())
                .addOnSuccessListener(user -> {
                    if (user == null || !user.isAdmin()) {
                        Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify admin access", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initViews() {
        rvAdminOrders = findViewById(R.id.rvAdminOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvConfirmedOrders = findViewById(R.id.tvConfirmedOrders);
        tvDeliveredOrders = findViewById(R.id.tvDeliveredOrders);
        tabLayout = findViewById(R.id.tabLayout);
        progressOverlay = findViewById(R.id.progressOverlay);
        llEmptyState = findViewById(R.id.llEmptyState);
        btnBack = findViewById(R.id.btnBack);

        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();
        deliveryPartners = new ArrayList<>();
    }

    private void setupRecyclerView() {
        // Use the existing OrderAdapter with long click for admin actions
        orderAdapter = new OrderAdapter(this, filteredOrders);
        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        rvAdminOrders.setAdapter(orderAdapter);

        // Add item click listener for admin actions
        orderAdapter.setOnOrderClickListener(order -> showOrderActionsDialog(order));
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = "pending";
                        break;
                    case 1:
                        currentFilter = "confirmed";
                        break;
                    case 2:
                        currentFilter = "delivered";
                        break;
                    case 3:
                        currentFilter = "cancelled";
                        break;
                }
                filterOrders();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDeliveryPartners() {
        db.collection("users")
                .whereEqualTo("role", "delivery")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    deliveryPartners.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        deliveryPartners.add(user);
                    }
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to load delivery partners", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadAllOrders() {
        showLoading(true);

        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allOrders.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        allOrders.add(order);
                    }

                    updateOrderStats();
                    filterOrders();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterOrders() {
        filteredOrders.clear();

        for (Order order : allOrders) {
            if (currentFilter.equals("pending") && "pending".equals(order.getStatus())) {
                filteredOrders.add(order);
            } else if (currentFilter.equals("confirmed") && "confirmed".equals(order.getStatus())) {
                filteredOrders.add(order);
            } else if (currentFilter.equals("delivered") && "delivered".equals(order.getStatus())) {
                filteredOrders.add(order);
            } else if (currentFilter.equals("cancelled") && "cancelled".equals(order.getStatus())) {
                filteredOrders.add(order);
            }
        }

        orderAdapter.notifyDataSetChanged();

        if (filteredOrders.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvAdminOrders.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvAdminOrders.setVisibility(View.VISIBLE);
        }
    }

    private void updateOrderStats() {
        int total = allOrders.size();
        int pending = 0;
        int confirmed = 0;
        int delivered = 0;

        for (Order order : allOrders) {
            if ("pending".equals(order.getStatus())) {
                pending++;
            } else if ("confirmed".equals(order.getStatus())) {
                confirmed++;
            } else if ("delivered".equals(order.getStatus())) {
                delivered++;
            }
        }

        tvTotalOrders.setText(String.valueOf(total));
        tvPendingOrders.setText(String.valueOf(pending));
        tvConfirmedOrders.setText(String.valueOf(confirmed));
        tvDeliveredOrders.setText(String.valueOf(delivered));
    }

    private void showOrderActionsDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Actions - #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

        List<String> options = new ArrayList<>();

        // Add options based on order status
        if ("pending".equals(order.getStatus())) {
            if (order.getAssignedDeliveryPartner() == null) {
                options.add("Assign Delivery Partner");
            } else {
                options.add("Reassign Delivery Partner");
            }

            if (order.getAssignedDeliveryPartner() != null) {
                options.add("Confirm Order");
            }
            options.add("Cancel Order");
        } else if ("confirmed".equals(order.getStatus())) {
            options.add("Reassign Delivery Partner");
            options.add("Mark as Delivered");
            options.add("Cancel Order");
        } else if ("delivered".equals(order.getStatus())) {
            options.add("View Order Details");
        }

        String[] optionsArray = options.toArray(new String[0]);

        builder.setItems(optionsArray, (dialog, which) -> {
            String selectedOption = optionsArray[which];

            switch (selectedOption) {
                case "Assign Delivery Partner":
                case "Reassign Delivery Partner":
                    showDeliveryPartnerDialog(order);
                    break;
                case "Confirm Order":
                    confirmOrder(order);
                    break;
                case "Mark as Delivered":
                    markOrderAsDelivered(order);
                    break;
                case "Cancel Order":
                    cancelOrder(order);
                    break;
                case "View Order Details":
                    Toast.makeText(this, "Order Details: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showDeliveryPartnerDialog(Order order) {
        if (deliveryPartners.isEmpty()) {
            Toast.makeText(this, "No delivery partners available. Please add delivery partners first.", Toast.LENGTH_LONG).show();
            return;
        }

        String[] partnerNames = new String[deliveryPartners.size()];
        for (int i = 0; i < deliveryPartners.size(); i++) {
            partnerNames[i] = deliveryPartners.get(i).getName() + " (" + deliveryPartners.get(i).getPhone() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Delivery Partner");
        builder.setItems(partnerNames, (dialog, which) -> {
            User selectedPartner = deliveryPartners.get(which);
            assignDeliveryPartner(order, selectedPartner);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void assignDeliveryPartner(Order order, User deliveryPartner) {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedDeliveryPartner", deliveryPartner.getUserId());
        updates.put("deliveryPartnerName", deliveryPartner.getName());
        // Automatically confirm order when delivery partner is assigned
        updates.put("confirmed", true);
        updates.put("status", "confirmed");

        db.collection("orders")
                .document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Delivery partner assigned and order confirmed! Auto-delivery in 20 seconds...", Toast.LENGTH_LONG).show();

                    // Update local order object
                    order.setAssignedDeliveryPartner(deliveryPartner.getUserId());
                    order.setDeliveryPartnerName(deliveryPartner.getName());
                    order.setConfirmed(true);
                    order.setStatus("confirmed");

                    // Schedule automatic delivery after 20 seconds
                    scheduleAutoDelivery(order);

                    // Refresh the list
                    loadAllOrders();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to assign delivery partner: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Automatically marks order as delivered after 20 seconds
     */
    private void scheduleAutoDelivery(Order order) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Update order status to delivered in Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "delivered");

            db.collection("orders")
                    .document(order.getOrderId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) + " automatically delivered!", Toast.LENGTH_SHORT).show();

                        // Update local order object
                        order.setStatus("delivered");

                        // Refresh the list to show updated status
                        loadAllOrders();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to auto-deliver order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }, 20000); // 20 seconds delay
    }

    private void confirmOrder(Order order) {
        if (order.getAssignedDeliveryPartner() == null) {
            Toast.makeText(this, "Please assign a delivery partner first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("Confirm order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) +
                           "?\n\nDelivery Partner: " + order.getDeliveryPartnerName())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    showLoading(true);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("confirmed", true);
                    updates.put("status", "confirmed");

                    db.collection("orders")
                            .document(order.getOrderId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                Toast.makeText(this, "Order confirmed successfully!", Toast.LENGTH_SHORT).show();

                                // Update local order object
                                order.setConfirmed(true);
                                order.setStatus("confirmed");

                                // Refresh the list
                                loadAllOrders();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Failed to confirm order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markOrderAsDelivered(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Delivered")
                .setMessage("Mark order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) + " as delivered?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    updateOrderStatus(order, "delivered");
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelOrder(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    updateOrderStatus(order, "cancelled");
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateOrderStatus(Order order, String status) {
        showLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        db.collection("orders")
                .document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Order status updated to " + status, Toast.LENGTH_SHORT).show();

                    // Update local order object
                    order.setStatus(status);

                    // Refresh the list
                    loadAllOrders();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to update order status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
