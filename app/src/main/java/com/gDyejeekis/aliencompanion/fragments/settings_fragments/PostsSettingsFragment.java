package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.PostViewType;

/**
 * Created by George on 9/10/2016.
 */
public class PostsSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.posts_preferences);

        Preference defaultView = findPreference("defaultView");
        defaultView.setSummary(PostViewType.getName(MyApplication.currentPostListView));
        defaultView.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals("defaultView")) {
            preference.setSummary(PostViewType.getName(Integer.parseInt((String)newValue)));
            return true;
        }
        return false;
    }
}
