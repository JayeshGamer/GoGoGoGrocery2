package com.grocerygo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.grocerygo.app.R;

public class NotificationPreferencesActivity extends AppCompatActivity {
    private ImageView btnBack;
    private SwitchCompat switchOrderUpdates, switchPromotions, switchNewArrivals,
                         switchPriceAlerts, switchEmailNotifications, switchSmsNotifications;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_preferences);

        preferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        initViews();
        setupClickListeners();
        loadPreferences();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchOrderUpdates = findViewById(R.id.switchOrderUpdates);
        switchPromotions = findViewById(R.id.switchPromotions);
        switchNewArrivals = findViewById(R.id.switchNewArrivals);
        switchPriceAlerts = findViewById(R.id.switchPriceAlerts);
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchSmsNotifications = findViewById(R.id.switchSmsNotifications);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchOrderUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("order_updates", isChecked);
            Toast.makeText(this, "Order updates " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        switchPromotions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("promotions", isChecked);
            Toast.makeText(this, "Promotional notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        switchNewArrivals.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("new_arrivals", isChecked);
        });

        switchPriceAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("price_alerts", isChecked);
        });

        switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("email_notifications", isChecked);
        });

        switchSmsNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("sms_notifications", isChecked);
        });
    }

    private void loadPreferences() {
        switchOrderUpdates.setChecked(preferences.getBoolean("order_updates", true));
        switchPromotions.setChecked(preferences.getBoolean("promotions", true));
        switchNewArrivals.setChecked(preferences.getBoolean("new_arrivals", true));
        switchPriceAlerts.setChecked(preferences.getBoolean("price_alerts", false));
        switchEmailNotifications.setChecked(preferences.getBoolean("email_notifications", true));
        switchSmsNotifications.setChecked(preferences.getBoolean("sms_notifications", false));
    }

    private void savePreference(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}

