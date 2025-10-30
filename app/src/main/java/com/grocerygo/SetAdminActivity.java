package com.grocerygo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.grocerygo.app.R;
import com.grocerygo.utils.RoleManager;

/**
 * Developer utility to set current user as admin
 * This should be used only for testing purposes
 */
public class SetAdminActivity extends AppCompatActivity {

    private TextView tvCurrentUser;
    private Button btnSetAdmin, btnSetCustomer, btnCheckRole;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_admin);

        roleManager = new RoleManager();

        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        btnSetAdmin = findViewById(R.id.btnSetAdmin);
        btnSetCustomer = findViewById(R.id.btnSetCustomer);
        btnCheckRole = findViewById(R.id.btnCheckRole);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvCurrentUser.setText("Current User: " + currentUser.getEmail() + "\nUID: " + currentUser.getUid());

            btnSetAdmin.setOnClickListener(v -> {
                roleManager.setUserAsAdmin(currentUser.getUid())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User set as ADMIN successfully!", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });

            btnSetCustomer.setOnClickListener(v -> {
                roleManager.setUserAsCustomer(currentUser.getUid())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User set as CUSTOMER successfully!", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });

            btnCheckRole.setOnClickListener(v -> {
                roleManager.getUserRole(currentUser.getUid())
                        .addOnSuccessListener(role -> {
                            Toast.makeText(this, "Current role: " + role.toUpperCase(), Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to check role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });
        } else {
            tvCurrentUser.setText("No user logged in!");
            btnSetAdmin.setEnabled(false);
            btnSetCustomer.setEnabled(false);
            btnCheckRole.setEnabled(false);
        }
    }
}

