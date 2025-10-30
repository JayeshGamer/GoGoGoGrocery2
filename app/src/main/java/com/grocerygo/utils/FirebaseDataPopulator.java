package com.grocerygo.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.models.Category;
import com.grocerygo.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDataPopulator {
    private static final String TAG = "FirebaseDataPopulator";
    private final FirebaseFirestore db;

    public FirebaseDataPopulator() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void populateSampleData(OnDataPopulatedListener listener) {
        // First populate categories
        populateCategories(() -> {
            // Then populate products
            populateProducts(() -> {
                if (listener != null) {
                    listener.onSuccess();
                }
            });
        });
    }

    private void populateCategories(Runnable onComplete) {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category("cat_fruits_vegetables", "Fruits & Vegetables", "https://images.unsplash.com/photo-1610348725531-843dff563e2c?w=400"));
        categories.add(new Category("cat_dairy_eggs", "Dairy & Eggs", "https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=400"));
        categories.add(new Category("cat_bakery", "Bakery & Bread", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400"));
        categories.add(new Category("cat_beverages", "Beverages", "https://images.unsplash.com/photo-1437418747212-8d9709afab22?w=400"));
        categories.add(new Category("cat_snacks", "Snacks & Sweets", "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=400"));
        categories.add(new Category("cat_meat_seafood", "Meat & Seafood", "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400"));
        categories.add(new Category("cat_frozen", "Frozen Foods", "https://images.unsplash.com/photo-1628776084818-70e5b4359d8b?w=400"));
        categories.add(new Category("cat_grains_pulses", "Grains & Pulses", "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400"));
        categories.add(new Category("cat_spices", "Spices & Seasonings", "https://images.unsplash.com/photo-1596040033229-a0b55e07bdf4?w=400"));
        categories.add(new Category("cat_cooking_oils", "Cooking Oils", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400"));
        categories.add(new Category("cat_personal_care", "Personal Care", "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400"));
        categories.add(new Category("cat_household", "Household Items", "https://images.unsplash.com/photo-1610557892470-55d9e80c0bce?w=400"));

        int[] counter = {0};
        for (Category category : categories) {
            db.collection("categories")
                    .document(category.getCategoryId())
                    .set(category)
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        Log.d(TAG, "Category added: " + category.getName());
                        if (counter[0] == categories.size() && onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding category", e));
        }
    }

    private void populateProducts(Runnable onComplete) {
        List<Product> products = new ArrayList<>();

        // Fruits & Vegetables
        products.add(createProduct("prod_apple", "Fresh Red Apples", "Crisp and sweet red apples", 120.0,
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 100, 4.5, 89));
        products.add(createProduct("prod_banana", "Bananas", "Fresh yellow bananas", 50.0,
                "https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "dozen", 150, 4.7, 112));
        products.add(createProduct("prod_tomato", "Tomatoes", "Fresh red tomatoes", 40.0,
                "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 80, 4.3, 67));
        products.add(createProduct("prod_potato", "Potatoes", "Farm fresh potatoes", 30.0,
                "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 200, 4.2, 134));
        products.add(createProduct("prod_onion", "Onions", "Fresh red onions", 35.0,
                "https://images.unsplash.com/photo-1508747703725-719777637510?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 150, 4.1, 98));
        products.add(createProduct("prod_carrot", "Carrots", "Organic carrots", 45.0,
                "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 90, 4.4, 76));
        products.add(createProduct("prod_cucumber", "Cucumber", "Fresh green cucumber", 25.0,
                "https://images.unsplash.com/photo-1604977042946-1eecc30f269e?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 70, 4.0, 54));
        products.add(createProduct("prod_spinach", "Spinach", "Fresh green spinach leaves", 30.0,
                "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "bunch", 60, 4.3, 43));
        products.add(createProduct("prod_orange", "Oranges", "Sweet juicy oranges", 80.0,
                "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 110, 4.6, 92));
        products.add(createProduct("prod_mango", "Mangoes", "Fresh sweet mangoes", 150.0,
                "https://images.unsplash.com/photo-1605027990121-cbae9d3ce6ba?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 85, 4.8, 156));
        products.add(createProduct("prod_grapes", "Green Grapes", "Seedless green grapes", 100.0,
                "https://images.unsplash.com/photo-1599819177680-b6f9c3e8b1d0?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 75, 4.5, 68));
        products.add(createProduct("prod_capsicum", "Bell Pepper", "Fresh bell peppers", 60.0,
                "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400", "Fruits & Vegetables", "cat_fruits_vegetables", "kg", 65, 4.2, 45));

        // Dairy & Eggs
        products.add(createProduct("prod_milk", "Fresh Milk", "Full cream pasteurized milk", 65.0,
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", "Dairy & Eggs", "cat_dairy_eggs", "liter", 120, 4.6, 203));
        products.add(createProduct("prod_eggs", "Farm Eggs", "Fresh brown eggs", 90.0,
                "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400", "Dairy & Eggs", "cat_dairy_eggs", "dozen", 100, 4.7, 187));
        products.add(createProduct("prod_butter", "Butter", "Unsalted butter", 220.0,
                "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=400", "Dairy & Eggs", "cat_dairy_eggs", "500g", 80, 4.5, 142));
        products.add(createProduct("prod_yogurt", "Plain Yogurt", "Fresh plain yogurt", 60.0,
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", "Dairy & Eggs", "cat_dairy_eggs", "500g", 95, 4.4, 118));
        products.add(createProduct("prod_cheese", "Cheddar Cheese", "Aged cheddar cheese", 350.0,
                "https://images.unsplash.com/photo-1552767059-ce182ead6c1b?w=400", "Dairy & Eggs", "cat_dairy_eggs", "250g", 60, 4.6, 89));
        products.add(createProduct("prod_paneer", "Fresh Paneer", "Homemade style paneer", 120.0,
                "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=400", "Dairy & Eggs", "cat_dairy_eggs", "250g", 70, 4.5, 95));
        products.add(createProduct("prod_cream", "Fresh Cream", "Cooking cream", 80.0,
                "https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=400", "Dairy & Eggs", "cat_dairy_eggs", "200ml", 55, 4.3, 67));

        // Bakery & Bread
        products.add(createProduct("prod_white_bread", "White Bread", "Fresh white bread loaf", 40.0,
                "https://images.unsplash.com/photo-1586444248902-2f64eddc13df?w=400", "Bakery & Bread", "cat_bakery", "loaf", 50, 4.2, 134));
        products.add(createProduct("prod_wheat_bread", "Whole Wheat Bread", "Healthy whole wheat bread", 50.0,
                "https://images.unsplash.com/photo-1598373182133-52452f7691ef?w=400", "Bakery & Bread", "cat_bakery", "loaf", 45, 4.4, 156));
        products.add(createProduct("prod_croissant", "Butter Croissants", "Flaky butter croissants", 150.0,
                "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400", "Bakery & Bread", "cat_bakery", "pack of 6", 30, 4.7, 98));
        products.add(createProduct("prod_baguette", "French Baguette", "Crispy French baguette", 60.0,
                "https://images.unsplash.com/photo-1534620808146-d33bb39128b2?w=400", "Bakery & Bread", "cat_bakery", "piece", 35, 4.5, 76));
        products.add(createProduct("prod_cake", "Chocolate Cake", "Delicious chocolate cake", 450.0,
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400", "Bakery & Bread", "cat_bakery", "500g", 25, 4.8, 145));

        // Beverages
        products.add(createProduct("prod_orange_juice", "Orange Juice", "Fresh squeezed orange juice", 90.0,
                "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400", "Beverages", "cat_beverages", "liter", 80, 4.5, 112));
        products.add(createProduct("prod_apple_juice", "Apple Juice", "100% apple juice", 95.0,
                "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400", "Beverages", "cat_beverages", "liter", 75, 4.4, 98));
        products.add(createProduct("prod_cola", "Cola", "Carbonated soft drink", 45.0,
                "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400", "Beverages", "cat_beverages", "1L bottle", 150, 4.1, 234));
        products.add(createProduct("prod_green_tea", "Green Tea", "Organic green tea bags", 180.0,
                "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400", "Beverages", "cat_beverages", "100 bags", 90, 4.6, 167));
        products.add(createProduct("prod_coffee", "Instant Coffee", "Premium instant coffee", 250.0,
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e?w=400", "Beverages", "cat_beverages", "200g jar", 70, 4.7, 189));
        products.add(createProduct("prod_water", "Mineral Water", "Natural mineral water", 20.0,
                "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400", "Beverages", "cat_beverages", "1L bottle", 200, 4.3, 312));
        products.add(createProduct("prod_mango_juice", "Mango Juice", "Fresh mango juice", 100.0,
                "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400", "Beverages", "cat_beverages", "liter", 65, 4.6, 134));

        // Snacks & Sweets
        products.add(createProduct("prod_chips", "Potato Chips", "Classic salted chips", 30.0,
                "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400", "Snacks & Sweets", "cat_snacks", "150g pack", 120, 4.2, 267));
        products.add(createProduct("prod_chocolate", "Milk Chocolate", "Creamy milk chocolate bar", 60.0,
                "https://images.unsplash.com/photo-1511381939415-e44015466834?w=400", "Snacks & Sweets", "cat_snacks", "100g", 100, 4.5, 198));
        products.add(createProduct("prod_cookies", "Butter Cookies", "Crunchy butter cookies", 50.0,
                "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400", "Snacks & Sweets", "cat_snacks", "200g pack", 85, 4.4, 156));
        products.add(createProduct("prod_biscuits", "Cream Biscuits", "Vanilla cream biscuits", 40.0,
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400", "Snacks & Sweets", "cat_snacks", "300g pack", 110, 4.3, 234));
        products.add(createProduct("prod_nuts", "Mixed Nuts", "Roasted mixed nuts", 180.0,
                "https://images.unsplash.com/photo-1508736793122-f516e3ba5569?w=400", "Snacks & Sweets", "cat_snacks", "250g", 70, 4.7, 145));
        products.add(createProduct("prod_popcorn", "Popcorn", "Butter popcorn", 35.0,
                "https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=400", "Snacks & Sweets", "cat_snacks", "100g pack", 90, 4.1, 178));

        // Meat & Seafood
        products.add(createProduct("prod_chicken", "Fresh Chicken", "Antibiotic-free chicken", 180.0,
                "https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=400", "Meat & Seafood", "cat_meat_seafood", "kg", 60, 4.4, 98));
        products.add(createProduct("prod_mutton", "Mutton", "Fresh mutton", 550.0,
                "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400", "Meat & Seafood", "cat_meat_seafood", "kg", 40, 4.5, 76));
        products.add(createProduct("prod_fish", "Fresh Fish", "River fish", 320.0,
                "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400", "Meat & Seafood", "cat_meat_seafood", "kg", 45, 4.3, 89));
        products.add(createProduct("prod_prawns", "Prawns", "Fresh prawns", 450.0,
                "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=400", "Meat & Seafood", "cat_meat_seafood", "kg", 35, 4.6, 67));
        products.add(createProduct("prod_eggs_white", "Egg Whites", "Liquid egg whites", 75.0,
                "https://images.unsplash.com/photo-1587486937820-4365958a69c5?w=400", "Meat & Seafood", "cat_meat_seafood", "250ml", 50, 4.2, 54));

        // Frozen Foods
        products.add(createProduct("prod_ice_cream", "Vanilla Ice Cream", "Premium vanilla ice cream", 180.0,
                "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=400", "Frozen Foods", "cat_frozen", "500ml", 70, 4.7, 189));
        products.add(createProduct("prod_frozen_peas", "Frozen Peas", "Quick frozen green peas", 60.0,
                "https://images.unsplash.com/photo-1588739190547-7d5ec36e8d50?w=400", "Frozen Foods", "cat_frozen", "500g", 85, 4.3, 112));
        products.add(createProduct("prod_frozen_pizza", "Frozen Pizza", "Margherita pizza", 220.0,
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400", "Frozen Foods", "cat_frozen", "piece", 55, 4.4, 134));
        products.add(createProduct("prod_frozen_fries", "French Fries", "Crispy frozen fries", 90.0,
                "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400", "Frozen Foods", "cat_frozen", "500g", 95, 4.2, 167));

        // Grains & Pulses
        products.add(createProduct("prod_rice", "Basmati Rice", "Premium basmati rice", 85.0,
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400", "Grains & Pulses", "cat_grains_pulses", "kg", 150, 4.6, 234));
        products.add(createProduct("prod_wheat_flour", "Wheat Flour", "Whole wheat flour", 45.0,
                "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400", "Grains & Pulses", "cat_grains_pulses", "kg", 180, 4.4, 198));
        products.add(createProduct("prod_lentils", "Red Lentils", "Organic red lentils", 120.0,
                "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=400", "Grains & Pulses", "cat_grains_pulses", "kg", 100, 4.5, 145));
        products.add(createProduct("prod_chickpeas", "Chickpeas", "Dried chickpeas", 100.0,
                "https://images.unsplash.com/photo-1610988934047-d4c6e0e62f96?w=400", "Grains & Pulses", "cat_grains_pulses", "kg", 90, 4.3, 123));
        products.add(createProduct("prod_oats", "Rolled Oats", "Quick cooking oats", 140.0,
                "https://images.unsplash.com/photo-1574672280600-4accfa5b6f98?w=400", "Grains & Pulses", "cat_grains_pulses", "500g", 110, 4.7, 176));
        products.add(createProduct("prod_pasta", "Pasta", "Italian pasta", 75.0,
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400", "Grains & Pulses", "cat_grains_pulses", "500g", 130, 4.4, 189));

        // Spices & Seasonings
        products.add(createProduct("prod_turmeric", "Turmeric Powder", "Pure turmeric powder", 80.0,
                "https://images.unsplash.com/photo-1596040033229-a0b55e07bdf4?w=400", "Spices & Seasonings", "cat_spices", "100g", 120, 4.5, 156));
        products.add(createProduct("prod_chili", "Red Chili Powder", "Spicy red chili powder", 70.0,
                "https://images.unsplash.com/photo-1583997052301-0042b33fc598?w=400", "Spices & Seasonings", "cat_spices", "100g", 115, 4.4, 134));
        products.add(createProduct("prod_cumin", "Cumin Seeds", "Aromatic cumin seeds", 90.0,
                "https://images.unsplash.com/photo-1599909533270-0c3e6e6b99f6?w=400", "Spices & Seasonings", "cat_spices", "100g", 100, 4.3, 112));
        products.add(createProduct("prod_coriander", "Coriander Powder", "Ground coriander", 65.0,
                "https://images.unsplash.com/photo-1596392305498-13f3d5e0e00d?w=400", "Spices & Seasonings", "cat_spices", "100g", 110, 4.4, 98));
        products.add(createProduct("prod_garam_masala", "Garam Masala", "Spice blend", 120.0,
                "https://images.unsplash.com/photo-1596040033229-a0b55e07bdf4?w=400", "Spices & Seasonings", "cat_spices", "100g", 85, 4.6, 145));
        products.add(createProduct("prod_salt", "Table Salt", "Iodized table salt", 20.0,
                "https://images.unsplash.com/photo-1563372552-1a9c6a7b4e82?w=400", "Spices & Seasonings", "cat_spices", "kg", 200, 4.1, 267));

        // Cooking Oils
        products.add(createProduct("prod_sunflower_oil", "Sunflower Oil", "Refined sunflower oil", 150.0,
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400", "Cooking Oils", "cat_cooking_oils", "liter", 90, 4.4, 178));
        products.add(createProduct("prod_olive_oil", "Olive Oil", "Extra virgin olive oil", 450.0,
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400", "Cooking Oils", "cat_cooking_oils", "500ml", 60, 4.7, 134));
        products.add(createProduct("prod_mustard_oil", "Mustard Oil", "Pure mustard oil", 180.0,
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400", "Cooking Oils", "cat_cooking_oils", "liter", 75, 4.3, 112));
        products.add(createProduct("prod_coconut_oil", "Coconut Oil", "Virgin coconut oil", 220.0,
                "https://images.unsplash.com/photo-1523616843462-e6ee8fdfb7da?w=400", "Cooking Oils", "cat_cooking_oils", "500ml", 65, 4.5, 98));

        // Personal Care
        products.add(createProduct("prod_shampoo", "Shampoo", "Herbal shampoo", 180.0,
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400", "Personal Care", "cat_personal_care", "200ml", 100, 4.3, 189));
        products.add(createProduct("prod_soap", "Soap", "Moisturizing soap bar", 35.0,
                "https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=400", "Personal Care", "cat_personal_care", "125g", 150, 4.2, 234));
        products.add(createProduct("prod_toothpaste", "Toothpaste", "Fluoride toothpaste", 75.0,
                "https://images.unsplash.com/photo-1622207706723-b5b76aaf3929?w=400", "Personal Care", "cat_personal_care", "150g", 120, 4.4, 267));
        products.add(createProduct("prod_tissue", "Facial Tissues", "Soft facial tissues", 45.0,
                "https://images.unsplash.com/photo-1584556326561-c7098fad5b7f?w=400", "Personal Care", "cat_personal_care", "box", 110, 4.1, 178));

        // Household Items
        products.add(createProduct("prod_detergent", "Laundry Detergent", "Powerful cleaning detergent", 220.0,
                "https://images.unsplash.com/photo-1610557892470-55d9e80c0bce?w=400", "Household Items", "cat_household", "kg", 85, 4.3, 156));
        products.add(createProduct("prod_dish_soap", "Dish Soap", "Grease-cutting dish soap", 85.0,
                "https://images.unsplash.com/photo-1563899893-5ee2587f7ae7?w=400", "Household Items", "cat_household", "500ml", 130, 4.4, 198));
        products.add(createProduct("prod_garbage_bags", "Garbage Bags", "Heavy duty garbage bags", 120.0,
                "https://images.unsplash.com/photo-1589886397009-d2f9cc7d2b19?w=400", "Household Items", "cat_household", "30 pcs", 95, 4.2, 145));
        products.add(createProduct("prod_foil", "Aluminum Foil", "Kitchen aluminum foil", 90.0,
                "https://images.unsplash.com/photo-1595246140406-01a75caffe18?w=400", "Household Items", "cat_household", "25m roll", 100, 4.1, 123));

        int[] counter = {0};
        for (Product product : products) {
            db.collection("products")
                    .document(product.getProductId())
                    .set(product)
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        Log.d(TAG, "Product added: " + product.getName() + " (" + counter[0] + "/" + products.size() + ")");
                        if (counter[0] == products.size() && onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding product: " + product.getName(), e));
        }
    }

    private Product createProduct(String id, String name, String description, double price,
                                   String imageUrl, String category, String categoryId, String unit, int stock,
                                   double rating, int reviewCount) {
        Product product = new Product(id, name, description, price, imageUrl, category, categoryId, unit, stock);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        return product;
    }

    public interface OnDataPopulatedListener {
        void onSuccess();
    }
}
