package com.grocerygo.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private static final String TAG = "CategoryRepository";
    private final FirebaseFirestore db;
    private static final String COLLECTION_CATEGORIES = "categories";

    public CategoryRepository() {
        this.db = FirebaseManager.getInstance().getDb();
    }

    // Get all categories
    public Task<List<Category>> getAllCategories() {
        return db.collection(COLLECTION_CATEGORIES)
                .get()
                .continueWith(task -> {
                    List<Category> categories = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        categories = task.getResult().toObjects(Category.class);
                        Log.d(TAG, "Categories fetched: " + categories.size());
                    } else {
                        Log.e(TAG, "Error getting categories", task.getException());
                    }
                    return categories;
                });
    }

    // Get category by ID
    public Task<Category> getCategoryById(String categoryId) {
        return db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Category.class);
                    }
                    return null;
                });
    }

    // Add a new category
    public Task<Void> addCategory(Category category) {
        String categoryId = db.collection(COLLECTION_CATEGORIES).document().getId();
        category.setCategoryId(categoryId);
        return db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .set(category);
    }

    // Update category
    public Task<Void> updateCategory(Category category) {
        return db.collection(COLLECTION_CATEGORIES)
                .document(category.getCategoryId())
                .set(category);
    }

    // Delete category
    public Task<Void> deleteCategory(String categoryId) {
        return db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .delete();
    }
}

