package com.grocerygo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.grocerygo.app.R;

public class AccountPrivacyActivity extends AppCompatActivity {
    private ImageView btnBack;
    private SwitchCompat switchProfileVisibility, switchOrderHistory, switchActivityStatus;
    private CardView cardChangePassword, cardTwoFactorAuth, cardDeleteAccount;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_privacy);

        preferences = getSharedPreferences("PrivacyPrefs", MODE_PRIVATE);

        initViews();
        setupClickListeners();
        loadPrivacySettings();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchProfileVisibility = findViewById(R.id.switchProfileVisibility);
        switchOrderHistory = findViewById(R.id.switchOrderHistory);
        switchActivityStatus = findViewById(R.id.switchActivityStatus);
        cardChangePassword = findViewById(R.id.cardChangePassword);
        cardTwoFactorAuth = findViewById(R.id.cardTwoFactorAuth);
        cardDeleteAccount = findViewById(R.id.cardDeleteAccount);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchProfileVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting("profile_visibility", isChecked);
            Toast.makeText(this, "Profile is now " + (isChecked ? "public" : "private"), Toast.LENGTH_SHORT).show();
        });

        switchOrderHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting("order_history_visible", isChecked);
        });

        switchActivityStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePrivacySetting("activity_status", isChecked);
        });

        cardChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change password feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardTwoFactorAuth.setOnClickListener(v -> {
            Toast.makeText(this, "Two-factor authentication setup", Toast.LENGTH_SHORT).show();
        });

        cardDeleteAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Account deletion requires verification", Toast.LENGTH_LONG).show();
        });
    }

    private void loadPrivacySettings() {
        switchProfileVisibility.setChecked(preferences.getBoolean("profile_visibility", true));
        switchOrderHistory.setChecked(preferences.getBoolean("order_history_visible", false));
        switchActivityStatus.setChecked(preferences.getBoolean("activity_status", true));
    }

    private void savePrivacySetting(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}

