package com.grocerygo.models;

import com.google.firebase.firestore.DocumentId;

public class Address {
    @DocumentId
    private String addressId;
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String addressType; // Home, Work, Other
    private boolean isDefault;

    public Address() {
        // Required empty constructor for Firestore
    }

    public Address(String userId, String fullName, String phoneNumber, String addressLine1,
                   String addressLine2, String city, String state, String pincode, String addressType) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.addressType = addressType;
        this.isDefault = false;
    }

    // Getters and setters
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    // Build a safe, human readable address string without causing NPEs when fields are null
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        boolean added = false;

        if (addressLine1 != null && !addressLine1.isEmpty()) {
            sb.append(addressLine1);
            added = true;
        }
        if (addressLine2 != null && !addressLine2.isEmpty()) {
            if (added) sb.append(", ");
            sb.append(addressLine2);
            added = true;
        }
        if (city != null && !city.isEmpty()) {
            if (added) sb.append(", ");
            sb.append(city);
            added = true;
        }
        if (state != null && !state.isEmpty()) {
            if (added) sb.append(", ");
            sb.append(state);
            added = true;
        }
        if (pincode != null && !pincode.isEmpty()) {
            if (added) sb.append(" - ");
            sb.append(pincode);
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? "" : result;
    }
}
