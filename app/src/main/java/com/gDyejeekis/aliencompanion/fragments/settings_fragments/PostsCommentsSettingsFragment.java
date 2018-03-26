package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.OverEighteenDialogFragment;

/**
 * Created by George on 1/30/2018.
 */

public class PostsCommentsSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.posts_comments_preferences);

        Preference nsfwPosts = findPreference("showNsfwPosts");
        nsfwPosts.setOnPreferenceChangeListener(this);

        Preference nsfwPreviews = findPreference("showNsfwPreviews");
        nsfwPreviews.setOnPreferenceChangeListener(this);

        Preference defaultView = findPreference("defaultView");
        defaultView.setSummary(PostViewType.getName(MyApplication.currentPostListView));
        defaultView.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "defaultView":
                preference.setSummary(PostViewType.getName(Integer.parseInt((String)newValue)));
                return true;
            case "showNsfwPosts":
            case "showNsfwPreviews":
                if ((boolean) newValue && !MyApplication.userOver18) {
                    OverEighteenDialogFragment.showDialog((AppCompatActivity) getActivity());
                    return false;
                }
                return true;
        }
        return false;
    }

}
