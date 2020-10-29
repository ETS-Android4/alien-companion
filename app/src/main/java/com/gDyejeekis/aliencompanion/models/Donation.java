package com.gDyejeekis.aliencompanion.models;

import android.util.Log;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.JsonUtils;
//import com.google.firebase.database.IgnoreExtraProperties;

import org.json.simple.JSONObject;

/**
 * Created by George on 1/22/2018.
 */

//@IgnoreExtraProperties
public class Donation {

    public long createdAt;
    public String name;
    public String message;
    public String orderId;
    public float amount;
    public boolean isPublic;
    public boolean isInappropriate;

    // Default constructor required for calls to DataSnapshot.getValue(Donation.class)
    public Donation() {
    }

    public Donation(long createdAt, String orderId, String name, String message, float amount, boolean isPublic) {
        this.createdAt = createdAt;
        this.orderId = orderId;
        this.name = (name==null || name.trim().isEmpty()) ? null : name;
        this.message = (message==null || message.trim().isEmpty()) ? null : message;
        this.amount = amount;
        this.isPublic = isPublic;
        this.isInappropriate = GeneralUtils.containsProfanity(name) || GeneralUtils.containsProfanity(message);
    }

    public boolean hasMessage() {
        return message != null;
    }

    public boolean showToPublic() {
        return isPublic && !isInappropriate;
    }
}
