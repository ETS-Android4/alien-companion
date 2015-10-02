package com.dyejeekis.aliencompanion.Fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.File;

/**
 * Created by George on 8/3/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference clearSynced = findPreference("clearSynced");
        clearSynced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File dir = getActivity().getFilesDir();
                File[] files = dir.listFiles();
                for(File file : files) {
                    //Log.d("geo test", file.getName());
                    if(!file.getName().equals("SavedAccounts")) file.delete();
                }

                ToastUtils.displayShortToast(getActivity(), "Synced posts cleared");
                return false;
            }
        });
    }

}
