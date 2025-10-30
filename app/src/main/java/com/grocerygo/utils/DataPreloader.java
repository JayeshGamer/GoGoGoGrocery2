package com.grocerygo.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.firebase.AuthRepository;
import com.grocerygo.firebase.CategoryRepository;
import com.grocerygo.firebase.ProductRepository;
import com.grocerygo.models.Address;
import com.grocerygo.models.Category;
import com.grocerygo.models.Product;
import com.grocerygo.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton class to preload all critical app data during splash screen
 * This ensures smooth user experience with no visible placeholders
 */
public class DataPreloader {
    private static final String TAG = "DataPreloader";
    private static DataPreloader instance;

    // Cached data
    private User currentUser;
    private Address defaultAddress;
    private List<Category> categories;
    private List<Product> featuredProducts;
    private boolean isDataLoaded = false;

    // Repositories
    private final AuthRepository authRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FirebaseFirestore db;

    // Loading state
    private boolean isLoading = false;

    private DataPreloader() {
        authRepository = new AuthRepository();
        categoryRepository = new CategoryRepository();
        productRepository = new ProductRepository();
        db = FirebaseFirestore.getInstance();

        categories = new ArrayList<>();
        featuredProducts = new ArrayList<>();
    }

    public static synchronized DataPreloader getInstance() {
        if (instance == null) {
            instance = new DataPreloader();
        }
        return instance;
    }

    /**
     * Preload all critical data in parallel for optimal performance
     */
    public Task<Void> preloadAllData() {
        if (isLoading) {
            Log.d(TAG, "Already loading data, skipping...");
            return Tasks.forResult(null);
        }

        if (isDataLoaded) {
            Log.d(TAG, "Data already loaded, returning cached data");
            return Tasks.forResult(null);
        }

        isLoading = true;
        Log.d(TAG, "Starting data preload...");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "No user logged in, skipping preload");
            isLoading = false;
            return Tasks.forResult(null);
        }

        // Create tasks for parallel loading
        List<Task<?>> tasks = new ArrayList<>();

        // Task 1: Load user data
        Task<User> userTask = loadUserData(firebaseUser.getUid());
        tasks.add(userTask);

        // Task 2: Load default address
        Task<Address> addressTask = loadDefaultAddress(firebaseUser.getUid());
        tasks.add(addressTask);

        // Task 3: Load categories
        Task<List<Category>> categoriesTask = loadCategories();
        tasks.add(categoriesTask);

        // Task 4: Load featured products
        Task<List<Product>> productsTask = loadFeaturedProducts();
        tasks.add(productsTask);

        // Wait for all tasks to complete
        return Tasks.whenAllComplete(tasks)
            .continueWith(task -> {
                isLoading = false;
                isDataLoaded = true;

                // Log results
                logPreloadResults();

                return null;
            });
    }

    private Task<User> loadUserData(String userId) {
        return authRepository.getUserData(userId)
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentUser = task.getResult();
                    Log.d(TAG, "User data loaded: " + currentUser.getName());
                } else {
                    Log.w(TAG, "Failed to load user data");
                    currentUser = null;
                }
                return currentUser;
            });
    }

    private Task<Address> loadDefaultAddress(String userId) {
        return db.collection("addresses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .limit(1)
            .get()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    defaultAddress = task.getResult().getDocuments().get(0).toObject(Address.class);
                    Log.d(TAG, "Default address loaded: " + defaultAddress.getCity());
                } else {
                    Log.w(TAG, "No default address found");
                    defaultAddress = null;
                }
                return defaultAddress;
            });
    }

    private Task<List<Category>> loadCategories() {
        return categoryRepository.getAllCategories()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    categories.clear();
                    categories.addAll(task.getResult());
                    Log.d(TAG, "Categories loaded: " + categories.size());
                } else {
                    Log.w(TAG, "Failed to load categories");
                    categories.clear();
                }
                return categories;
            });
    }

    private Task<List<Product>> loadFeaturedProducts() {
        return productRepository.getFeaturedProducts(10)
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    featuredProducts.clear();
                    featuredProducts.addAll(task.getResult());
                    Log.d(TAG, "Featured products loaded: " + featuredProducts.size());
                } else {
                    // Fallback to all products
                    Log.d(TAG, "No featured products, loading all products...");
                    return loadAllProductsFallback();
                }
                return featuredProducts;
            })
            .continueWithTask(task -> {
                if (task.getResult() != null && task.getResult() instanceof Task) {
                    return (Task<List<Product>>) task.getResult();
                }
                return Tasks.forResult(featuredProducts);
            });
    }

    private Task<List<Product>> loadAllProductsFallback() {
        return productRepository.getAllProducts()
            .continueWith(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    int limit = Math.min(10, task.getResult().size());
                    featuredProducts.clear();
                    featuredProducts.addAll(task.getResult().subList(0, limit));
                    Log.d(TAG, "All products loaded (limited): " + featuredProducts.size());
                } else {
                    Log.w(TAG, "Failed to load products");
                    featuredProducts.clear();
                }
                return featuredProducts;
            });
    }

    private void logPreloadResults() {
        Log.i(TAG, "=== Data Preload Complete ===");
        Log.i(TAG, "User: " + (currentUser != null ? currentUser.getName() : "Not loaded"));
        Log.i(TAG, "Address: " + (defaultAddress != null ? defaultAddress.getCity() : "Not loaded"));
        Log.i(TAG, "Categories: " + categories.size());
        Log.i(TAG, "Products: " + featuredProducts.size());
        Log.i(TAG, "============================");
    }

    // Getters for cached data
    public User getCurrentUser() {
        return currentUser;
    }

    public Address getDefaultAddress() {
        return defaultAddress;
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    public List<Product> getFeaturedProducts() {
        return new ArrayList<>(featuredProducts);
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    /**
     * Clear cached data (call on logout)
     */
    public void clearCache() {
        currentUser = null;
        defaultAddress = null;
        categories.clear();
        featuredProducts.clear();
        isDataLoaded = false;
        isLoading = false;
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Refresh specific data without full reload
     */
    public Task<User> refreshUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return loadUserData(firebaseUser.getUid());
        }
        return Tasks.forResult(null);
    }

    public Task<Address> refreshAddress() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return loadDefaultAddress(firebaseUser.getUid());
        }
        return Tasks.forResult(null);
    }
}

