package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;

import java.net.URISyntaxException;

/**
 * Created by George on 8/16/2015.
 */
public class UrlOptionsDialogFragment extends ScalableDialogFragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_url_options, container, false);
        TextView title = (TextView) view.findViewById(R.id.textView_title);
        title.setText(url);
        TextView openBrowser = (TextView) view.findViewById(R.id.textView_openBrowser);
        TextView copyLink = (TextView) view.findViewById(R.id.textView_copyLink);
        TextView share = (TextView) view.findViewById(R.id.textView_share);

        openBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getActivity().startActivity(intent);
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Link address", url);
                clipboard.setPrimaryClip(clip);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");
                getActivity().startActivity(Intent.createChooser(intent, "Share link to.."));
            }
        });

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    //@Override
    //public void onResume() {
    //    super.onResume();
    //    setDialogWidth();
    //}
//
    //@Override
    //public void onConfigurationChanged(Configuration newConfig) {
    //    super.onConfigurationChanged(newConfig);
    //    setDialogWidth();
    //}
//
    //private void setDialogWidth() {
    //    Window window = getDialog().getWindow();
    //    int width = 6 * getResources().getDisplayMetrics().widthPixels / 7;
    //    window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    //}

    //@Override
    //public Dialog onCreateDialog(Bundle bundle) {
    //    Context context = (MainActivity.nightThemeEnabled) ? new ContextThemeWrapper(getActivity(), R.style.AlertDialogDark) : getActivity();
    //    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    //    builder.setTitle(url).setItems(R.array.urlOptions, new DialogInterface.OnClickListener() {
    //        @Override
    //        public void onClick(DialogInterface dialog, int which) {
    //            switch (which) {
    //                case 1: //copy link address
    //                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    //                    ClipData clip = ClipData.newPlainText("Link address", url);
    //                    clipboard.setPrimaryClip(clip);
    //                    break;
    //                case 0: //open in browser
    //                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    //                    getActivity().startActivity(intent);
    //                    break;
    //                case 2: //share
    //                    intent = new Intent();
    //                    intent.setAction(Intent.ACTION_SEND);
    //                    intent.putExtra(Intent.EXTRA_TEXT, url);
    //                    intent.setType("text/plain");
    //                    getActivity().startActivity(Intent.createChooser(intent, "Share link to.."));
    //                    break;
    //            }
    //        }
    //    });
    //    return builder.create();
    //}
}
