package com.grocerygo.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocerygo.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class to populate mock delivery partners in Firestore
 */
public class DeliveryPartnerPopulator {
    private static final String TAG = "DeliveryPartnerPopulator";
    private final FirebaseFirestore db;
    private final Random random;

    public DeliveryPartnerPopulator() {
        this.db = FirebaseFirestore.getInstance();
        this.random = new Random();
    }

    /**
     * Create and populate mock delivery partners
     * @return Task that completes when all partners are added
     */
    public Task<Void> populateDeliveryPartners() {
        List<User> deliveryPartners = createMockDeliveryPartners();
        List<Task<Void>> tasks = new ArrayList<>();

        for (User partner : deliveryPartners) {
            Task<Void> task = db.collection("users")
                    .document(partner.getUserId())
                    .set(partner)
                    .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Added delivery partner: " + partner.getName()))
                    .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to add partner: " + partner.getName(), e));

            tasks.add(task);
        }

        return Tasks.whenAll(tasks);
    }

    /**
     * Create list of mock delivery partners
     */
    private List<User> createMockDeliveryPartners() {
        List<User> partners = new ArrayList<>();

        // Partner 1
        User partner1 = new User();
        partner1.setUserId("DP001");
        partner1.setName("Ravi Kumar");
        partner1.setPhone("+91-9876543210");
        partner1.setEmail("ravi.kumar@grocerygo.com");
        partner1.setRole("delivery");
        partners.add(partner1);

        // Partner 2
        User partner2 = new User();
        partner2.setUserId("DP002");
        partner2.setName("Amit Sharma");
        partner2.setPhone("+91-9876543211");
        partner2.setEmail("amit.sharma@grocerygo.com");
        partner2.setRole("delivery");
        partners.add(partner2);

        // Partner 3
        User partner3 = new User();
        partner3.setUserId("DP003");
        partner3.setName("Priya Singh");
        partner3.setPhone("+91-9876543212");
        partner3.setEmail("priya.singh@grocerygo.com");
        partner3.setRole("delivery");
        partners.add(partner3);

        // Partner 4
        User partner4 = new User();
        partner4.setUserId("DP004");
        partner4.setName("Rajesh Verma");
        partner4.setPhone("+91-9876543213");
        partner4.setEmail("rajesh.verma@grocerygo.com");
        partner4.setRole("delivery");
        partners.add(partner4);

        // Partner 5
        User partner5 = new User();
        partner5.setUserId("DP005");
        partner5.setName("Sneha Patel");
        partner5.setPhone("+91-9876543214");
        partner5.setEmail("sneha.patel@grocerygo.com");
        partner5.setRole("delivery");
        partners.add(partner5);

        // Partner 6
        User partner6 = new User();
        partner6.setUserId("DP006");
        partner6.setName("Vikram Reddy");
        partner6.setPhone("+91-9876543215");
        partner6.setEmail("vikram.reddy@grocerygo.com");
        partner6.setRole("delivery");
        partners.add(partner6);

        // Partner 7
        User partner7 = new User();
        partner7.setUserId("DP007");
        partner7.setName("Anjali Gupta");
        partner7.setPhone("+91-9876543216");
        partner7.setEmail("anjali.gupta@grocerygo.com");
        partner7.setRole("delivery");
        partners.add(partner7);

        // Partner 8
        User partner8 = new User();
        partner8.setUserId("DP008");
        partner8.setName("Suresh Kumar");
        partner8.setPhone("+91-9876543217");
        partner8.setEmail("suresh.kumar@grocerygo.com");
        partner8.setRole("delivery");
        partners.add(partner8);

        return partners;
    }

    /**
     * Delete all delivery partners (for testing purposes)
     */
    public Task<Void> clearDeliveryPartners() {
        return db.collection("users")
                .whereEqualTo("role", "delivery")
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Task<Void>> deleteTasks = new ArrayList<>();
                        task.getResult().forEach(document -> {
                            deleteTasks.add(document.getReference().delete());
                        });
                        return Tasks.whenAll(deleteTasks);
                    }
                    return Tasks.forResult(null);
                });
    }
}

