# Database Population Guide

## Problem Fixed

**Issue:** "No products available" redirection error in the category section.

**Root Cause:** Products were being stored with a `category` field (e.g., "Fruits & Vegetables") but queries were searching by `categoryId` (e.g., "cat_fruits_vegetables"), causing a mismatch that resulted in no products being found.

## Solution Implemented

### 1. **Updated Product Model**
   - Added `categoryId` field to the Product class
   - Now stores both category name and category ID for proper filtering

### 2. **Fixed FirebaseDataPopulator**
   - Updated to include `categoryId` when creating products
   - Properly maps category names to their IDs
   - Creates 12 categories and 70+ products with proper relationships

### 3. **Improved ProductListActivity**
   - Better handling of empty product results
   - No longer redirects infinitely when no products are found
   - Shows appropriate empty state message

### 4. **Created Database Populator Tool**
   - New activity to easily populate the database
   - User-friendly interface with progress indicator
   - Automated batch script for quick access

---

## How to Populate the Database

### Method 1: Using the Batch Script (Easiest)

1. Make sure your app is installed on your device/emulator
2. Run the batch script:
   ```
   populate_database.bat
   ```
3. The Database Populator screen will open automatically
4. Click the "Populate Database" button
5. Wait for completion (shows success message)

### Method 2: Using ADB Command

1. Install the app on your device/emulator
2. Open a terminal and run:
   ```
   adb shell am start -n com.grocerygo/.DatabasePopulatorActivity
   ```
3. Click "Populate Database" button in the app

### Method 3: Add a Menu Option (Manual)

You can add a button in your app (e.g., in HomeActivity or ProfileActivity) to launch the Database Populator:

```java
Button btnPopulateDB = findViewById(R.id.btnPopulateDB);
btnPopulateDB.setOnClickListener(v -> {
    startActivity(new Intent(this, DatabasePopulatorActivity.class));
});
```

---

## What Gets Populated

### Categories (12 total)
- Fruits & Vegetables (12 products)
- Dairy & Eggs (7 products)
- Bakery & Bread (5 products)
- Beverages (7 products)
- Snacks & Sweets (6 products)
- Meat & Seafood (5 products)
- Frozen Foods (4 products)
- Grains & Pulses (6 products)
- Spices & Seasonings (6 products)
- Cooking Oils (4 products)
- Personal Care (4 products)
- Household Items (4 products)

### Products (70+ total)
Each product includes:
- Product ID
- Name and description
- Price
- Image URL (from Unsplash)
- Category name and ID
- Unit (kg, liter, piece, etc.)
- Stock quantity
- Rating (4.0 - 4.8)
- Review count

---

## Verification Steps

After populating the database:

1. **Check Categories Screen**
   - Navigate to Categories
   - You should see 12 categories with product counts

2. **Check Products by Category**
   - Click on any category
   - Products for that category should load without errors
   - No infinite redirection

3. **Check Firebase Console**
   - Go to Firebase Console → Firestore Database
   - Verify `categories` collection has 12 documents
   - Verify `products` collection has 70+ documents
   - Check that products have both `category` and `categoryId` fields

---

## Building the App

To rebuild the app with these changes:

```bash
# Clean build
gradlew clean

# Build and install
gradlew installDebug

# Or use the combined script
build_and_debug.bat
```

---

## Troubleshooting

### Issue: "Cannot resolve symbol 'activity_database_populator'"
**Solution:** Clean and rebuild the project:
```bash
gradlew clean build
```

### Issue: Activity doesn't launch
**Solution:** 
1. Verify the app is installed: `adb shell pm list packages | findstr grocerygo`
2. Check logcat for errors: `adb logcat | findstr DatabasePopulator`

### Issue: Products still not showing
**Solution:**
1. Check Firebase connection (ensure google-services.json is correct)
2. Verify Firebase rules allow read/write
3. Check internet connection on device/emulator

### Issue: Duplicate data after running multiple times
**Solution:** The populator uses document IDs, so running it multiple times will update existing data, not duplicate it.

---

## Firebase Rules

Make sure your Firestore rules allow reading and writing:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /categories/{document=**} {
      allow read, write: if true;
    }
    match /products/{document=**} {
      allow read, write: if true;
    }
  }
}
```

---

## Files Modified/Created

### Modified:
- `app/src/main/java/com/grocerygo/models/Product.java` - Added categoryId field
- `app/src/main/java/com/grocerygo/utils/FirebaseDataPopulator.java` - Updated to use categoryId
- `app/src/main/java/com/grocerygo/ProductListActivity.java` - Better empty state handling
- `app/src/main/AndroidManifest.xml` - Added DatabasePopulatorActivity

### Created:
- `app/src/main/java/com/grocerygo/DatabasePopulatorActivity.java` - New activity
- `app/src/main/res/layout/activity_database_populator.xml` - UI layout
- `populate_database.bat` - Batch script for easy access
- `DATABASE_POPULATION_GUIDE.md` - This file

---

## Next Steps

1. Run the batch script or install the app
2. Populate the database using the DatabasePopulatorActivity
3. Test browsing categories and products
4. Verify no redirection errors occur
5. Enjoy a fully functional category/product browsing experience!

---

## Support

If you encounter any issues:
1. Check the logcat output for errors
2. Verify Firebase configuration
3. Ensure proper internet connectivity
4. Check that all files were properly updated

