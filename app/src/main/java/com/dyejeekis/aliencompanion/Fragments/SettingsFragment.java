package com.dyejeekis.aliencompanion.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

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
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File dir = getActivity().getFilesDir();
                        File[] files = dir.listFiles();
                        for (File file : files) {
                            //Log.d("geo test", file.getName());
                            if (!file.getName().equals("SavedAccounts")) file.delete();
                        }

                        ToastUtils.displayShortToast(getActivity(), "Synced posts cleared");
                    }
                };

                new AlertDialog.Builder(getActivity()).setMessage("Delete all synced posts and comments?").setPositiveButton("Yes", listener)
                        .setNegativeButton("No", null).show();
                return false;
            }
        });
    }

}
