package com.gDyejeekis.aliencompanion.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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

    private CheckBoxPreference syncImages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference clearSynced = findPreference("clearSynced");
        Preference clearCache = findPreference("clearCache");
        Preference viewChangeLog = findPreference("changelog");
        Preference profiles = findPreference("profiles");
        Preference feedback = findPreference("feedback");
        syncImages = (CheckBoxPreference) findPreference("syncImg");
        syncImages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
                    else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
                        return false;
                    }
                }
                return true;
            }
        });
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

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 20) {
            boolean flag;
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                flag = true;
            }
            else {
                flag = false;
            }
            SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putBoolean("syncImg", flag);
            editor.apply();
            syncImages.setChecked(flag);
        }
    }

}
