package com.grocerygo.utils;

import com.grocerygo.app.R;

import java.util.HashMap;
import java.util.Map;

public class CategoryIconMapper {
    private static final Map<String, Integer> categoryIcons = new HashMap<>();

    static {
        // Map category names to their respective icons
        categoryIcons.put("Bakery & Bread", R.drawable.ic_bakery);
        categoryIcons.put("Beverages", R.drawable.ic_beverage);
        categoryIcons.put("Cooking Oils", R.drawable.ic_cooking_oil);
        categoryIcons.put("Dairy & Eggs", R.drawable.ic_dairy);
        categoryIcons.put("Meat & Seafood", R.drawable.ic_meat);
        categoryIcons.put("Fruits", R.drawable.ic_fruits);
        categoryIcons.put("Vegetables", R.drawable.ic_vegetables);
        categoryIcons.put("Snacks", R.drawable.ic_snacks);
        categoryIcons.put("Frozen Foods", R.drawable.ic_frozen);

        // Alternative names (case variations)
        categoryIcons.put("bakery & bread", R.drawable.ic_bakery);
        categoryIcons.put("beverages", R.drawable.ic_beverage);
        categoryIcons.put("cooking oils", R.drawable.ic_cooking_oil);
        categoryIcons.put("dairy & eggs", R.drawable.ic_dairy);
        categoryIcons.put("meat & seafood", R.drawable.ic_meat);
        categoryIcons.put("fruits", R.drawable.ic_fruits);
        categoryIcons.put("vegetables", R.drawable.ic_vegetables);
        categoryIcons.put("snacks", R.drawable.ic_snacks);
        categoryIcons.put("frozen foods", R.drawable.ic_frozen);
    }

    /**
     * Get the icon resource ID for a given category name
     * @param categoryName The name of the category
     * @return The drawable resource ID, or a default placeholder if not found
     */
    public static int getIconForCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return R.drawable.ic_categories;
        }

        // Try exact match first
        Integer iconRes = categoryIcons.get(categoryName);
        if (iconRes != null) {
            return iconRes;
        }

        // Try case-insensitive match
        iconRes = categoryIcons.get(categoryName.toLowerCase());
        if (iconRes != null) {
            return iconRes;
        }

        // Try partial match for flexibility
        String lowerCaseName = categoryName.toLowerCase();

        if (lowerCaseName.contains("bakery") || lowerCaseName.contains("bread")) {
            return R.drawable.ic_bakery;
        } else if (lowerCaseName.contains("beverage") || lowerCaseName.contains("drink")) {
            return R.drawable.ic_beverage;
        } else if (lowerCaseName.contains("oil")) {
            return R.drawable.ic_cooking_oil;
        } else if (lowerCaseName.contains("dairy") || lowerCaseName.contains("milk") || lowerCaseName.contains("egg")) {
            return R.drawable.ic_dairy;
        } else if (lowerCaseName.contains("meat") || lowerCaseName.contains("seafood") || lowerCaseName.contains("fish")) {
            return R.drawable.ic_meat;
        } else if (lowerCaseName.contains("fruit")) {
            return R.drawable.ic_fruits;
        } else if (lowerCaseName.contains("vegetable")) {
            return R.drawable.ic_vegetables;
        } else if (lowerCaseName.contains("snack")) {
            return R.drawable.ic_snacks;
        } else if (lowerCaseName.contains("frozen")) {
            return R.drawable.ic_frozen;
        }

        // Default fallback icon
        return R.drawable.ic_categories;
    }

    /**
     * Get the background color resource for a category
     * @param categoryName The name of the category
     * @return The color resource ID
     */
    public static int getBackgroundColorForCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return R.color.background_light_green;
        }

        String lowerCaseName = categoryName.toLowerCase();

        if (lowerCaseName.contains("bakery") || lowerCaseName.contains("bread")) {
            return R.color.card_bakery;
        } else if (lowerCaseName.contains("beverage") || lowerCaseName.contains("drink")) {
            return R.color.background_light_blue;
        } else if (lowerCaseName.contains("oil")) {
            return R.color.card_oil;
        } else if (lowerCaseName.contains("dairy") || lowerCaseName.contains("milk") || lowerCaseName.contains("egg")) {
            return R.color.card_dairy;
        } else if (lowerCaseName.contains("meat") || lowerCaseName.contains("seafood")) {
            return R.color.card_meat;
        } else if (lowerCaseName.contains("fruit")) {
            return R.color.card_fruits;
        } else if (lowerCaseName.contains("vegetable")) {
            return R.color.card_vegetables;
        } else if (lowerCaseName.contains("snack")) {
            return R.color.background_cream;
        } else if (lowerCaseName.contains("frozen")) {
            return R.color.background_light_blue;
        }

        return R.color.background_light_green;
    }
}

