package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;

/**
 * Created by George on 9/10/2016.
 */
public class NavigationSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_preferences);

        Preference autoHideToolbar = findPreference("autoHideToolbar");
        Preference fabPostNav = findPreference("postNav");
        Preference autoHidePostFab = findPreference("autoHidePostNav");
        Preference fabCommentNav = findPreference("commentNav");
        Preference autoHideCommentNav = findPreference("autoHideCommentNav");
        autoHideToolbar.setOnPreferenceChangeListener(this);
        fabPostNav.setOnPreferenceChangeListener(this);
        autoHidePostFab.setOnPreferenceChangeListener(this);
        fabCommentNav.setOnPreferenceChangeListener(this);
        autoHideCommentNav.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key =  preference.getKey();
        if(key.equals("postNav") || key.equals("autoHidePostNav")) {
            MyApplication.fabPostNavChanged = true;
            return true;
        }
        else if(key.equals("commentNav") || key.equals("autoHideCommentNav")) {
            MyApplication.fabCommentNavChanged = true;
            return true;
        }
        else if (key.equals("autoHideToolbar")) {
            MainActivity.notifyToolbarAutohideChanged = true;
            return true;
        }
        return false;
    }
}
