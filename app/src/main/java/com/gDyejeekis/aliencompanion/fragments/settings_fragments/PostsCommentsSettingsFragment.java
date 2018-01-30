package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.PostViewType;

/**
 * Created by George on 1/30/2018.
 */

public class PostsCommentsSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.posts_comments_preferences);

        Preference defaultView = findPreference("defaultView");
        defaultView.setSummary(PostViewType.getName(MyApplication.currentPostListView));
        defaultView.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals("defaultView")) {
            preference.setSummary(PostViewType.getName(Integer.parseInt((String)newValue)));
            return true;
        }
        return false;
    }
}
