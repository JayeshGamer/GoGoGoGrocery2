package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.adapters.CategoryGridAdapter;
import com.grocerygo.app.R;
import com.grocerygo.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryGridAdapter categoryAdapter;
    private List<Category> categoryList;
    private List<Category> filteredCategoryList;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private ImageView btnBack;
    private EditText etSearch;
    private BottomNavigationView bottomNavigation;

    private FirebaseFirestore db;
    private int totalProducts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearch();
        loadCategories();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        db = FirebaseFirestore.getInstance();
        categoryList = new ArrayList<>();
        filteredCategoryList = new ArrayList<>();
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryGridAdapter(this, filteredCategoryList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvCategories.setLayoutManager(gridLayoutManager);
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setNestedScrollingEnabled(false);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCategories(String query) {
        filteredCategoryList.clear();

        if (query.isEmpty()) {
            filteredCategoryList.addAll(categoryList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Category category : categoryList) {
                if (category.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredCategoryList.add(category);
                }
            }
        }

        categoryAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_categories);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadCategories() {
        showLoading(true);

        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        categoryList.add(category);
                    }

                    // Update stats
                    // Load product counts for each category
                    loadProductCounts();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load categories: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void loadProductCounts() {
        if (categoryList.isEmpty()) {
            showLoading(false);
            updateEmptyState();
            return;
        }

        int[] loadedCount = {0};
        totalProducts = 0;

        for (Category category : categoryList) {
            db.collection("products")
                    .whereEqualTo("categoryId", category.getCategoryId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int productCount = queryDocumentSnapshots.size();
                        category.setProductCount(productCount);
                        totalProducts += productCount;
                        loadedCount[0]++;

                        if (loadedCount[0] == categoryList.size()) {
                            filteredCategoryList.clear();
                            filteredCategoryList.addAll(categoryList);
                            categoryAdapter.notifyDataSetChanged();

                            // Update total products stat
                            // tvTotalProducts.setText(totalProducts + "+");

                            showLoading(false);
                            updateEmptyState();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        if (loadedCount[0] == categoryList.size()) {
                            filteredCategoryList.clear();
                            filteredCategoryList.addAll(categoryList);
                            categoryAdapter.notifyDataSetChanged();
                            showLoading(false);
                            updateEmptyState();
                        }
                    });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCategories.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (filteredCategoryList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_categories);
    }
}
