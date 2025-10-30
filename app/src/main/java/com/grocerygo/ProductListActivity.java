package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.grocerygo.adapters.ProductAdapter;
import com.grocerygo.app.R;
import com.grocerygo.firebase.ProductRepository;
import com.grocerygo.models.Product;
import com.grocerygo.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private static final String TAG = "ProductListActivity";

    private ImageView btnBack, btnCart;
    private TextView tvCartBadge;
    private EditText etSearch;
    private TextView tvCategoryName, tvProductCount;
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private BottomNavigationView bottomNavigation;

    private ProductAdapter productAdapter;
    private ProductRepository productRepository;
    private CartManager cartManager;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private String categoryId = null;
    private String categoryName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Initialize repository
        productRepository = new ProductRepository();
        cartManager = CartManager.getInstance(this);

        // Get intent extras
        getIntentData();

        // Initialize views
        initViews();

        // Setup UI
        setupRecyclerView();
        setupSearchFunctionality();
        setupClickListeners();
        setupBottomNavigation();

        // Load products
        loadProducts();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            categoryId = intent.getStringExtra("category_id");
            categoryName = intent.getStringExtra("category_name");
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        etSearch = findViewById(R.id.etSearch);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvProductCount = findViewById(R.id.tvProductCount);
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Show category name if available
        if (categoryName != null && !categoryName.isEmpty()) {
            tvCategoryName.setVisibility(View.VISIBLE);
            tvCategoryName.setText(categoryName);
        }
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, filteredProducts);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupSearchFunctionality() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening cart", e);
                    Toast.makeText(ProductListActivity.this, "Error opening cart", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_categories);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ProductListActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    // Already on products/categories page
                    return true;
                } else if (itemId == R.id.nav_orders) {
                    startActivity(new Intent(ProductListActivity.this, OrdersActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(ProductListActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    private void loadProducts() {
        showLoading(true);

        // Load products based on whether we have a category filter
        if (categoryId != null && !categoryId.isEmpty()) {
            loadProductsByCategory();
        } else {
            loadAllProducts();
        }
    }

    private void loadAllProducts() {
        productRepository.getAllProducts()
                .addOnSuccessListener(products -> {
                    showLoading(false);
                    allProducts.clear();
                    allProducts.addAll(products);
                    filteredProducts.clear();
                    filteredProducts.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    updateProductCount();
                    updateEmptyState();
                    Log.d(TAG, "Loaded " + products.size() + " products");
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading products", e);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void loadProductsByCategory() {
        productRepository.getProductsByCategoryId(categoryId)
                .addOnSuccessListener(products -> {
                    showLoading(false);
                    allProducts.clear();
                    allProducts.addAll(products);
                    filteredProducts.clear();
                    filteredProducts.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    updateProductCount();
                    updateEmptyState();
                    Log.d(TAG, "Loaded " + products.size() + " products for category " + categoryName);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading products for category", e);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void filterProducts(String query) {
        filteredProducts.clear();

        if (query.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Product product : allProducts) {
                if (product.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    (product.getDescription() != null &&
                     product.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))) {
                    filteredProducts.add(product);
                }
            }
        }

        productAdapter.notifyDataSetChanged();
        updateProductCount();
        updateEmptyState();
    }

    private void updateProductCount() {
        if (tvProductCount != null) {
            int count = filteredProducts.size();
            String text = count + (count == 1 ? " Product" : " Products");
            tvProductCount.setText(text);
        }
    }

    private void updateEmptyState() {
        if (llEmptyState != null && rvProducts != null) {
            if (filteredProducts.isEmpty()) {
                llEmptyState.setVisibility(View.VISIBLE);
                rvProducts.setVisibility(View.GONE);
            } else {
                llEmptyState.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvProducts != null) {
            rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register cart update listener
        cartManager.addCartUpdateListener(this);
        updateCartBadge();
        // Refresh the adapter in case cart state changed
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister cart update listener
        cartManager.removeCartUpdateListener(this);
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
