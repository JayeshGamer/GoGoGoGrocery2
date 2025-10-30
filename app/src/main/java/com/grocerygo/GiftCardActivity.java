package com.grocerygo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.grocerygo.app.R;

public class GiftCardActivity extends AppCompatActivity {
    private ImageView btnBack;
    private CardView cardAddGiftCard, cardRedeemCode, cardViewRewards;
    private LinearLayout llActiveGiftCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_card);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardAddGiftCard = findViewById(R.id.cardAddGiftCard);
        cardRedeemCode = findViewById(R.id.cardRedeemCode);
        cardViewRewards = findViewById(R.id.cardViewRewards);
        llActiveGiftCards = findViewById(R.id.llActiveGiftCards);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardAddGiftCard.setOnClickListener(v -> {
            Toast.makeText(this, "Add gift card feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardRedeemCode.setOnClickListener(v -> {
            Toast.makeText(this, "Redeem gift card code", Toast.LENGTH_SHORT).show();
        });

        cardViewRewards.setOnClickListener(v -> {
            Toast.makeText(this, "View your rewards and points", Toast.LENGTH_SHORT).show();
        });
    }
}

