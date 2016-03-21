package com.gDyejeekis.aliencompanion.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import com.gDyejeekis.aliencompanion.Activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ChangeLogDialogFragment;
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
        Preference viewChangeLog = findPreference("changelog");
        Preference profiles = findPreference("profiles");
        Preference feedback = findPreference("feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "alien.companion@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Alien Companion app feedback");
                //emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                getActivity().startActivity(emailIntent);
                return false;
            }
        });
        profiles.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startActivity(new Intent(getActivity(), SyncProfilesActivity.class));
                return false;
            }
        });
        clearSynced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GeneralUtils.clearSyncedPosts(getActivity());
                        GeneralUtils.clearSyncedImages(getActivity());
                        ToastUtils.displayShortToast(getActivity(), "All synced posts cleared");
                    }
                };

                new AlertDialog.Builder(getActivity()).setMessage("Delete all synced posts, comments and images?").setPositiveButton("Yes", listener)
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
        viewChangeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GeneralUtils.showChangeLog(getActivity());
                return false;
            }
        });
    }

}
