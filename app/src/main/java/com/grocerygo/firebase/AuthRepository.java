package com.grocerygo.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.models.User;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        FirebaseManager manager = FirebaseManager.getInstance();
        this.auth = manager.getAuth();
        this.db = manager.getDb();
    }

    // Sign up with email and password
    public Task<AuthResult> signUpWithEmail(String email, String password, String name, String phone) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Create user document in Firestore
                            User user = new User(firebaseUser.getUid(), email, name, phone);
                            return createUserDocument(user).continueWith(t -> task.getResult());
                        }
                    }
                    return task;
                });
    }

    // Sign in with email and password
    public Task<AuthResult> signInWithEmail(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    // Sign out
    public void signOut() {
        auth.signOut();
    }

    // Create user document in Firestore
    private Task<Void> createUserDocument(User user) {
        return db.collection("users")
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User document created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating user document", e));
    }

    // Get current user
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Check if user is logged in
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // Send password reset email
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    // Update user profile
    public Task<Void> updateUserProfile(String userId, String name, String phone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        return db.collection("users")
                .document(userId)
                .update(updates);
    }

    // Update user name only
    public Task<Void> updateUserName(String userId, String name) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        return db.collection("users")
                .document(userId)
                .update(updates);
    }

    // Get user data from Firestore
    public Task<User> getUserData(String userId) {
        return db.collection("users")
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(User.class);
                    }
                    return null;
                });
    }
}
