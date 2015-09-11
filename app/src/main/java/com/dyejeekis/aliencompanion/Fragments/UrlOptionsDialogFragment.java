package com.dyejeekis.aliencompanion.Fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.utils.ApiEndpointUtils;

import java.net.URISyntaxException;

/**
 * Created by George on 8/16/2015.
 */
public class UrlOptionsDialogFragment extends DialogFragment {

    private String url;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        url = getArguments().getString("url");
        try {
            if (ConvertUtils.getDomainName(url) == null)
                url = ApiEndpointUtils.REDDIT_BASE_URL + url;
        } catch (URISyntaxException e) {}
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(url).setItems(R.array.urlOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1: //copy link address
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link address", url);
                        clipboard.setPrimaryClip(clip);
                        break;
                    case 0: //open in browser
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        getActivity().startActivity(intent);
                        break;
                    case 2: //share
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, url);
                        intent.setType("text/plain");
                        getActivity().startActivity(Intent.createChooser(intent, "Share link to.."));
                        break;
                }
            }
        });
        return builder.create();
    }
}
