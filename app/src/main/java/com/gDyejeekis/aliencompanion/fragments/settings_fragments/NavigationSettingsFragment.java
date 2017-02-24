package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 9/10/2016.
 */
public class NavigationSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_preferences);

        Preference fabPostNav = findPreference("postNav");
        Preference autoHidePostFab = findPreference("autoHidePostNav");
        fabPostNav.setOnPreferenceChangeListener(this);
        autoHidePostFab.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals("postNav")) {
            MyApplication.fabPostNavChanged = true;
            return true;
        }
        else if(preference.getKey().equals("autoHidePostNav")) {
            MyApplication.fabPostNavChanged = true;
            return true;
        }
        return false;
    }
}
