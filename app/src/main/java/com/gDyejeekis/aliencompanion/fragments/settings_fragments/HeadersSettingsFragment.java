package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.activities.DonateActivity;
import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.activities.SettingsActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SettingsMenuType;

/**
 * Created by George on 9/10/2016.
 */
public class HeadersSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_headers);

        final SettingsActivity activity = (SettingsActivity) getActivity();

        findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startActivity(new Intent(getActivity(), DonateActivity.class));
                return true;
            }
        });

        findPreference("appearance").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new AppearanceSettingsFragment(), SettingsMenuType.appearance);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.appearance);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("navigation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new NavigationSettingsFragment(), SettingsMenuType.navigation);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.navigation);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("posts").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new PostsSettingsFragment(), SettingsMenuType.posts);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.posts);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("comments").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new CommentsSettingsFragment(), SettingsMenuType.comments);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.comments);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("sync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new SyncSettingsFragment(), SettingsMenuType.sync);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.sync);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("links").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new LinkHandlingSettingsFragment(), SettingsMenuType.linkHandling);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.linkHandling);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });

        findPreference("filters").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ProfilesActivity.class);
                intent.putExtra(ProfilesActivity.PROFILES_TYPE_EXTRA, ProfilesActivity.FILTER_PROFILES);
                getActivity().startActivity(intent);
                return true;
            }
        });

        findPreference("other").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(activity.isDualPaneActive()) {
                    activity.setupOptionsFragment(new OtherSettingsFragment(), SettingsMenuType.other);
                }
                else {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra("menuType", SettingsMenuType.other);
                    getActivity().startActivity(intent);
                }
                return true;
            }
        });
    }
}
