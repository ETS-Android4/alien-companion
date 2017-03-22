package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by George on 9/10/2016.
 */
public class SyncSettingsFragment extends PreferenceFragment {

    private CheckBoxPreference syncImages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_preferences);

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

        Preference clearSynced = findPreference("clearSynced");
        clearSynced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("message", "Clearing all synced data");
                        dialogFragment.setArguments(args);
                        dialogFragment.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), "dialog");

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                CleaningUtils.clearSyncedPosts(getActivity());
                                CleaningUtils.clearSyncedImages(getActivity());
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialogFragment.dismiss();
                                ToastUtils.showToast(getActivity(), "All synced data cleared");
                            }
                        }.execute();
                    }
                };
                //Log.d(GeneralUtils.TAG, "Remaining local app files BEFORE delete:");
                //GeneralUtils.listFilesInDir(getActivity().getFilesDir());

                new AlertDialog.Builder(getActivity()).setMessage("Delete all synced posts, comments, images and articles?").setPositiveButton("Yes", listener)
                        .setNegativeButton("No", null).show();
                return false;
            }
        });

        Preference profiles = findPreference("profiles");
        profiles.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startActivity(new Intent(getActivity(), SyncProfilesActivity.class));
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
