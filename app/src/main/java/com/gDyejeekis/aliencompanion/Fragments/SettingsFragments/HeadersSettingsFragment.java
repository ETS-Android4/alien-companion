package com.gDyejeekis.aliencompanion.Fragments.SettingsFragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.Activities.SettingsActivity;
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

        findPreference("appearance").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.appearance);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("navigation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.navigation);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("posts").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.posts);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("comments").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.comments);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("sync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.sync);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("links").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.linkHandling);
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("other").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("menuType", SettingsMenuType.other);
                getActivity().startActivity(intent);
                return false;
            }
        });
    }
}
