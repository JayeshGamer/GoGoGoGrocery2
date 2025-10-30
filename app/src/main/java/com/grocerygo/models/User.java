package com.grocerygo.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class User {
    @DocumentId
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String dateOfBirth;
    private String profileImageUrl;
    private String role; // "admin", "customer", "delivery"
    private List<String> addresses;
    private List<String> wishlist; // Product IDs
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    public User() {
        // Required empty constructor for Firestore
        this.addresses = new ArrayList<>();
        this.wishlist = new ArrayList<>();
        this.role = "customer"; // Default role
    }

    public User(String userId, String email, String name, String phone) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.addresses = new ArrayList<>();
        this.wishlist = new ArrayList<>();
        this.role = "customer"; // Default role
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getAddresses() { return addresses; }
    public void setAddresses(List<String> addresses) { this.addresses = addresses; }

    public List<String> getWishlist() { return wishlist; }
    public void setWishlist(List<String> wishlist) { this.wishlist = wishlist; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper method to check if user is admin
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    // Helper method to check if user is delivery partner
    public boolean isDeliveryPartner() {
        return "delivery".equalsIgnoreCase(role);
    }
}
