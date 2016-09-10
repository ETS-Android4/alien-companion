package com.gDyejeekis.aliencompanion.Fragments.SettingsFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ChangeLogDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.MoveAppDataDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.StorageUtils;
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
        Preference feedback = findPreference("feedback");
        Preference profiles = findPreference("profiles");

        Preference preferExternal = findPreference("prefExternal");
        preferExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean moveToExternal = (boolean) newValue;
                if(!StorageUtils.isExternalStorageAvailable(getActivity())) {
                    if(moveToExternal) {
                        ToastUtils.displayShortToast(getActivity(), "External storage unavailable");
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

        //Preference pendingActionsInterval = findPreference("offlineActionsInterval");
        //pendingActionsInterval.setSummary(getTimeIntervalString(MyApplication.offlineActionsInterval));
        //pendingActionsInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //    @Override
        //    public boolean onPreferenceChange(Preference preference, Object o) {
        //        MyApplication.offlineActionsInterval = Integer.valueOf((String) o);
        //        preference.setSummary(getTimeIntervalString(MyApplication.offlineActionsInterval));
        //        MyApplication.scheduleOfflineActionsService(getActivity());
        //        return true;
        //    }
        //});
        Preference autoExecutePendingActions = findPreference("autoOfflineActions");
        autoExecutePendingActions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MyApplication.autoExecuteOfflineActions = (boolean) newValue;
                MyApplication.scheduleOfflineActionsService(getActivity());
                return true;
            }
        });

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
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("message", "Clearing all synced data");
                        dialogFragment.setArguments(args);
                        dialogFragment.show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), "dialog");

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                GeneralUtils.clearSyncedPosts(getActivity());
                                GeneralUtils.clearSyncedImages(getActivity());
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialogFragment.dismiss();
                                ToastUtils.displayShortToast(getActivity(), "All synced data cleared");
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
                        GeneralUtils.deleteCache(getActivity());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        dialogFragment.dismiss();
                        ToastUtils.displayShortToast(getActivity(), "Cache cleared");
                    }
                }.execute();
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

    private String getTimeIntervalString(int minutes) {
        if(minutes == -1) {
            return "Disabled";
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
