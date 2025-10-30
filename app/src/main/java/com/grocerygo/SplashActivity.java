package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.grocerygo.app.R;
import com.grocerygo.firebase.FirebaseManager;
import com.grocerygo.utils.DataPreloader;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int MIN_SPLASH_DURATION = 800; // Reduced to 800ms minimum
    private static final int MAX_SPLASH_DURATION = 3000; // Reduced to 3 seconds max timeout

    private ProgressBar progressBar;
    private TextView tvLoadingStatus;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startTime = System.currentTimeMillis();

        // Check authentication status immediately
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        if (!firebaseManager.isUserLoggedIn()) {
            // User is not logged in, show splash and go to login
            setContentView(R.layout.activity_splash);
            progressBar = findViewById(R.id.progressBar);
            tvLoadingStatus = findViewById(R.id.tvLoadingStatus);

            // Navigate to LoginActivity after minimum delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, MIN_SPLASH_DURATION);
            return;
        }

        // User is logged in, show splash with loading indicator and preload data
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingStatus = findViewById(R.id.tvLoadingStatus);

        // Show loading indicators
        progressBar.setVisibility(View.VISIBLE);
        tvLoadingStatus.setVisibility(View.VISIBLE);
        tvLoadingStatus.setText("Loading your data...");

        // Start preloading data
        preloadDataAndNavigate();
    }

    private void preloadDataAndNavigate() {
        Log.d(TAG, "Starting data preload...");

        DataPreloader dataPreloader = DataPreloader.getInstance();

        // Set timeout to prevent indefinite loading
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            Log.w(TAG, "Preload timeout reached, navigating anyway");
            navigateToHome();
        };
        timeoutHandler.postDelayed(timeoutRunnable, MAX_SPLASH_DURATION);

        // Preload all data
        dataPreloader.preloadAllData()
            .addOnCompleteListener(task -> {
                // Cancel timeout
                timeoutHandler.removeCallbacks(timeoutRunnable);

                if (task.isSuccessful()) {
                    Log.d(TAG, "Data preload successful");
                } else {
                    Log.e(TAG, "Data preload failed", task.getException());
                }

                // Ensure minimum splash duration for smooth UX (but reduced)
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = MIN_SPLASH_DURATION - elapsedTime;

                if (remainingTime > 0) {
                    // Only wait the remaining time
                    new Handler(Looper.getMainLooper()).postDelayed(
                        this::navigateToHome,
                        remainingTime
                    );
                } else {
                    // Navigate immediately if minimum time has passed
                    navigateToHome();
                }
            });
    }

    private void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
