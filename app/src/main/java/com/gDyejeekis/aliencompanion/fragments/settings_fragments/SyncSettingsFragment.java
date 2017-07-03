package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;

import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by George on 9/10/2016.
 */
public class SyncSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_preferences);

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
                                CleaningUtils.clearAllSyncedData(getActivity());
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

                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyAlertDialogStyle)).setMessage("Delete all synced posts, comments, media and articles?").setPositiveButton("Yes", listener)
                        .setNegativeButton("No", null).show();
                return true;
            }
        });

        Preference profiles = findPreference("profiles");
        profiles.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ProfilesActivity.class);
                intent.putExtra(ProfilesActivity.PROFILES_TYPE_EXTRA, ProfilesActivity.SYNC_PROFILES);
                getActivity().startActivity(intent);
                return true;
            }
        });

    }

}
