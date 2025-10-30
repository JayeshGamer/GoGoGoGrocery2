package com.grocerygo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grocerygo.app.R;

public class HelpActivity extends AppCompatActivity {
    private ImageView btnBack;
    private LinearLayout llContactSupport, llFAQ, llTermsConditions, llPrivacyPolicy,
                        llAboutUs, llRateApp, llShareApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        llContactSupport = findViewById(R.id.llContactSupport);
        llFAQ = findViewById(R.id.llFAQ);
        llTermsConditions = findViewById(R.id.llTermsConditions);
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);
        llAboutUs = findViewById(R.id.llAboutUs);
        llRateApp = findViewById(R.id.llRateApp);
        llShareApp = findViewById(R.id.llShareApp);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        llContactSupport.setOnClickListener(v -> contactSupport());
        llFAQ.setOnClickListener(v -> showFAQ());
        llTermsConditions.setOnClickListener(v -> openWebPage("https://grocerygo.com/terms"));
        llPrivacyPolicy.setOnClickListener(v -> openWebPage("https://grocerygo.com/privacy"));
        llAboutUs.setOnClickListener(v -> showAboutUs());
        llRateApp.setOnClickListener(v -> rateApp());
        llShareApp.setOnClickListener(v -> shareApp());
    }

    private void contactSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@grocerygo.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "GroceryGo Support Request");

        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFAQ() {
        // Open FAQ screen or web page
        Toast.makeText(this, "FAQ section coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No browser installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutUs() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About GroceryGo")
                .setMessage("GroceryGo v1.0.0\n\n" +
                        "Fresh Groceries at Your Doorstep\n\n" +
                        "We deliver fresh groceries and daily essentials right to your door. " +
                        "Shop from a wide range of products including fruits, vegetables, dairy, " +
                        "bakery items, and more.\n\n" +
                        "© 2025 GroceryGo. All rights reserved.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void rateApp() {
        // Open Play Store or App Store to rate the app
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            // If Play Store not available, open in browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            startActivity(intent);
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GroceryGo App");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out GroceryGo - Fresh Groceries at Your Doorstep!\n\n" +
                "Download now: https://play.google.com/store/apps/details?id=" + getPackageName());

        startActivity(Intent.createChooser(shareIntent, "Share GroceryGo"));
    }
}

