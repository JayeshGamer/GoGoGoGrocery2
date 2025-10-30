package com.grocerygo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.grocerygo.app.R;

public class WalletActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvWalletBalance, tvAvailableBalance, tvPendingAmount;
    private CardView cardAddMoney, cardTransactionHistory, cardLinkAccount;
    private LinearLayout llRecentTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initViews();
        setupClickListeners();
        loadWalletData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        cardAddMoney = findViewById(R.id.cardAddMoney);
        cardTransactionHistory = findViewById(R.id.cardTransactionHistory);
        cardLinkAccount = findViewById(R.id.cardLinkAccount);
        llRecentTransactions = findViewById(R.id.llRecentTransactions);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardAddMoney.setOnClickListener(v -> {
            Toast.makeText(this, "Add money feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardTransactionHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Transaction history feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardLinkAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Link to GroceryGo Money account", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadWalletData() {
        // Simulated wallet data
        tvWalletBalance.setText("₹1,250.00");
        tvAvailableBalance.setText("₹1,200.00");
        tvPendingAmount.setText("₹50.00");
    }
}

