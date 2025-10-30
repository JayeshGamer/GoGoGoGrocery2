package com.grocerygo.models;

import com.grocerygo.utils.CartManager;

public class CartItem {
    private String productId;
    private String productName;
    private String productImage;
    private double productPrice;
    private String productUnit;
    private int quantity;
    private double totalPrice;

    public CartItem() {
        // Required empty constructor for Firebase
    }

    public CartItem(String productId, String productName, String productImage,
                    double productPrice, String productUnit, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.productUnit = productUnit;
        this.quantity = quantity;
        this.totalPrice = productPrice * quantity;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
        updateTotalPrice();
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    private void updateTotalPrice() {
        this.totalPrice = this.productPrice * this.quantity;
    }

    public void incrementQuantity() {
        if (this.quantity < CartManager.MAX_QUANTITY) {
            this.quantity++;
            updateTotalPrice();
        }
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            updateTotalPrice();
        }
    }
}
