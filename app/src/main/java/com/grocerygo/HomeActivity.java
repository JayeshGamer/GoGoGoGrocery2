package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.grocerygo.adapters.CategoryAdapter;
import com.grocerygo.adapters.ProductAdapter;
import com.grocerygo.app.R;
import com.grocerygo.firebase.AuthRepository;
import com.grocerygo.firebase.CategoryRepository;
import com.grocerygo.firebase.ProductRepository;
import com.grocerygo.models.Category;
import com.grocerygo.models.Product;
import com.grocerygo.models.User;
import com.grocerygo.models.Address;
import com.grocerygo.utils.CartManager;
import com.grocerygo.utils.DataPreloader;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private static final String TAG = "HomeActivity";

    private RecyclerView rvProducts, rvCategories;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CartManager cartManager;
    private TextView tvCartBadge;
    private DataPreloader dataPreloader;

    private List<Product> featuredProducts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize repositories and preloader BEFORE setting content view
        productRepository = new ProductRepository();
        categoryRepository = new CategoryRepository();
        cartManager = CartManager.getInstance(this);
        dataPreloader = DataPreloader.getInstance();

        // Pre-populate data lists from cache before inflation
        if (dataPreloader.isDataLoaded()) {
            List<Category> cachedCategories = dataPreloader.getCategories();
            if (cachedCategories != null && !cachedCategories.isEmpty()) {
                categories.addAll(cachedCategories);
            }
            List<Product> cachedProducts = dataPreloader.getFeaturedProducts();
            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                featuredProducts.addAll(cachedProducts);
            }
        }

        setContentView(R.layout.activity_home);

        // Initialize views
        initViews();
        setupRecyclerViews();

        // Load data - use preloaded data if available, otherwise fetch
        // Defer heavy fetches by posting them so the UI can render immediately
        rvProducts.post(() -> {
            if (!dataPreloader.isDataLoaded()) {
                loadCategories();
                loadFeaturedProducts();
            }
        });

        // Update cart badge
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register cart update listener
        cartManager.addCartUpdateListener(this);
        updateCartBadge();

        // Refresh address in case user added/changed it - keep this in onResume only
        loadUserLocation();
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

    private void initViews() {
        // Header icons
        ImageView ivNotifications = findViewById(R.id.ivNotifications);
        ImageView ivWishlist = findViewById(R.id.ivWishlist);
        ImageView ivCart = findViewById(R.id.ivCart);
        ImageView ivMicrophone = findViewById(R.id.ivMicrophone);
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        // Search field
        EditText etSearch = findViewById(R.id.etSearch);

        // See all links
        TextView tvSeeAllStores = findViewById(R.id.tvSeeAllStores);
        TextView tvSeeAllCategories = findViewById(R.id.tvSeeAllCategories);
        TextView tvSeeAllProducts = findViewById(R.id.tvSeeAllProducts);

        // Shop Now button
        androidx.cardview.widget.CardView btnShopNow = findViewById(R.id.btnShopNow);

        // Product and category lists
        rvProducts = findViewById(R.id.rvProducts);
        rvCategories = findViewById(R.id.rvCategories);

        // Improve RecyclerView performance by declaring fixed size when content layout size does not change
        if (rvProducts != null) rvProducts.setHasFixedSize(true);
        if (rvCategories != null) rvCategories.setHasFixedSize(true);

        // Bottom navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load user name from Firebase
        loadUserName(tvUserName);

        // Note: loadUserLocation() call removed from initViews to avoid doing network work before UI is ready.

        // Click handlers - Fixed notification button to go to NotificationsActivity
        ivNotifications.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening notifications", e);
                Toast.makeText(HomeActivity.this, "Error opening notifications", Toast.LENGTH_SHORT).show();
            }
        });

        // Wishlist button click handler
        ivWishlist.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, WishlistActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening wishlist", e);
                Toast.makeText(HomeActivity.this, "Error opening wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        // Updated cart click to navigate to CartActivity
        ivCart.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening cart", e);
                Toast.makeText(HomeActivity.this, "Error opening cart", Toast.LENGTH_SHORT).show();
            }
        });

        ivMicrophone.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        etSearch.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        if (tvSeeAllStores != null) {
            tvSeeAllStores.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                startActivity(intent);
            });
        }

        if (tvSeeAllCategories != null) {
            tvSeeAllCategories.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
                startActivity(intent);
            });
        }

        if (tvSeeAllProducts != null) {
            tvSeeAllProducts.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                startActivity(intent);
            });
        }

        if (btnShopNow != null) {
            btnShopNow.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                startActivity(intent);
            });
        }

        // Bottom navigation behavior
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(HomeActivity.this, CategoriesActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(HomeActivity.this, OrdersActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadDataFromCacheOrFetch() {
        if (dataPreloader.isDataLoaded()) {
            // Use preloaded data for instant display
            Log.d(TAG, "Using preloaded data");
            loadFromCache();
        } else {
            // Fallback: fetch data if not preloaded
            Log.d(TAG, "Preloaded data not available, fetching...");
            loadCategories();
            loadFeaturedProducts();
        }
    }

    private void loadFromCache() {
        // Load categories from cache
        List<Category> cachedCategories = dataPreloader.getCategories();
        if (cachedCategories != null && !cachedCategories.isEmpty()) {
            categories.clear();
            categories.addAll(cachedCategories);
            categoryAdapter.updateList(cachedCategories);
            Log.d(TAG, "Loaded " + cachedCategories.size() + " categories from cache");
        } else {
            // Fallback to fetching if cache is empty
            loadCategories();
        }

        // Load products from cache
        List<Product> cachedProducts = dataPreloader.getFeaturedProducts();
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            featuredProducts.clear();
            featuredProducts.addAll(cachedProducts);
            productAdapter.updateList(cachedProducts);
            Log.d(TAG, "Loaded " + cachedProducts.size() + " products from cache");
        } else {
            // Fallback to fetching if cache is empty
            loadFeaturedProducts();
        }
    }

    private void loadUserName(TextView tvUserName) {
        if (tvUserName == null) return;

        // Try to use preloaded user data first
        User cachedUser = dataPreloader.getCurrentUser();
        if (cachedUser != null && cachedUser.getName() != null && !cachedUser.getName().isEmpty()) {
            tvUserName.setText("Hi, " + cachedUser.getName() + "!");
            Log.d(TAG, "Loaded user name from cache: " + cachedUser.getName());
            return;
        }

        // Fallback to fetching from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Load user data from Firestore to get real name
            AuthRepository authRepository = new AuthRepository();
            authRepository.getUserData(userId)
                    .addOnSuccessListener(user -> {
                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                            tvUserName.setText("Hi, " + user.getName() + "!");
                        } else {
                            // Fallback to Firebase display name
                            String displayName = currentUser.getDisplayName();
                            if (displayName != null && !displayName.isEmpty()) {
                                tvUserName.setText("Hi, " + displayName + "!");
                            } else {
                                tvUserName.setText("Hi, User!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Fallback to Firebase display name on error
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            tvUserName.setText("Hi, " + displayName + "!");
                        } else {
                            tvUserName.setText("Hi, User!");
                        }
                    });
        }
    }

    private void loadUserLocation() {
        TextView tvLocation = findViewById(R.id.tvLocation);
        if (tvLocation == null) return;

        // Make the location clickable to open AddressBookActivity
        try {
            // Prefer attaching click listener directly to the TextView to avoid relying on parent view type
            tvLocation.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(HomeActivity.this, AddressBookActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open AddressBookActivity", e);
                    Toast.makeText(HomeActivity.this, "Unable to open address book", Toast.LENGTH_SHORT).show();
                }
            });
            // Also try to make the parent clickable if available (best-effort)
            if (tvLocation.getParent() instanceof View) {
                View parent = (View) tvLocation.getParent();
                parent.setClickable(true);
                parent.setFocusable(true);
                parent.setOnClickListener(v -> tvLocation.performClick());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error attaching AddressBook click listener", e);
        }

        // Try to use preloaded address data first
        Address cachedAddress = dataPreloader.getDefaultAddress();
        if (cachedAddress != null) {
            String displayAddress = cachedAddress.getCity() + ", " + cachedAddress.getState();
            tvLocation.setText(displayAddress);
            tvLocation.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loaded address from cache: " + displayAddress);
            return;
        }

        // Fallback to fetching from Firebase - run asynchronously (already async) and avoid heavy processing in UI thread
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("addresses")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isDefault", true)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Address mainAddress =
                                    queryDocumentSnapshots.getDocuments().get(0).toObject(Address.class);
                            if (mainAddress != null) {
                                String displayAddress = safeString(mainAddress.getCity()) + ", " + safeString(mainAddress.getState());
                                tvLocation.setText(displayAddress);
                                tvLocation.setVisibility(View.VISIBLE);
                                Log.d(TAG, "Loaded default address: " + displayAddress);
                            } else {
                                tvLocation.setText("Add delivery address");
                                tvLocation.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvLocation.setText("Add delivery address");
                            tvLocation.setVisibility(View.VISIBLE);
                            Log.d(TAG, "No default address found - prompting user to add");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading main address", e);
                        tvLocation.setText("Add delivery address");
                        tvLocation.setVisibility(View.VISIBLE);
                    });
        } else {
            tvLocation.setText("Login to add address");
            tvLocation.setVisibility(View.VISIBLE);
        }
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private void setupRecyclerViews() {
        // Setup categories RecyclerView (grid layout)
        categoryAdapter = new CategoryAdapter(this, categories);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        rvCategories.setLayoutManager(gridLayoutManager);
        rvCategories.setAdapter(categoryAdapter);

        // Setup products RecyclerView (horizontal)
        productAdapter = new ProductAdapter(this, featuredProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvProducts.setAdapter(productAdapter);
    }

    private void loadCategories() {
        categoryRepository.getAllCategories()
                .addOnSuccessListener(categoryList -> {
                    if (categoryList != null && !categoryList.isEmpty()) {
                        categories.clear();
                        categories.addAll(categoryList);
                        categoryAdapter.updateList(categoryList);
                        Log.d(TAG, "Loaded " + categoryList.size() + " categories");
                    } else {
                        Log.d(TAG, "No categories found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading categories", e));
    }

    private void loadFeaturedProducts() {
        productRepository.getFeaturedProducts(10)
                .addOnSuccessListener(productList -> {
                    if (productList != null && !productList.isEmpty()) {
                        featuredProducts.clear();
                        featuredProducts.addAll(productList);
                        productAdapter.updateList(productList);
                        Log.d(TAG, "Loaded " + productList.size() + " featured products");
                    } else {
                        Log.d(TAG, "No featured products found, trying to load all products");
                        loadAllProductsFallback();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading featured products", e);
                    loadAllProductsFallback();
                });
    }

    private void loadAllProductsFallback() {
        productRepository.getAllProducts()
                .addOnSuccessListener(productList -> {
                    if (productList != null && !productList.isEmpty()) {
                        int limit = Math.min(10, productList.size());
                        List<Product> limitedList = productList.subList(0, limit);
                        featuredProducts.clear();
                        featuredProducts.addAll(limitedList);
                        productAdapter.updateList(limitedList);
                        Log.d(TAG, "Loaded " + limitedList.size() + " products as fallback");
                    } else {
                        Log.d(TAG, "No products found in database");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading all products", e));
    }

    // Reusable RecyclerView item click listener
    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
            void onLongClick(View view, int position);
        }

        public RecyclerItemClickListener(final android.content.Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
