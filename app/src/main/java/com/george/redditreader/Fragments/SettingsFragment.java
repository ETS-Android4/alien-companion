package com.george.redditreader.Fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.george.redditreader.R;

/**
 * Created by George on 8/3/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
