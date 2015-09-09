package com.george.redditreader.Fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.R;

/**
 * Created by sound on 8/27/2015.
 */
public class AccountOptionsDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        final String accountName = getArguments().getString("accountName");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(accountName).setItems(R.array.accountOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        MainActivity activity = (MainActivity) getActivity();
                        activity.getNavDrawerAdapter().deleteAccount(accountName);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        });
        return builder.create();
    }
}
