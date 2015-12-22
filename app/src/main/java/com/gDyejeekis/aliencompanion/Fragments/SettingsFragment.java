package com.gDyejeekis.aliencompanion.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

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
        Preference clearCache = findPreference("clearCache");
        clearSynced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        clearSyncedPosts(getActivity());
                    }
                };

                new AlertDialog.Builder(getActivity()).setMessage("Delete all synced posts and comments?").setPositiveButton("Yes", listener)
                        .setNegativeButton("No", null).show();
                return false;
            }
        });

        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GeneralUtils.deleteCache(getActivity());
                ToastUtils.displayShortToast(getActivity(), "Cache cleared");
                return false;
            }
        });
    }

    private void clearSyncedPosts(Context context) {
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        for (File file : files) {
            //Log.d("geo test", file.getName());
            if (!file.getName().equals(MyApplication.SAVED_ACCOUNTS_FILENAME)) file.delete();
        }

        ToastUtils.displayShortToast(context, "Synced posts cleared");
    }

}
