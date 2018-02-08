package com.gDyejeekis.aliencompanion.models;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.util.UUID;

/**
 * Created by George on 1/22/2018.
 */

public class Donation {

    public static final String TAG = "Donation";

    public static final float[] DONATION_AMOUNTS = {0.99f, 1.99f, 2.99f, 3.99f, 4.99f, 5.99f, 6.99f, 7.99f, 8.99f, 9.99f};

    public static final String DONATION_FAILED_MESSAGE = "There was an error processing your donation (you have not been charged)";

    public static final String THANK_YOU_MESSAGE = "Donation received! Thanks for your support :)";

    private final int donationId;
    private long createdAt;
    private String name;
    private String message;
    private float amount;
    private boolean isPublic;

    public Donation(String name, String message, float amount, boolean isPublic) {
        this.donationId = UUID.randomUUID().hashCode();
        this.createdAt = System.currentTimeMillis();
        this.name = (name==null || name.trim().isEmpty()) ? "Anonymous" : name;
        this.message = (message==null || message.trim().isEmpty()) ? null : message;
        this.amount = amount;
        boolean profanity = GeneralUtils.containsProfanity(name) || GeneralUtils.containsProfanity(message);
        this.isPublic = isPublic && !profanity;
    }

    public int getDonationId() {
        return donationId;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasMessage() {
        return message != null;
    }

    public float getAmount() {
        return amount;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
