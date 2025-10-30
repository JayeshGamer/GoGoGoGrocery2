# Firebase Authentication & Firestore Database Implementation

## Overview
This implementation provides a complete Firebase Authentication and Firestore database system for your GoGoGoGrocery app.

## What Has Been Implemented

### 1. Firebase Authentication
- **Email/Password Authentication**: Users can sign up and log in using email and password
- **User Session Management**: Automatic login state persistence
- **Password Reset**: Forgot password functionality via email
- **Logout**: Secure logout functionality

### 2. Firestore Database Structure

#### Collections:
1. **users**: Stores user profile information
   - userId (document ID)
   - email
   - name
   - phone
   - profileImageUrl
   - addresses (array)
   - createdAt (timestamp)
   - updatedAt (timestamp)

2. **products**: Stores grocery products
   - productId (document ID)
   - name
   - description
   - price
   - imageUrl
   - category
   - unit (kg, piece, liter, etc.)
   - stockQuantity
   - available (boolean)
   - rating
   - reviewCount

3. **categories**: Stores product categories
   - categoryId (document ID)
   - name
   - imageUrl
   - productCount

4. **orders**: Stores user orders
   - orderId (document ID)
   - userId
   - items (array of OrderItem)
   - totalAmount
   - deliveryAddress
   - status (pending, confirmed, shipped, delivered, cancelled)
   - paymentMethod
   - isPaid (boolean)
   - orderDate (timestamp)
   - deliveryDate (timestamp)

### 3. Activities Created/Modified

#### New Activities:
- **LoginActivity**: User login screen
- **RegisterActivity**: New user registration screen

#### Modified Activities:
- **SplashActivity**: Now checks if user is logged in and redirects accordingly
- **ProfileActivity**: Displays user data from Firestore and allows logout

### 4. Repository Classes (Data Access Layer)

#### AuthRepository
- `signUpWithEmail()`: Create new user account
- `signInWithEmail()`: Login existing user
- `signOut()`: Logout user
- `getCurrentUser()`: Get current logged-in user
- `sendPasswordResetEmail()`: Send password reset email
- `updateUserProfile()`: Update user profile information
- `getUserData()`: Fetch user data from Firestore

#### ProductRepository
- `getAllProducts()`: Fetch all products
- `getProductsByCategory()`: Fetch products by category
- `getProductById()`: Get single product details
- `searchProducts()`: Search products by name
- `getFeaturedProducts()`: Get top-rated products
- `addProduct()`: Add new product (admin)
- `updateProduct()`: Update product information
- `deleteProduct()`: Remove product

#### OrderRepository
- `createOrder()`: Create a new order
- `getUserOrders()`: Fetch all orders for a user
- `getOrderById()`: Get single order details
- `updateOrderStatus()`: Update order status
- `cancelOrder()`: Cancel an order
- `updatePaymentStatus()`: Update payment status

#### CategoryRepository
- `getAllCategories()`: Fetch all categories
- `getCategoryById()`: Get single category
- `addCategory()`: Add new category
- `updateCategory()`: Update category
- `deleteCategory()`: Remove category

### 5. Data Models
- **User**: User profile model
- **Product**: Product model
- **Order**: Order model with OrderItem nested class
- **Category**: Category model

### 6. Utilities
- **FirebaseManager**: Singleton class for Firebase instances
- **FirebaseDataPopulator**: Utility to populate sample data for testing

## How to Use

### Step 1: Configure Firebase
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project or create a new one
3. Enable **Authentication** → Email/Password sign-in method
4. Enable **Cloud Firestore** database
5. Make sure your `google-services.json` file is in the `app` folder

### Step 2: Set Firestore Rules
In Firebase Console → Firestore Database → Rules, add these security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Products collection (read-only for users)
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null; // Add admin check in production
    }
    
    // Categories collection (read-only for users)
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if request.auth != null; // Add admin check in production
    }
    
    // Orders collection
    match /orders/{orderId} {
      allow read: if request.auth != null && 
                     request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
                       request.auth.uid == request.resource.data.userId;
      allow update: if request.auth != null && 
                       request.auth.uid == resource.data.userId;
    }
  }
}
```

### Step 3: Build and Run
1. Build the project (Build → Make Project)
2. Run the app on your device or emulator
3. You'll see the login screen on first launch

### Step 4: Test the Implementation
1. **Register**: Click "Sign Up" and create a new account
2. **Login**: Login with your credentials
3. **Populate Data**: In Profile → Click "Populate Firebase Data (Debug)" button
4. **Explore**: Navigate through the app to see products and categories
5. **Logout**: Click the logout button in the Profile screen

## App Flow

```
SplashActivity
    ↓
Check if user is logged in?
    ├── No → LoginActivity → RegisterActivity
    └── Yes → HomeActivity
                ↓
    [Home, Categories, Orders, Profile]
                ↓
            ProfileActivity → Logout → LoginActivity
```

## Using the Repositories in Your Activities

### Example: Fetch Products in HomeActivity
```java
ProductRepository productRepository = new ProductRepository();

// Get all products
productRepository.getAllProducts()
    .addOnSuccessListener(products -> {
        // Update your RecyclerView adapter
        adapter.setProducts(products);
    })
    .addOnFailureListener(e -> {
        Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
    });

// Get products by category
productRepository.getProductsByCategory("Fruits & Vegetables")
    .addOnSuccessListener(products -> {
        // Display filtered products
    });
```

### Example: Create Order in CheckoutActivity
```java
OrderRepository orderRepository = new OrderRepository();
String userId = FirebaseManager.getInstance().getCurrentUserId();

// Create order items
List<Order.OrderItem> items = new ArrayList<>();
items.add(new Order.OrderItem("prod_1", "Fresh Apples", 2, 120.0, "url"));

// Create order
Order order = new Order(null, userId, items, 240.0, 
                       "123 Main Street", "Cash on Delivery");

orderRepository.createOrder(order)
    .addOnSuccessListener(orderId -> {
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
        // Navigate to order confirmation
    })
    .addOnFailureListener(e -> {
        Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
    });
```

### Example: Fetch User Orders in OrdersActivity
```java
OrderRepository orderRepository = new OrderRepository();
String userId = FirebaseManager.getInstance().getCurrentUserId();

orderRepository.getUserOrders(userId)
    .addOnSuccessListener(orders -> {
        // Display orders in RecyclerView
        adapter.setOrders(orders);
    })
    .addOnFailureListener(e -> {
        Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
    });
```

## Next Steps

### To Complete Your App Integration:

1. **Update HomeActivity**: 
   - Use ProductRepository to fetch and display products
   - Use CategoryRepository to fetch categories

2. **Update ProductListActivity**:
   - Use ProductRepository.getProductsByCategory() to show filtered products

3. **Update ProductDetailActivity**:
   - Use ProductRepository.getProductById() to show product details

4. **Update CheckoutActivity**:
   - Use OrderRepository.createOrder() to place orders

5. **Update OrdersActivity**:
   - Use OrderRepository.getUserOrders() to display user orders

6. **Update SearchActivity**:
   - Use ProductRepository.searchProducts() to search

## Important Notes

- **Sample Data**: Use the "Populate Firebase Data" button in Profile to add sample products and categories
- **Image URLs**: The sample data uses placeholder URLs. Replace with real image URLs or Firebase Storage URLs
- **Internet Permission**: Already configured in your manifest
- **Offline Support**: Firestore automatically caches data for offline use
- **Error Handling**: All repository methods return Tasks that you should handle with success/failure listeners

## Troubleshooting

### Build Errors
If you see "Cannot resolve symbol" errors:
1. Build → Clean Project
2. Build → Rebuild Project
3. File → Invalidate Caches / Restart

### Authentication Errors
- Check that Email/Password auth is enabled in Firebase Console
- Verify google-services.json is in the correct location

### Firestore Errors
- Check Firestore rules in Firebase Console
- Ensure Firestore is enabled for your project
- Check your internet connection

## Security Considerations

⚠️ **For Production**:
1. Add proper admin authentication for product/category management
2. Implement server-side validation for orders
3. Add data validation rules in Firestore
4. Secure image uploads with Firebase Storage rules
5. Implement proper error logging and crash reporting
6. Add rate limiting for authentication attempts

---

**Created**: October 2025
**Version**: 1.0
**Status**: Ready for Testing

