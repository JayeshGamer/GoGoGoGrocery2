package com.grocerygo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.adapters.ProductAdapter;
import com.grocerygo.app.R;
import com.grocerygo.models.Product;
import com.grocerygo.utils.WishlistManager;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity implements WishlistManager.WishlistUpdateListener {
    private static final String TAG = "WishlistActivity";

    private RecyclerView rvWishlist;
    private ProductAdapter productAdapter;
    private List<Product> wishlistProducts;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private TextView tvWishlistCount;

    private FirebaseFirestore db;
    private WishlistManager wishlistManager;
    private boolean isLoadingProducts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        wishlistManager = WishlistManager.getInstance();

        initViews();
        setupRecyclerView();

        // Register for wishlist updates immediately
        wishlistManager.addWishlistUpdateListener(this);

        // Load wishlist after a short delay to ensure WishlistManager has initialized
        rvWishlist.postDelayed(() -> {
            Log.d(TAG, "Loading wishlist with " + wishlistManager.getWishlistCount() + " items");
            loadWishlist();
        }, 300);
    }

    private void initViews() {
        rvWishlist = findViewById(R.id.rvWishlist);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        ImageView btnBack = findViewById(R.id.btnBack);
        tvWishlistCount = findViewById(R.id.tvWishlistCount);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        wishlistProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, wishlistProducts);
        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        rvWishlist.setAdapter(productAdapter);
    }

    private void loadWishlist() {
        if (isLoadingProducts) {
            Log.d(TAG, "Already loading products, skipping duplicate load");
            return;
        }

        isLoadingProducts = true;
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        rvWishlist.setVisibility(View.GONE);

        // Get wishlist product IDs from WishlistManager
        List<String> productIds = wishlistManager.getWishlistProductIds();

        Log.d(TAG, "Loading wishlist with product IDs: " + productIds.size());

        if (productIds.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            isLoadingProducts = false;
            updateEmptyState();
            updateWishlistCount();
            return;
        }

        loadWishlistProducts(productIds);
    }

    private void loadWishlistProducts(List<String> productIds) {
        wishlistProducts.clear();

        if (productIds.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            isLoadingProducts = false;
            updateEmptyState();
            updateWishlistCount();
            return;
        }

        // Firestore 'in' queries support max 10 items, so we need to batch if more
        int batchSize = 10;
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i += batchSize) {
            batches.add(productIds.subList(i, Math.min(i + batchSize, productIds.size())));
        }

        Log.d(TAG, "Loading " + batches.size() + " batches of products");
        loadBatchedProducts(batches, 0);
    }

    private void loadBatchedProducts(List<List<String>> batches, int batchIndex) {
        if (batchIndex >= batches.size()) {
            progressBar.setVisibility(View.GONE);
            isLoadingProducts = false;
            productAdapter.updateList(wishlistProducts);
            updateEmptyState();
            updateWishlistCount();
            Log.d(TAG, "Finished loading all batches. Total products: " + wishlistProducts.size());
            return;
        }

        Log.d(TAG, "Loading batch " + (batchIndex + 1) + " of " + batches.size());

        db.collection("products")
                .whereIn("productId", batches.get(batchIndex))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Batch " + (batchIndex + 1) + " loaded: " + queryDocumentSnapshots.size() + " products");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            wishlistProducts.add(product);
                        }
                    }
                    loadBatchedProducts(batches, batchIndex + 1);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    isLoadingProducts = false;
                    Log.e(TAG, "Failed to load batch " + (batchIndex + 1), e);
                    Toast.makeText(this, "Failed to load products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Still update UI with whatever we have
                    productAdapter.updateList(wishlistProducts);
                    updateEmptyState();
                    updateWishlistCount();
                });
    }

    private void updateEmptyState() {
        if (wishlistProducts.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            Log.d(TAG, "Showing empty state");
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing " + wishlistProducts.size() + " products");
        }
    }

    private void updateWishlistCount() {
        if (tvWishlistCount != null) {
            int count = wishlistProducts.size();
            String countText = count + (count == 1 ? " item" : " items");
            tvWishlistCount.setText(countText);
            Log.d(TAG, "Updated wishlist count: " + countText);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for wishlist updates
        wishlistManager.addWishlistUpdateListener(this);
        // Force reload to ensure we have latest data
        Log.d(TAG, "onResume - reloading wishlist");
        loadWishlist();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listener
        wishlistManager.removeWishlistUpdateListener(this);
    }

    @Override
    public void onWishlistUpdated(int itemCount) {
        // Reload wishlist when it changes in real-time
        Log.d(TAG, "onWishlistUpdated called with " + itemCount + " items");
        runOnUiThread(() -> {
            if (!isLoadingProducts) {
                loadWishlist();
            }
        });
    }
}
