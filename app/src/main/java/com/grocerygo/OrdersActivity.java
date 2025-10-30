package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.adapters.OrderAdapter;
import com.grocerygo.app.R;
import com.grocerygo.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private List<Order> filteredOrderList;

    private TextView tvTotalOrders, tvPendingOrders, tvCompletedOrders;
    private TabLayout tabLayout;
    private FrameLayout progressOverlay;
    private LinearLayout llEmptyState;
    private ImageView btnBack, btnFilter;
    private CardView btnStartShopping;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tabLayout = findViewById(R.id.tabLayout);
        progressOverlay = findViewById(R.id.progressOverlay);
        llEmptyState = findViewById(R.id.llEmptyState);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        btnStartShopping = findViewById(R.id.btnStartShopping);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, filteredOrderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
        rvOrders.setNestedScrollingEnabled(false);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = "all";
                        break;
                    case 1:
                        currentFilter = "pending";
                        break;
                    case 2:
                        currentFilter = "confirmed";
                        break;
                    case 3:
                        currentFilter = "delivered";
                        break;
                    case 4:
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

        btnFilter.setOnClickListener(v -> {
            // TODO: Implement advanced filter dialog (by date, amount, etc.)
            Toast.makeText(this, "Advanced filters coming soon", Toast.LENGTH_SHORT).show();
        });

        btnStartShopping.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void loadOrders() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to view orders", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        showLoading(true);

        // Query orders without orderBy to avoid index requirement
        // We'll sort locally after fetching
        db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        orderList.add(order);
                    }

                    // Sort orders by date locally (newest first)
                    orderList.sort((o1, o2) -> {
                        if (o1.getOrderDate() == null) return 1;
                        if (o2.getOrderDate() == null) return -1;
                        return o2.getOrderDate().compareTo(o1.getOrderDate());
                    });

                    updateStats();
                    filterOrders();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Log the error for debugging
                    android.util.Log.e("OrdersActivity", "Error loading orders", e);
                    updateEmptyState();
                });
    }

    private void filterOrders() {
        filteredOrderList.clear();

        if (currentFilter.equals("all")) {
            filteredOrderList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                if (order.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredOrderList.add(order);
                }
            }
        }

        orderAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateStats() {
        int totalOrders = orderList.size();
        int pendingOrders = 0;
        int completedOrders = 0;

        for (Order order : orderList) {
            String status = order.getStatus().toLowerCase();
            if (status.equals("pending") || status.equals("confirmed")) {
                pendingOrders++;
            } else if (status.equals("delivered")) {
                completedOrders++;
            }
        }

        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvCompletedOrders.setText(String.valueOf(completedOrders));
    }

    private void showLoading(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        if (filteredOrderList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }
}
