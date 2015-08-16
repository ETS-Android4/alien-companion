package com.george.redditreader.Fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.george.redditreader.R;
import com.george.redditreader.api.utils.ApiEndpointUtils;

/**
 * Created by George on 8/16/2015.
 */
public class UrlOptionsDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        final String url = getArguments().getString("url");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(url).setItems(R.array.urlOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        break;
                    case 1:
                        Intent intent;
                        try {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiEndpointUtils.REDDIT_BASE_URL + url));
                            getActivity().startActivity(intent);
                        }
                        break;
                    case 2:
                        break;
                }
            }
        });
        return builder.create();
    }
}
