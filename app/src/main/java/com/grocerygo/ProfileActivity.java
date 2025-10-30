package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseUser;
import com.grocerygo.app.R;
import com.grocerygo.firebase.AuthRepository;
import com.grocerygo.firebase.FirebaseManager;
import com.grocerygo.models.User;
import com.grocerygo.utils.DataPreloader;
import com.grocerygo.utils.ThemeManager;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvUserName, tvUserPhone;
    private ImageView ivProfilePicture, btnBack, btnEditName;
    private LinearLayout btnLogout;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private FirebaseManager firebaseManager;
    private ThemeManager themeManager;
    private DataPreloader dataPreloader;

    // Quick action cards
    private CardView cardYourOrders, cardGroceryGoMoney, cardNeedHelp;

    // Admin button
    private LinearLayout llAdminPanel;
    private CardView cardAdminPanel;

    // Developer tools quick link (Database Setup)
    private LinearLayout llDevTools;
    private CardView cardDatabaseSetup;

    // Your information section
    private LinearLayout llAddressBook, llWishlist;

    // Payment & Wallet section
    private LinearLayout llWallet, llPaymentSettings, llGiftCard;

    // Settings & Privacy section
    private LinearLayout llAccountPrivacy, llNotificationPreferences, llShareApp, llAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authRepository = new AuthRepository();
        firebaseManager = FirebaseManager.getInstance();
        dataPreloader = DataPreloader.getInstance();

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void applyTheme() {
        if (themeManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void initViews() {
        // Profile header
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnBack = findViewById(R.id.btnBack);
        btnEditName = findViewById(R.id.btnEditName);
        progressBar = findViewById(R.id.progressBar);

        // Quick actions
        cardYourOrders = findViewById(R.id.cardYourOrders);
        cardGroceryGoMoney = findViewById(R.id.cardGroceryGoMoney);
        cardNeedHelp = findViewById(R.id.cardNeedHelp);

        // Admin button
        llAdminPanel = findViewById(R.id.llAdminPanel);
        cardAdminPanel = findViewById(R.id.cardAdminPanel);

        // Developer tools link - NOW HIDDEN FOR ALL USERS (keep functionality intact)
        llDevTools = findViewById(R.id.llDevTools);
        cardDatabaseSetup = findViewById(R.id.cardDatabaseSetup);
        if (llDevTools != null) {
            // Ensure this developer-only section stays hidden in production builds
            llDevTools.setVisibility(View.GONE);
        }
        if (cardDatabaseSetup != null) {
            // Also hide the card explicitly to be safe
            cardDatabaseSetup.setVisibility(View.GONE);
        }

        // Your information
        llAddressBook = findViewById(R.id.llAddressBook);
        llWishlist = findViewById(R.id.llWishlist);

        // Payment & Wallet section
        llWallet = findViewById(R.id.llWallet);
        llPaymentSettings = findViewById(R.id.llPaymentSettings);
        llGiftCard = findViewById(R.id.llGiftCard);

        // Settings & Privacy section
        llAccountPrivacy = findViewById(R.id.llAccountPrivacy);
        llNotificationPreferences = findViewById(R.id.llNotificationPreferences);
        llShareApp = findViewById(R.id.llShareApp);
        llAboutUs = findViewById(R.id.llAboutUs);

        // Buttons
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Edit name functionality
        btnEditName.setOnClickListener(v -> showEditNameDialog());

        // Quick actions
        cardYourOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        cardGroceryGoMoney.setOnClickListener(v -> {
            Toast.makeText(this, "GroceryGo Money feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardNeedHelp.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        // Database Setup: open DatabasePopulatorActivity - SET ON CARDVIEW
        if (cardDatabaseSetup != null) {
            cardDatabaseSetup.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DatabasePopulatorActivity.class);
                startActivity(intent);
            });
        }

        // Your information section
        llAddressBook.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfileActivity.this, AddressBookActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(ProfileActivity.this, "Unable to open address book", Toast.LENGTH_SHORT).show();
            }
        });

        llWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        // Payment & Wallet section
        llWallet.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WalletActivity.class);
            startActivity(intent);
        });

        llPaymentSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PaymentSettingsActivity.class);
            startActivity(intent);
        });

        llGiftCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GiftCardActivity.class);
            startActivity(intent);
        });

        // Settings & Privacy section
        llAccountPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AccountPrivacyActivity.class);
            startActivity(intent);
        });

        llNotificationPreferences.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationPreferencesActivity.class);
            startActivity(intent);
        });

        llShareApp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing app: [App Link]");
            startActivity(Intent.createChooser(intent, "Share App Via"));
        });

        llAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

        // Buttons
        btnLogout.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to profile
        loadUserData();
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");

        // Create EditText
        final EditText input = new EditText(this);
        input.setText(tvUserName.getText().toString());
        input.setHint("Enter your name");
        input.setPadding(50, 30, 50, 30);
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        input.setHintTextColor(getResources().getColor(R.color.text_hint, null));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserName(String newName) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            authRepository.updateUserName(userId, newName)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        tvUserName.setText(newName);
                        Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        // Try to use preloaded user data first - IMMEDIATE, no fallback delay
        User cachedUser = dataPreloader.getCurrentUser();
        if (cachedUser != null) {
            // Set data immediately from cache
            tvUserName.setText(cachedUser.getName() != null ? cachedUser.getName() : "No Name");
            tvUserPhone.setText(cachedUser.getPhone() != null ? cachedUser.getPhone() : "No Phone");

            // Check admin access
            checkAndShowAdminButton(cachedUser);

            // Optionally refresh in background without showing progress
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                authRepository.getUserData(userId)
                    .addOnSuccessListener(user -> {
                        if (user != null) {
                            // Update only if data changed
                            if (!cachedUser.getName().equals(user.getName()) ||
                                !cachedUser.getPhone().equals(user.getPhone())) {
                                tvUserName.setText(user.getName() != null ? user.getName() : "No Name");
                                tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "No Phone");
                            }
                            // Re-check admin access with fresh data
                            checkAndShowAdminButton(user);
                        }
                    });
            }
            return;
        }

        // Fallback to fetching from Firebase (only if no cache)
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            authRepository.getUserData(userId)
                    .addOnSuccessListener(user -> {
                        progressBar.setVisibility(View.GONE);
                        if (user != null) {
                            tvUserName.setText(user.getName() != null ? user.getName() : "No Name");
                            tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "No Phone");

                            // Check admin access
                            checkAndShowAdminButton(user);
                        } else {
                            // If user data doesn't exist in Firestore, show Firebase auth data
                            tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                            tvUserPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "No Phone");

                            // No admin access if user data doesn't exist
                            if (llAdminPanel != null) {
                                llAdminPanel.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        // Show basic auth data
                        tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");

                        // No admin access on error
                        if (llAdminPanel != null) {
                            llAdminPanel.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void checkAndShowAdminButton(User user) {
        if (llAdminPanel != null) {
            if (user != null && user.isAdmin()) {
                llAdminPanel.setVisibility(View.VISIBLE);
                // Set up click listener on the CardView instead of parent LinearLayout
                if (cardAdminPanel != null) {
                    cardAdminPanel.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, AdminPanelActivity.class);
                        startActivity(intent);
                    });
                }
            } else {
                llAdminPanel.setVisibility(View.GONE);
                if (cardAdminPanel != null) {
                    cardAdminPanel.setOnClickListener(null);
                }
            }
        }
    }

    private void logout() {
        // Clear preloaded data cache on logout
        dataPreloader.clearCache();

        authRepository.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
