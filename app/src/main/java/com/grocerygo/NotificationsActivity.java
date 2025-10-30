package com.grocerygo;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerygo.app.R;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    private CardView btnBack;
    private RecyclerView rvNotifications;
    private LinearLayout llEmptyState;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupClickListeners();

        // Show empty state for now (notifications can be implemented later)
        showEmptyState();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvNotifications = findViewById(R.id.rvNotifications);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvTitle = findViewById(R.id.tvTitle);

        if (tvTitle != null) {
            tvTitle.setText("Notifications");
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void showEmptyState() {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvNotifications != null) {
            rvNotifications.setVisibility(View.GONE);
        }
    }
}

