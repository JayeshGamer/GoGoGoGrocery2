package com.grocerygo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.grocerygo.app.R;

public class PaymentSettingsActivity extends AppCompatActivity {
    private ImageView btnBack;
    private LinearLayout llAddCard, llAddUPI, llAddNetBanking;
    private CardView cardDefaultPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_settings);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        llAddCard = findViewById(R.id.llAddCard);
        llAddUPI = findViewById(R.id.llAddUPI);
        llAddNetBanking = findViewById(R.id.llAddNetBanking);
        cardDefaultPayment = findViewById(R.id.cardDefaultPayment);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        llAddCard.setOnClickListener(v -> {
            Toast.makeText(this, "Add Credit/Debit Card", Toast.LENGTH_SHORT).show();
        });

        llAddUPI.setOnClickListener(v -> {
            Toast.makeText(this, "Add UPI Payment Method", Toast.LENGTH_SHORT).show();
        });

        llAddNetBanking.setOnClickListener(v -> {
            Toast.makeText(this, "Add Net Banking", Toast.LENGTH_SHORT).show();
        });

        cardDefaultPayment.setOnClickListener(v -> {
            Toast.makeText(this, "Set Default Payment Method", Toast.LENGTH_SHORT).show();
        });
    }
}

