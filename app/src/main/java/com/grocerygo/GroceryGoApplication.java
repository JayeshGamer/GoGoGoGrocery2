package com.grocerygo;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class GroceryGoApplication extends Application {
    private static final String TAG = "GroceryGoApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");

            // Initialize Firebase App Check
            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
            Log.d(TAG, "Firebase App Check initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
}

