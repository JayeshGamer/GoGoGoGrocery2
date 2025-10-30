package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerygo.adapters.ProductAdapter;
import com.grocerygo.app.R;
import com.grocerygo.firebase.ProductRepository;
import com.grocerygo.models.Product;
import com.grocerygo.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private static final String TAG = "SearchActivity";

    private ImageView btnBack, btnClearSearch;
    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private View llEmptyState;
    private TextView tvCartBadge;
    private ImageView ivCart;
    private CartManager cartManager;

    private ProductAdapter productAdapter;
    private ProductRepository productRepository;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();
    private boolean isProductsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize repository
        productRepository = new ProductRepository();
        cartManager = CartManager.getInstance(this);

        // Initialize views
        initViews();
        setupRecyclerView();
        setupSearchFunctionality();

        // Load all products for searching
        loadProducts();
    }

    private void initViews() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Products");
        }

        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.ivClearSearch);
        etSearch = findViewById(R.id.etSearchQuery);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.tvNoResults);
        ivCart = toolbar != null ? toolbar.findViewById(R.id.ivCart) : null;
        tvCartBadge = toolbar != null ? toolbar.findViewById(R.id.tvCartBadge) : null;

        // Set focus on search field
        if (etSearch != null) {
            etSearch.requestFocus();
        }

        // Setup cart icon click listener
        if (ivCart != null) {
            ivCart.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, searchResults);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvSearchResults.setLayoutManager(gridLayoutManager);
        rvSearchResults.setAdapter(productAdapter);
    }

    private void setupSearchFunctionality() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Clear search button
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                    etSearch.requestFocus();
                }
                btnClearSearch.setVisibility(View.GONE);
            });
        }

        // Search text watcher
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();

                    // Show/hide clear button
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    // Perform search
                    performSearch(query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadProducts() {
        showLoading(true);
        isProductsLoaded = false;

        // Load ALL products from database, not just featured ones
        productRepository.getAllProducts()
                .addOnSuccessListener(productList -> {
                    showLoading(false);

                    if (productList != null && !productList.isEmpty()) {
                        allProducts.clear();
                        allProducts.addAll(productList);
                        isProductsLoaded = true;
                        Log.d(TAG, "Loaded " + productList.size() + " products for search");

                        // If user has already typed something, perform search
                        if (etSearch != null && !etSearch.getText().toString().trim().isEmpty()) {
                            performSearch(etSearch.getText().toString().trim());
                        }
                    } else {
                        Log.w(TAG, "No products found in database");
                        Toast.makeText(this, "No products available", Toast.LENGTH_SHORT).show();
                    }

                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    isProductsLoaded = false;
                    Log.e(TAG, "Error loading products", e);
                    Toast.makeText(this, "Failed to load products. Please check your connection.", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void performSearch(String query) {
        searchResults.clear();

        if (!isProductsLoaded) {
            // Products not loaded yet, wait
            Log.d(TAG, "Products not loaded yet, skipping search");
            updateEmptyState();
            return;
        }

        if (query.isEmpty()) {
            // Show empty state with hint when no query
            Log.d(TAG, "Empty query, showing empty state");
        } else {
            // Filter products by query (case-insensitive, null-safe)
            String lowerQuery = query.toLowerCase(Locale.getDefault());

            try {
                for (Product product : allProducts) {
                    // Null-safe search across multiple fields
                    boolean matchesName = product.getName() != null &&
                                         product.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery);

                    boolean matchesDescription = product.getDescription() != null &&
                                                 product.getDescription().toLowerCase(Locale.getDefault()).contains(lowerQuery);

                    boolean matchesCategory = product.getCategory() != null &&
                                             product.getCategory().toLowerCase(Locale.getDefault()).contains(lowerQuery);

                    if (matchesName || matchesDescription || matchesCategory) {
                        searchResults.add(product);
                    }
                }

                Log.d(TAG, "Search for '" + query + "' returned " + searchResults.size() + " results out of " + allProducts.size() + " total products");
            } catch (Exception e) {
                Log.e(TAG, "Error during search", e);
                Toast.makeText(this, "Search error occurred", Toast.LENGTH_SHORT).show();
            }
        }

        productAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        if (llEmptyState != null) {
            llEmptyState.setVisibility(show ? View.GONE : llEmptyState.getVisibility());
        }
    }

    private void updateEmptyState() {
        if (llEmptyState == null || rvSearchResults == null) {
            return;
        }

        String currentQuery = etSearch != null ? etSearch.getText().toString().trim() : "";

        // Show empty state only when:
        // 1. Products are loaded
        // 2. User has typed something
        // 3. No results found
        boolean shouldShowEmpty = isProductsLoaded &&
                                 !currentQuery.isEmpty() &&
                                 searchResults.isEmpty();

        llEmptyState.setVisibility(shouldShowEmpty ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(shouldShowEmpty ? View.GONE : View.VISIBLE);

        Log.d(TAG, "Empty state: " + shouldShowEmpty + " (loaded: " + isProductsLoaded +
              ", query: '" + currentQuery + "', results: " + searchResults.size() + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.addCartUpdateListener(this);
        updateCartBadge();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cartManager.removeCartUpdateListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onCartUpdated(int itemCount) {
        runOnUiThread(this::updateCartBadge);
    }

    private void updateCartBadge() {
        if (tvCartBadge != null && cartManager != null) {
            int itemCount = cartManager.getCartItemCount();
            if (itemCount > 0) {
                tvCartBadge.setVisibility(View.VISIBLE);
                tvCartBadge.setText(String.valueOf(itemCount));
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }
}
