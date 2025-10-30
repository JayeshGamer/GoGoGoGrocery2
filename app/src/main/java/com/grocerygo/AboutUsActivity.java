package com.grocerygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.grocerygo.app.R;

public class AboutUsActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvAppVersion;
    private LinearLayout llTermsConditions, llPrivacyPolicy, llContactUs, llRateApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initViews();
        setupClickListeners();
        loadAppInfo();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAppVersion = findViewById(R.id.tvAppVersion);
        llTermsConditions = findViewById(R.id.llTermsConditions);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        llContactUs = findViewById(R.id.llContactUs);
        llRateApp = findViewById(R.id.llRateApp);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        llTermsConditions.setOnClickListener(v -> {
            // Open terms and conditions
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://grocerygo.com/terms"));
            startActivity(browserIntent);
        });

        llPrivacyPolicy.setOnClickListener(v -> {
            // Open privacy policy
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://grocerygo.com/privacy"));
            startActivity(browserIntent);
        });

        llContactUs.setOnClickListener(v -> {
            // Open email client
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@grocerygo.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "GroceryGo Support Query");
            startActivity(Intent.createChooser(emailIntent, "Contact Us"));
        });

        llRateApp.setOnClickListener(v -> {
            // Open Play Store for rating
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });
    }

    private void loadAppInfo() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }
}

