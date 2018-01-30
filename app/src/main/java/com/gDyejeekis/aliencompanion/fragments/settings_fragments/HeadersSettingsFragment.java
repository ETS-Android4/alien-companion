package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.gDyejeekis.aliencompanion.activities.DonateActivity;
import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.activities.SettingsActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SettingsMenuType;

/**
 * Created by George on 9/10/2016.
 */
public class HeadersSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String TAG = "HeadersFragment";

    private SettingsActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_headers);

        activity = (SettingsActivity) getActivity();

        findPreference("donate").setOnPreferenceClickListener(this);
        findPreference("appearance").setOnPreferenceClickListener(this);
        findPreference("navigation").setOnPreferenceClickListener(this);
        //findPreference("posts").setOnPreferenceClickListener(this);
        //findPreference("comments").setOnPreferenceClickListener(this);
        findPreference("postsAndComments").setOnPreferenceClickListener(this);
        findPreference("sync").setOnPreferenceClickListener(this);
        findPreference("links").setOnPreferenceClickListener(this);
        findPreference("filters").setOnPreferenceClickListener(this);
        findPreference("other").setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SettingsMenuType menuType = null;
        switch (preference.getKey()) {
            case "donate":
                activity.startActivity(new Intent(activity, DonateActivity.class));
                return true;
            case "appearance":
                menuType = SettingsMenuType.appearance;
                break;
            case "navigation":
                menuType = SettingsMenuType.navigation;
                break;
            case "posts":
                menuType = SettingsMenuType.posts;
                break;
            case "comments":
                menuType = SettingsMenuType.comments;
                break;
            case "postsAndComments":
                menuType = SettingsMenuType.postsAndComments;
                break;
            case "sync":
                menuType = SettingsMenuType.sync;
                break;
            case "links":
                menuType = SettingsMenuType.linkHandling;
                break;
            case "filters":
                Intent intent = new Intent(activity, ProfilesActivity.class);
                intent.putExtra(ProfilesActivity.PROFILES_TYPE_EXTRA, ProfilesActivity.FILTER_PROFILES);
                activity.startActivity(intent);
                return true;
            case "other":
                menuType = SettingsMenuType.other;
                break;
        }

        if (menuType!=null) {
            if(activity.isDualPaneActive()) {
                activity.setupOptionsFragment(menuType);
            }
            else {
                Intent intent = new Intent(activity, SettingsActivity.class);
                intent.putExtra("menuType", menuType);
                activity.startActivity(intent);
            }
            return true;
        } else {
            Log.e(TAG, "Invalid menu type");
        }

        return false;
    }

}
