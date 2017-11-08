package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.MoveAppDataDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by George on 9/10/2016.
 */
public class OtherSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_preferences);

        Preference messageCheckInterval = findPreference("messageCheckInterval");
        messageCheckInterval.setSummary(ConvertUtils.getTimeIntervalString(MyApplication.messageCheckInterval));
        messageCheckInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                MyApplication.messageCheckInterval = Integer.valueOf((String) o);
                preference.setSummary(ConvertUtils.getTimeIntervalString(MyApplication.messageCheckInterval));
                MyApplication.scheduleMessageCheckService(getActivity());
                return true;
            }
        });

        Preference clearCache = findPreference("clearCache");
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                Bundle args = new Bundle();
                args.putString("message", "Clearing app cache");
                dialogFragment.setArguments(args);
                dialogFragment.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), "dialog");

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        CleaningUtils.clearCache(getActivity());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        dialogFragment.dismiss();
                        ToastUtils.showToast(getActivity(), "Cache cleared");
                    }
                }.execute();
                return true;
            }
        });

        Preference deviceSettings = findPreference("deviceSettings");
        deviceSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myAppSettings, 46414);
                return true;
            }
        });

        Preference viewChangeLog = findPreference("changelog");
        viewChangeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GeneralUtils.showChangeLog(getActivity());
                return true;
            }
        });

        Preference feedback = findPreference("feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "alien.companion@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Alien Companion app feedback");
                //emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                getActivity().startActivity(emailIntent);
                return true;
            }
        });

        Preference preferExternal = findPreference("prefExternal");
        preferExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean moveToExternal = (boolean) newValue;
                if(!StorageUtils.isExternalStorageAvailable(getActivity())) {
                    if(moveToExternal) {
                        ToastUtils.showToast(getActivity(), "External storage unavailable");
                        return false;
                    }
                    else {
                        MyApplication.preferExternalStorage = false;
                        return true;
                    }
                }
                MoveAppDataDialogFragment dialog = new MoveAppDataDialogFragment();
                Bundle args = new Bundle();
                args.putBoolean("external", moveToExternal);
                dialog.setArguments(args);
                dialog.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), "dialog");
                MyApplication.preferExternalStorage = moveToExternal;
                return true;
            }
        });

        Preference pendingActions = findPreference("offlineActions");
        pendingActions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), PendingUserActionsActivity.class);
                getActivity().startActivity(intent);
                return true;
            }
        });

        Preference autoExecutePendingActions = findPreference("autoOfflineActions");
        autoExecutePendingActions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MyApplication.autoExecuteOfflineActions = (boolean) newValue;
                MyApplication.scheduleOfflineActionsService(getActivity());
                return true;
            }
        });
    }

}
