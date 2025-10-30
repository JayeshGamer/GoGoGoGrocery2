package com.grocerygo.models;

import com.google.firebase.firestore.DocumentId;

public class Product {
    @DocumentId
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private String categoryId; // Add categoryId field for filtering
    private String unit; // e.g., "kg", "piece", "liter"
    private int stockQuantity;
    private boolean available;
    private double rating;
    private int reviewCount;

    public Product() {
        // Required empty constructor for Firestore
    }

    public Product(String productId, String name, String description, double price,
                   String imageUrl, String category, String categoryId, String unit, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.categoryId = categoryId;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.available = stockQuantity > 0;
        this.rating = 0.0;
        this.reviewCount = 0;
    }

    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.available = stockQuantity > 0;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
