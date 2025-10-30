package com.grocerygo.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Order {
    @DocumentId
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private String deliveryAddress;
    private String status; // "pending", "confirmed", "shipped", "delivered", "cancelled"
    private String paymentMethod;
    private boolean isPaid;
    private String assignedDeliveryPartner; // User ID of delivery partner
    private String deliveryPartnerName; // Name of delivery partner
    private boolean confirmed; // Admin confirmation
    @ServerTimestamp
    private Date orderDate;
    private Date deliveryDate;

    public Order() {
        // Required empty constructor for Firestore
    }

    public Order(String orderId, String userId, List<OrderItem> items, double totalAmount,
                 String deliveryAddress, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.status = "pending";
        this.isPaid = false;
        this.confirmed = false;
        this.assignedDeliveryPartner = null;
        this.deliveryPartnerName = null;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getAssignedDeliveryPartner() { return assignedDeliveryPartner; }
    public void setAssignedDeliveryPartner(String assignedDeliveryPartner) {
        this.assignedDeliveryPartner = assignedDeliveryPartner;
    }

    public String getDeliveryPartnerName() { return deliveryPartnerName; }
    public void setDeliveryPartnerName(String deliveryPartnerName) {
        this.deliveryPartnerName = deliveryPartnerName;
    }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }

    // Helper method to get item count
    public int getItemCount() {
        if (items == null) return 0;
        int count = 0;
        for (OrderItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private double price;
        private String imageUrl;

        public OrderItem() {}

        public OrderItem(String productId, String productName, int quantity, double price, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}
