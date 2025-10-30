package com.grocerygo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.adapters.OrderItemAdapter;
import com.grocerygo.app.R;
import com.grocerygo.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvDeliveryAddress;
    private TextView tvSubtotal, tvDeliveryFee, tvTax, tvTotal, tvPaymentMethod;
    private RecyclerView rvOrderItems;
    private ProgressBar progressBar;
    private LinearLayout llOrderDetails;
    private ImageView btnBack;

    private OrderItemAdapter orderItemAdapter;
    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra("order_id");

        if (orderId == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        progressBar = findViewById(R.id.progressBar);
        llOrderDetails = findViewById(R.id.llOrderDetails);

        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(this, new ArrayList<>());
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void loadOrderDetails() {
        showLoading(true);

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            displayOrderDetails(order);
                        }
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load order: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void displayOrderDetails(Order order) {
        // Order ID
        tvOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

        // Order Status
        tvOrderStatus.setText(capitalizeFirst(order.getStatus()));

        // Order Date
        if (order.getOrderDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            tvOrderDate.setText(dateFormat.format(order.getOrderDate()));
        }

        // Delivery Address
        tvDeliveryAddress.setText(order.getDeliveryAddress());

        // Order Items
        if (order.getItems() != null) {
            orderItemAdapter.updateItems(order.getItems());
        }

        // Calculate prices
        double subtotal = order.getTotalAmount();
        double deliveryFee = subtotal > 500 ? 0 : 40; // Free delivery above ₹500
        double tax = subtotal * 0.05; // 5% tax
        double total = subtotal + deliveryFee + tax;

        tvSubtotal.setText("₹" + String.format("%.2f", subtotal));
        tvDeliveryFee.setText(deliveryFee == 0 ? "FREE" : "₹" + String.format("%.2f", deliveryFee));
        tvTax.setText("₹" + String.format("%.2f", tax));
        tvTotal.setText("₹" + String.format("%.2f", total));

        // Payment Method
        tvPaymentMethod.setText(order.getPaymentMethod());
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        llOrderDetails.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

