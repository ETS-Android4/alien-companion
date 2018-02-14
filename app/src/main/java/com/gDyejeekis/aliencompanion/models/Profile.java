package com.gDyejeekis.aliencompanion.models;

import android.content.Context;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.models.filters.FilterProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.ProfileListAdapter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by George on 6/18/2017.
 */

public abstract class Profile implements Serializable {

    public static final String TAG = "Profile";

    protected final String profileId;
    protected String name;

    public Profile(String name) {
        profileId = UUID.randomUUID().toString();
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return this.getClass().isInstance(o) && ((Profile) o).getProfileId().equals(this.profileId);
    }

    public static Profile getProfileById(String id, File file) {
        try {
            List<Profile> profiles = (List<Profile>) GeneralUtils.readObjectFromFile(file);
            for(Profile profile : profiles) {
                if(profile.getProfileId().equals(id)) {
                    return profile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getViewType() {
        return ProfileListAdapter.VIEW_TYPE_PROFILE_ITEM;
    }

    public String getFilename() {
        if(this instanceof SyncProfile) {
            return MyApplication.SYNC_PROFILES_FILENAME;
        }
        if(this instanceof FilterProfile) {
            return MyApplication.FILTER_PROFILES_FILENAME;
        }
        return "";
    }

    public boolean save(Context context, boolean newProfile) {
        try {
            File file = new File(context.getFilesDir(), getFilename());
            List<Profile> profiles;
            try {
                profiles  = (List<Profile>) GeneralUtils.readObjectFromFile(file);
            } catch (Exception e) {
                profiles = new ArrayList<>();
            }

            if(newProfile) {
                profiles.add(this);
            }
            else {
                int index = -1;
                for (Profile profile : profiles) {
                    if (profile.getProfileId().equals(this.profileId)) {
                        index = profiles.indexOf(profile);
                        break;
                    }
                }
                profiles.set(index, this);
            }
            GeneralUtils.writeObjectToFile(profiles, file);

            return true;
        } catch (Exception e) {
            ToastUtils.showToast(context, "Error saving profile");
            Log.e(TAG, "Error saving profile " + name + " (id: " + profileId + ")");
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Context context) {
        try {
            File file = new File(context.getFilesDir(), getFilename());
            List<Profile> profiles = (List<Profile>) GeneralUtils.readObjectFromFile(file);
            boolean removed = profiles.remove(this);
            if(!removed) {
                throw new RuntimeException("Profile not found");
            }
            GeneralUtils.writeObjectToFile(profiles, file);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting profile " + name + " (id: " + profileId + ")");
            e.printStackTrace();
        }
        return false;
    }

}
