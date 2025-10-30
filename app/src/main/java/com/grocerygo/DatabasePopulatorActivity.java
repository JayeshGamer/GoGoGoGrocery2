package com.grocerygo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grocerygo.app.R;
import com.grocerygo.utils.FirebaseDataPopulator;
import com.grocerygo.utils.DeliveryPartnerPopulator;

public class DatabasePopulatorActivity extends AppCompatActivity {

    private Button btnPopulateData, btnPopulateDeliveryPartners, btnClearDeliveryPartners;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private DeliveryPartnerPopulator deliveryPartnerPopulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_populator);

        deliveryPartnerPopulator = new DeliveryPartnerPopulator();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnPopulateData = findViewById(R.id.btnPopulateData);
        btnPopulateDeliveryPartners = findViewById(R.id.btnPopulateDeliveryPartners);
        btnClearDeliveryPartners = findViewById(R.id.btnClearDeliveryPartners);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupClickListeners() {
        btnPopulateData.setOnClickListener(v -> populateDatabase());
        btnPopulateDeliveryPartners.setOnClickListener(v -> populateDeliveryPartners());
        btnClearDeliveryPartners.setOnClickListener(v -> clearDeliveryPartners());
    }

    private void populateDatabase() {
        btnPopulateData.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Populating database...\nPlease wait...");

        FirebaseDataPopulator populator = new FirebaseDataPopulator();
        populator.populateSampleData(() -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnPopulateData.setEnabled(true);
                tvStatus.setText("✓ Database populated successfully!\n\n12 Categories\n70+ Products\n\nYou can now browse categories and products.");
                Toast.makeText(DatabasePopulatorActivity.this,
                        "Database populated successfully!", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void populateDeliveryPartners() {
        btnPopulateDeliveryPartners.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Adding delivery partners...\nPlease wait...");

        deliveryPartnerPopulator.populateDeliveryPartners()
                .addOnSuccessListener(aVoid -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnPopulateDeliveryPartners.setEnabled(true);
                        tvStatus.setText("✓ Delivery Partners Added Successfully!\n\n8 Delivery Partners Created:\n" +
                                "• Ravi Kumar\n" +
                                "• Amit Sharma\n" +
                                "• Priya Singh\n" +
                                "• Rajesh Verma\n" +
                                "• Sneha Patel\n" +
                                "• Vikram Reddy\n" +
                                "• Anjali Gupta\n" +
                                "• Suresh Kumar\n\n" +
                                "All partners are now available in the Admin Panel!");
                        Toast.makeText(this, "8 Delivery Partners Added!", Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnPopulateDeliveryPartners.setEnabled(true);
                        tvStatus.setText("✗ Failed to add delivery partners\n\nError: " + e.getMessage());
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                });
    }

    private void clearDeliveryPartners() {
        btnClearDeliveryPartners.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Clearing delivery partners...\nPlease wait...");

        deliveryPartnerPopulator.clearDeliveryPartners()
                .addOnSuccessListener(aVoid -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnClearDeliveryPartners.setEnabled(true);
                        tvStatus.setText("✓ All delivery partners cleared!");
                        Toast.makeText(this, "Delivery partners cleared successfully!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnClearDeliveryPartners.setEnabled(true);
                        tvStatus.setText("✗ Failed to clear delivery partners\n\nError: " + e.getMessage());
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }
}
