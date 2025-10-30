package com.grocerygo.models;

import com.google.firebase.firestore.DocumentId;

public class Category {
    @DocumentId
    private String categoryId;
    private String name;
    private String imageUrl;
    private int productCount;

    public Category() {
        // Required empty constructor for Firestore
    }

    public Category(String categoryId, String name, String imageUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.productCount = 0;
    }

    // Getters and setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
}

