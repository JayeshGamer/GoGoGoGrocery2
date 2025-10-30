package com.grocerygo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.grocerygo.app.R;
import com.grocerygo.models.Address;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;

public class AddressBookActivity extends AppCompatActivity {
    private RecyclerView rvAddresses;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private FloatingActionButton fabAddAddress;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    // Location client
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_address_book);

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            initViews();
            setupRecyclerView();
            loadAddresses();
        } catch (Exception e) {
            // Log and avoid crashing the launcher activity
            android.util.Log.e("AddressBook", "Error in onCreate - failing gracefully", e);
            runOnUiThread(() -> android.widget.Toast.makeText(this, "Unable to open address book", android.widget.Toast.LENGTH_SHORT).show());
            finish();
        }
    }

    private void initViews() {
        // Use defensive findViewById checks to avoid NPE in case layout changes
        View root = findViewById(android.R.id.content);
        if (root == null) {
            // fallback - try direct finds but guard
            rvAddresses = null;
            fabAddAddress = null;
            progressBar = null;
            llEmptyState = null;
            btnBack = null;
            return;
        }

        rvAddresses = root.findViewById(R.id.rvAddresses);
        fabAddAddress = root.findViewById(R.id.fabAddAddress);
        progressBar = root.findViewById(R.id.progressBar);
        llEmptyState = root.findViewById(R.id.llEmptyState);
        btnBack = root.findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (fabAddAddress != null) {
            fabAddAddress.setOnClickListener(v -> showAddAddressDialog(null));
        }
    }

    private void setupRecyclerView() {
        addressList = new ArrayList<>();
        try {
            addressAdapter = new AddressAdapter(addressList, this::onAddressClick, this::onEditAddress, this::onDeleteAddress);
            if (rvAddresses != null) {
                rvAddresses.setLayoutManager(new LinearLayoutManager(this));
                rvAddresses.setAdapter(addressAdapter);
            } else {
                android.util.Log.w("AddressBook", "rvAddresses is null - RecyclerView not initialized");
            }
        } catch (Exception e) {
            android.util.Log.e("AddressBook", "Error setting up RecyclerView", e);
        }
    }

    private void setVisibilitySafe(View v, int visibility) {
        if (v == null) return;
        try { v.setVisibility(visibility); } catch (Exception e) { android.util.Log.w("AddressBook", "Failed to set visibility", e); }
    }

    private void loadAddresses() {
        setVisibilitySafe(progressBar, View.VISIBLE);
        setVisibilitySafe(llEmptyState, View.GONE);
        setVisibilitySafe(rvAddresses, View.GONE);

        if (userId.isEmpty()) {
            setVisibilitySafe(progressBar, View.GONE);
            showEmptyState("Please login to view addresses");
            android.util.Log.w("AddressBook", "User ID is empty - user not logged in");
            // Redirect to login
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("AddressBook", "Loading addresses for userId: " + userId);

        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setVisibilitySafe(progressBar, View.GONE);
                    if (addressList != null) addressList.clear();

                    android.util.Log.d("AddressBook", "Query successful. Documents found: " +
                        (queryDocumentSnapshots != null ? queryDocumentSnapshots.size() : 0));

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Address address = document.toObject(Address.class);
                            if (address != null) {
                                if (address.getAddressId() == null || address.getAddressId().isEmpty()) {
                                    address.setAddressId(document.getId());
                                }
                                addressList.add(address);
                            }
                        }
                        setVisibilitySafe(rvAddresses, View.VISIBLE);
                        setVisibilitySafe(llEmptyState, View.GONE);
                        if (addressAdapter != null) addressAdapter.notifyDataSetChanged();
                        android.util.Log.d("AddressBook", "Loaded " + addressList.size() + " addresses successfully");
                    } else {
                        setVisibilitySafe(rvAddresses, View.GONE);
                        showEmptyState("No Addresses Saved\n\nTap + to add your first address");
                        android.util.Log.d("AddressBook", "No addresses found for this user - showing empty state");
                    }
                })
                .addOnFailureListener(e -> {
                    setVisibilitySafe(progressBar, View.GONE);
                    setVisibilitySafe(rvAddresses, View.GONE);

                    String errorMsg = e.getMessage();
                    android.util.Log.e("AddressBook", "Failed to load addresses. Error type: " + e.getClass().getName(), e);
                    android.util.Log.e("AddressBook", "Error message: " + errorMsg);

                    // Check for permission errors
                    if (errorMsg != null && (errorMsg.contains("PERMISSION_DENIED") || errorMsg.contains("permission"))) {
                        android.util.Log.e("AddressBook", "PERMISSION ERROR: Firestore rules need to be updated");
                        showEmptyState("Permission Denied\n\nPlease update Firebase Firestore rules to allow address access.\n\nSee FIRESTORE_RULES_DEPLOYMENT.md for instructions.");

                        // Show detailed error dialog
                        new AlertDialog.Builder(this)
                                .setTitle("Firebase Permission Error")
                                .setMessage("Your Firestore security rules don't allow access to addresses.\n\n" +
                                           "To fix this:\n" +
                                           "1. Go to Firebase Console\n" +
                                           "2. Open Firestore Database → Rules\n" +
                                           "3. Add rules for 'addresses' collection\n" +
                                           "4. Deploy the rules\n\n" +
                                           "See FIRESTORE_RULES_DEPLOYMENT.md in your project for detailed instructions.")
                                .setPositiveButton("OK", null)
                                .setNegativeButton("Retry", (dialog, which) -> loadAddresses())
                                .show();
                    } else if (errorMsg != null && (errorMsg.contains("UNAVAILABLE") || errorMsg.contains("network"))) {
                        android.util.Log.e("AddressBook", "NETWORK ERROR: Check internet connection");
                        showEmptyState("Unable to load addresses\n\nPlease check your internet connection");
                        Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
                    } else {
                        showEmptyState("Unable to load addresses\n\nPlease try again later");
                        Toast.makeText(this, "Error: " + (errorMsg != null ? errorMsg : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateEmptyState() {
        if (addressList.isEmpty()) {
            rvAddresses.setVisibility(View.GONE);
            showEmptyState("No Addresses Saved");
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvAddresses.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(String message) {
        llEmptyState.setVisibility(View.VISIBLE);
        rvAddresses.setVisibility(View.GONE);

        // Update the empty state message if the TextView exists
        try {
            TextView tvEmptyMessage = llEmptyState.findViewById(R.id.tvEmptyMessage);
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setText(message);
            }
        } catch (Exception e) {
            // If TextView not found, just show the default empty state
            android.util.Log.e("AddressBook", "Empty message TextView not found", e);
        }
    }

    private void onAddressClick(Address address) {
        // Show dialog to set as main address
        new AlertDialog.Builder(this)
                .setTitle("Set Main Address")
                .setMessage("Do you want to set this as your main delivery address?")
                .setPositiveButton("Yes", (dialog, which) -> setMainAddress(address))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onEditAddress(Address address) {
        showAddAddressDialog(address);
    }

    private void onDeleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setMainAddress(Address address) {
        setVisibilitySafe(progressBar, View.VISIBLE);

        // First, unset all default addresses for this user
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("isDefault", false);
                    }

                    // Then set the selected address as main/default
                    db.collection("addresses")
                            .document(address.getAddressId())
                            .update("isDefault", true)
                            .addOnSuccessListener(aVoid -> {
                                setVisibilitySafe(progressBar, View.GONE);
                                Toast.makeText(this, "Main address updated successfully", Toast.LENGTH_SHORT).show();
                                loadAddresses();
                            })
                            .addOnFailureListener(e -> {
                                setVisibilitySafe(progressBar, View.GONE);
                                Toast.makeText(this, "Failed to update main address", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setVisibilitySafe(progressBar, View.GONE);
                    Toast.makeText(this, "Error updating main address", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAddress(Address address) {
        db.collection("addresses")
                .document(address.getAddressId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to delete address", Toast.LENGTH_SHORT).show());
    }

    private void showAddAddressDialog(Address existingAddress) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);

        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddressLine1 = dialogView.findViewById(R.id.etAddressLine1);
        EditText etAddressLine2 = dialogView.findViewById(R.id.etAddressLine2);
        Spinner spCity = dialogView.findViewById(R.id.spCity);
        Spinner spState = dialogView.findViewById(R.id.spState);
        EditText etPincode = dialogView.findViewById(R.id.etPincode);
        RadioGroup rgAddressType = dialogView.findViewById(R.id.rgAddressType);
        Button btnUseCurrent = dialogView.findViewById(R.id.btnUseCurrentAddress);

        // Set input type and max length for phone number - ONLY DIGITS, MAX 10
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPhone.setFilters(new android.text.InputFilter[]{
            new android.text.InputFilter.LengthFilter(10)
        });
        
        // Add text watcher for real-time validation
        etPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user starts typing
                if (s.length() > 0) {
                    etPhone.setError(null);
                }
                
                // Show error if length exceeds 10 (shouldn't happen with filter, but just in case)
                if (s.length() > 10) {
                    etPhone.setError("Maximum 10 digits allowed");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Populate spinners from string arrays
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.india_cities_shortlist, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this,
                R.array.india_states_shortlist, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spState.setAdapter(stateAdapter);

        if (existingAddress != null) {
            etFullName.setText(existingAddress.getFullName());
            etPhone.setText(existingAddress.getPhoneNumber());
            etAddressLine1.setText(existingAddress.getAddressLine1());
            etAddressLine2.setText(existingAddress.getAddressLine2());
            etPincode.setText(existingAddress.getPincode());

            // Try to set spinner selections for city and state if present
            try {
                if (existingAddress.getCity() != null) {
                    int pos = cityAdapter.getPosition(existingAddress.getCity());
                    if (pos >= 0) spCity.setSelection(pos);
                }
                if (existingAddress.getState() != null) {
                    int pos = stateAdapter.getPosition(existingAddress.getState());
                    if (pos >= 0) spState.setSelection(pos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ("Work".equals(existingAddress.getAddressType())) {
                rgAddressType.check(R.id.rbWork);
            } else if ("Other".equals(existingAddress.getAddressType())) {
                rgAddressType.check(R.id.rbOther);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingAddress == null ? "Add Address" : "Edit Address")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        // Use current address button handler - FIXED
        btnUseCurrent.setOnClickListener(v -> {
            // Check if location permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
                Toast.makeText(this, "Please grant location permission and try again", Toast.LENGTH_LONG).show();
                return;
            }

            // Show loading state on button
            btnUseCurrent.setEnabled(false);
            btnUseCurrent.setText("Getting location...");

            // First try getLastLocation for quick response
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // We have a cached location - use it
                            android.util.Log.d("AddressBook", "Using cached location: " + location.getLatitude() + ", " + location.getLongitude());
                            fetchAddressFromCoordinates(location.getLatitude(), location.getLongitude(),
                                (formattedAddress, city, state, pincode) -> {
                                runOnUiThread(() -> populateAddressFields(btnUseCurrent, etAddressLine1, etAddressLine2,
                                    spCity, spState, etPincode, cityAdapter, stateAdapter,
                                    formattedAddress, city, state, pincode));
                            });
                        } else {
                            // No cached location - request fresh location
                            android.util.Log.d("AddressBook", "No cached location, requesting current location...");
                            requestCurrentLocation(btnUseCurrent, etAddressLine1, etAddressLine2,
                                spCity, spState, etPincode, cityAdapter, stateAdapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AddressBook", "getLastLocation failed, trying current location", e);
                        // If getLastLocation fails, try requesting current location
                        requestCurrentLocation(btnUseCurrent, etAddressLine1, etAddressLine2,
                            spCity, spState, etPincode, cityAdapter, stateAdapter);
                    });
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String fullName = etFullName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String addressLine1 = etAddressLine1.getText().toString().trim();
                String addressLine2 = etAddressLine2.getText().toString().trim();
                String city = spCity.getSelectedItem() != null ? spCity.getSelectedItem().toString().trim() : "";
                String state = spState.getSelectedItem() != null ? spState.getSelectedItem().toString().trim() : "";
                String pincode = etPincode.getText().toString().trim();

                if (fullName.isEmpty() || phone.isEmpty() || addressLine1.isEmpty() ||
                        city.isEmpty() || state.isEmpty() || pincode.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Enforce phone to be exactly 10 numeric digits
                if (!phone.matches("\\d{10}")) {
                    etPhone.setError("Enter a valid 10-digit phone number");
                    etPhone.requestFocus();
                    return;
                }

                int selectedTypeId = rgAddressType.getCheckedRadioButtonId();
                String addressType = "Home";
                if (selectedTypeId == R.id.rbWork) {
                    addressType = "Work";
                } else if (selectedTypeId == R.id.rbOther) {
                    addressType = "Other";
                }

                saveAddress(existingAddress, fullName, phone, addressLine1, addressLine2,
                        city, state, pincode, addressType);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // Handle permission result for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted. Tap 'Use Current Address' again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Reverse-geocode coordinates using Google Geocoding REST API and return formatted address components.
     * This runs network I/O in a background thread and calls callback on completion.
     */
    private void fetchAddressFromCoordinates(double lat, double lng, GeocodeCallback callback) {
        new Thread(() -> {
            String apiKey = getString(R.string.google_maps_api_key);
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GOOGLE_MAPS_API_KEY")) {
                runOnUiThread(() -> Toast.makeText(this, "Google Maps API key not set. Please update strings.xml", Toast.LENGTH_LONG).show());
                callback.onResult(null, null, null, null);
                return;
            }

            String urlStr = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=" + apiKey;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray results = root.optJSONArray("results");
                if (results != null && results.length() > 0) {
                    JSONObject first = results.getJSONObject(0);
                    String formattedAddress = first.optString("formatted_address", null);

                    String city = null;
                    String state = null;
                    String pincode = null;

                    JSONArray components = first.optJSONArray("address_components");
                    if (components != null) {
                        for (int i = 0; i < components.length(); i++) {
                            JSONObject comp = components.getJSONObject(i);
                            JSONArray types = comp.optJSONArray("types");
                            if (types != null) {
                                for (int t = 0; t < types.length(); t++) {
                                    String type = types.optString(t);
                                    if ("locality".equals(type) || "postal_town".equals(type)) {
                                        city = comp.optString("long_name");
                                    } else if ("administrative_area_level_1".equals(type)) {
                                        state = comp.optString("long_name");
                                    } else if ("postal_code".equals(type)) {
                                        pincode = comp.optString("long_name");
                                    }
                                }
                            }
                        }
                    }

                    callback.onResult(formattedAddress, city, state, pincode);
                    return;
                }

                callback.onResult(null, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(null, null, null, null);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * Save a new address or update an existing address in Firestore.
     */
    private void saveAddress(Address existingAddress, String fullName, String phone, String addressLine1,
                             String addressLine2, String city, String state, String pincode, String addressType) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Please login to save address", Toast.LENGTH_SHORT).show();
            return;
        }

        setVisibilitySafe(progressBar, View.VISIBLE);

        // Build Address object
        Address addr = new Address(userId, fullName, phone, addressLine1, addressLine2, city, state, pincode, addressType);

        if (existingAddress != null && existingAddress.getAddressId() != null && !existingAddress.getAddressId().isEmpty()) {
            // Preserve default flag if present
            addr.setDefault(existingAddress.isDefault());
            addr.setAddressId(existingAddress.getAddressId());

            db.collection("addresses")
                    .document(existingAddress.getAddressId())
                    .set(addr)
                    .addOnSuccessListener(aVoid -> {
                        setVisibilitySafe(progressBar, View.GONE);
                        Toast.makeText(this, "Address updated", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    })
                    .addOnFailureListener(e -> {
                        setVisibilitySafe(progressBar, View.GONE);
                        Toast.makeText(this, "Failed to update address: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            db.collection("addresses")
                    .add(addr)
                    .addOnSuccessListener(documentReference -> {
                        // Optionally store the generated id inside the document for easy lookup
                        String newId = documentReference.getId();
                        documentReference.update("addressId", newId);
                        setVisibilitySafe(progressBar, View.GONE);
                        Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    })
                    .addOnFailureListener(e -> {
                        setVisibilitySafe(progressBar, View.GONE);
                        Toast.makeText(this, "Failed to save address: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    /**
     * Request fresh current location using FusedLocationProviderClient
     */
    private void requestCurrentLocation(Button btnUseCurrent, EditText etAddressLine1, EditText etAddressLine2,
                                       Spinner spCity, Spinner spState, EditText etPincode,
                                       ArrayAdapter<CharSequence> cityAdapter, ArrayAdapter<CharSequence> stateAdapter) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            runOnUiThread(() -> {
                btnUseCurrent.setEnabled(true);
                btnUseCurrent.setText("Use Current Address");
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        android.util.Log.d("AddressBook", "Requesting current location...");

        // Use getCurrentLocation for fresh location (requires Google Play Services)
        com.google.android.gms.location.LocationRequest locationRequest =
            new com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                10000) // 10 seconds
            .setMaxUpdates(1)
            .build();

        com.google.android.gms.tasks.CancellationTokenSource cancellationTokenSource =
            new com.google.android.gms.tasks.CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.getToken())
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    android.util.Log.d("AddressBook", "Got fresh location: " + location.getLatitude() + ", " + location.getLongitude());
                    fetchAddressFromCoordinates(location.getLatitude(), location.getLongitude(),
                        (formattedAddress, city, state, pincode) -> {
                        runOnUiThread(() -> populateAddressFields(btnUseCurrent, etAddressLine1, etAddressLine2,
                            spCity, spState, etPincode, cityAdapter, stateAdapter,
                            formattedAddress, city, state, pincode));
                    });
                } else {
                    android.util.Log.e("AddressBook", "getCurrentLocation returned null");
                    runOnUiThread(() -> {
                        btnUseCurrent.setEnabled(true);
                        btnUseCurrent.setText("Use Current Address");
                        showLocationUnavailableDialog();
                    });
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AddressBook", "getCurrentLocation failed", e);
                runOnUiThread(() -> {
                    btnUseCurrent.setEnabled(true);
                    btnUseCurrent.setText("Use Current Address");
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showLocationUnavailableDialog();
                });
            });
    }

    /**
     * Populate address form fields with geocoded data
     */
    private void populateAddressFields(Button btnUseCurrent, EditText etAddressLine1, EditText etAddressLine2,
                                      Spinner spCity, Spinner spState, EditText etPincode,
                                      ArrayAdapter<CharSequence> cityAdapter, ArrayAdapter<CharSequence> stateAdapter,
                                      String formattedAddress, String city, String state, String pincode) {
        // Reset button state
        btnUseCurrent.setEnabled(true);
        btnUseCurrent.setText("Use Current Address");

        if (formattedAddress != null && !formattedAddress.isEmpty()) {
            android.util.Log.d("AddressBook", "Populating fields with: " + formattedAddress);

            // Parse formatted address into address lines
            String[] addressParts = formattedAddress.split(",");
            if (addressParts.length > 0) {
                // First part goes to address line 1
                etAddressLine1.setText(addressParts[0].trim());

                // Second part (if exists) goes to address line 2
                if (addressParts.length > 1) {
                    etAddressLine2.setText(addressParts[1].trim());
                }
            }

            // Set city
            if (city != null && !city.isEmpty()) {
                int cityPos = cityAdapter.getPosition(city);
                if (cityPos >= 0) {
                    spCity.setSelection(cityPos);
                } else {
                    // Try to find partial match
                    for (int i = 0; i < cityAdapter.getCount(); i++) {
                        String item = cityAdapter.getItem(i).toString();
                        if (item.toLowerCase().contains(city.toLowerCase()) ||
                            city.toLowerCase().contains(item.toLowerCase())) {
                            spCity.setSelection(i);
                            break;
                        }
                    }
                }
            }

            // Set state
            if (state != null && !state.isEmpty()) {
                int statePos = stateAdapter.getPosition(state);
                if (statePos >= 0) {
                    spState.setSelection(statePos);
                } else {
                    // Try to find partial match
                    for (int i = 0; i < stateAdapter.getCount(); i++) {
                        String item = stateAdapter.getItem(i).toString();
                        if (item.toLowerCase().contains(state.toLowerCase()) ||
                            state.toLowerCase().contains(item.toLowerCase())) {
                            spState.setSelection(i);
                            break;
                        }
                    }
                }
            }

            // Set pincode
            if (pincode != null && !pincode.isEmpty()) {
                etPincode.setText(pincode);
            }

            Toast.makeText(this, "Address fields populated from current location",
                Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.e("AddressBook", "Geocoding returned no results");
            Toast.makeText(this, "Unable to get address details. Please enter manually.",
                Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show helpful dialog when location is unavailable
     */
    private void showLocationUnavailableDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Location Unavailable")
            .setMessage("Unable to get your current location. Please ensure:\n\n" +
                       "1. Location services are enabled in device settings\n" +
                       "2. GPS is turned on\n" +
                       "3. You're in an area with GPS signal\n" +
                       "4. Google Play Services is up to date\n\n" +
                       "You may need to open Google Maps first to get a GPS fix.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                try {
                    startActivity(new android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open settings", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("OK", null)
            .show();
    }

    private interface GeocodeCallback {
        void onResult(String formattedAddress, String city, String state, String pincode);
    }

    // Inner Adapter Class
    private static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
        private List<Address> addresses;
        private OnAddressClickListener clickListener;
        private OnAddressEditListener editListener;
        private OnAddressDeleteListener deleteListener;

        interface OnAddressClickListener {
            void onAddressClick(Address address);
        }

        interface OnAddressEditListener {
            void onEdit(Address address);
        }

        interface OnAddressDeleteListener {
            void onDelete(Address address);
        }

        public AddressAdapter(List<Address> addresses, OnAddressClickListener clickListener,
                            OnAddressEditListener editListener, OnAddressDeleteListener deleteListener) {
            this.addresses = addresses != null ? addresses : new ArrayList<>();
            this.clickListener = clickListener;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @Override
        public AddressViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_address, parent, false);
            return new AddressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AddressViewHolder holder, int position) {
            if (position < 0 || position >= addresses.size()) return;
            Address address = addresses.get(position);
            holder.bind(address, clickListener, editListener, deleteListener);
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        static class AddressViewHolder extends RecyclerView.ViewHolder {
            TextView tvAddressType, tvFullName, tvPhone, tvAddress, tvDefault;
            ImageView btnEdit, btnDelete;
            CardView cardAddress;

            public AddressViewHolder(View itemView) {
                super(itemView);
                tvAddressType = safeFind(itemView, R.id.tvAddressType);
                tvFullName = safeFind(itemView, R.id.tvFullName);
                tvPhone = safeFind(itemView, R.id.tvPhone);
                tvAddress = safeFind(itemView, R.id.tvAddress);
                tvDefault = safeFind(itemView, R.id.tvDefault);
                btnEdit = safeFind(itemView, R.id.btnEdit);
                btnDelete = safeFind(itemView, R.id.btnDelete);
                cardAddress = safeFind(itemView, R.id.cardAddress);
            }

            private static <T extends View> T safeFind(View root, int id) {
                try {
                    return root.findViewById(id);
                } catch (Exception e) {
                    android.util.Log.w("AddressBook", "Missing view id: " + id, e);
                    return null;
                }
            }

            public void bind(Address address, OnAddressClickListener clickListener,
                           OnAddressEditListener editListener, OnAddressDeleteListener deleteListener) {
                if (address == null) return;

                try {
                    if (tvAddressType != null && address.getAddressType() != null)
                        tvAddressType.setText(address.getAddressType());
                    if (tvFullName != null && address.getFullName() != null)
                        tvFullName.setText(address.getFullName());
                    if (tvPhone != null && address.getPhoneNumber() != null)
                        tvPhone.setText(address.getPhoneNumber());
                    if (tvAddress != null)
                        tvAddress.setText(address.getFormattedAddress());
                    if (tvDefault != null)
                        tvDefault.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

                    if (cardAddress != null && clickListener != null) {
                        cardAddress.setOnClickListener(v -> {
                            try { clickListener.onAddressClick(address); } catch (Exception ignored) {}
                        });
                    }
                    if (btnEdit != null && editListener != null) {
                        btnEdit.setOnClickListener(v -> {
                            try { editListener.onEdit(address); } catch (Exception ignored) {}
                        });
                    }
                    if (btnDelete != null && deleteListener != null) {
                        btnDelete.setOnClickListener(v -> {
                            try { deleteListener.onDelete(address); } catch (Exception ignored) {}
                        });
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddressBook", "Error binding address view", e);
                }
            }
        }
    }
}
