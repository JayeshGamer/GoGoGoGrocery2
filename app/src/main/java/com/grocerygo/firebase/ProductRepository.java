package com.grocerygo.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.grocerygo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private static final String TAG = "ProductRepository";
    private final FirebaseFirestore db;
    private static final String COLLECTION_PRODUCTS = "products";

    public ProductRepository() {
        this.db = FirebaseManager.getInstance().getDb();
    }

    // Get all products
    public Task<List<Product>> getAllProducts() {
        return db.collection(COLLECTION_PRODUCTS)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        products = task.getResult().toObjects(Product.class);
                        Log.d(TAG, "Products fetched: " + products.size());
                    } else {
                        Log.e(TAG, "Error getting products", task.getException());
                    }
                    return products;
                });
    }

    // Get products by category
    public Task<List<Product>> getProductsByCategory(String category) {
        return db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("category", category)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        products = task.getResult().toObjects(Product.class);
                    }
                    return products;
                });
    }

    // Get products by category ID
    public Task<List<Product>> getProductsByCategoryId(String categoryId) {
        return db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("categoryId", categoryId)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        products = task.getResult().toObjects(Product.class);
                        Log.d(TAG, "Products fetched for category " + categoryId + ": " + products.size());
                    } else {
                        Log.e(TAG, "Error getting products by category ID", task.getException());
                    }
                    return products;
                });
    }

    // Get product by ID
    public Task<Product> getProductById(String productId) {
        return db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Product.class);
                    }
                    return null;
                });
    }

    // Search products by name
    public Task<List<Product>> searchProducts(String query) {
        String searchEnd = query + "\uf8ff";
        return db.collection(COLLECTION_PRODUCTS)
                .orderBy("name")
                .startAt(query)
                .endAt(searchEnd)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        products = task.getResult().toObjects(Product.class);
                    }
                    return products;
                });
    }

    // Get featured/popular products
    public Task<List<Product>> getFeaturedProducts(int limit) {
        return db.collection(COLLECTION_PRODUCTS)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Get all products and filter available ones
                        List<Product> allProducts = task.getResult().toObjects(Product.class);
                        Log.d(TAG, "Total products fetched: " + allProducts.size());

                        // Filter available products and sort by rating
                        for (Product product : allProducts) {
                            if (product.isAvailable()) {
                                products.add(product);
                            }
                        }

                        // Sort by rating in descending order
                        products.sort((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));

                        // Limit results
                        if (products.size() > limit) {
                            products = products.subList(0, limit);
                        }

                        Log.d(TAG, "Featured products after filtering: " + products.size());
                    } else {
                        Log.e(TAG, "Error getting featured products", task.getException());
                    }
                    return products;
                });
    }

    // Get available products (simpler query without ordering)
    public Task<List<Product>> getAvailableProducts(int limit) {
        return db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("available", true)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    List<Product> products = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        products = task.getResult().toObjects(Product.class);
                        Log.d(TAG, "Available products fetched: " + products.size());
                    } else {
                        Log.e(TAG, "Error getting available products", task.getException());
                    }
                    return products;
                });
    }

    // Add a new product (admin function)
    public Task<Void> addProduct(Product product) {
        String productId = db.collection(COLLECTION_PRODUCTS).document().getId();
        product.setProductId(productId);
        return db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .set(product);
    }

    // Update product
    public Task<Void> updateProduct(Product product) {
        return db.collection(COLLECTION_PRODUCTS)
                .document(product.getProductId())
                .set(product);
    }

    // Delete product
    public Task<Void> deleteProduct(String productId) {
        return db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .delete();
    }
}
