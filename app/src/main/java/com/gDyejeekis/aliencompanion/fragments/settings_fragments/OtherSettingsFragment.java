package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.MoveAppDataDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
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
        messageCheckInterval.setSummary(getTimeIntervalString(MyApplication.messageCheckInterval));
        messageCheckInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                MyApplication.messageCheckInterval = Integer.valueOf((String) o);
                preference.setSummary(getTimeIntervalString(MyApplication.messageCheckInterval));
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
                return false;
            }
        });

        Preference viewChangeLog = findPreference("changelog");
        viewChangeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GeneralUtils.showChangeLog(getActivity());
                return false;
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
                return false;
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
                        MyApplication.preferExternalStorage = moveToExternal;
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
                return false;
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

    private String getTimeIntervalString(int minutes) {
        if(minutes == -1) {
            return "Never";
        }
        else if(minutes == 60) {
            return "Every hour";
        }
        else if(minutes > 60) {
            return "Every " + minutes/60 + " hours";
        }
        return "Every " + minutes + " minutes";
    }

}
