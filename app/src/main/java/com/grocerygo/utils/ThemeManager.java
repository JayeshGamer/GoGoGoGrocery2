package com.grocerygo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    private static final String PREFS_NAME = "GroceryGoPrefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    private SharedPreferences preferences;

    public ThemeManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setThemeMode(int mode) {
        preferences.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        return preferences.getInt(KEY_THEME_MODE, THEME_LIGHT);
    }

    public boolean isDarkMode() {
        return getThemeMode() == THEME_DARK;
    }
}

