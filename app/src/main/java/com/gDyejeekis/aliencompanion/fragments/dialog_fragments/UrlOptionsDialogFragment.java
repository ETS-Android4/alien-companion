package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

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
            if (LinkUtils.getDomainName(url) == null)
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
                GeneralUtils.copyTextToClipboard(getContext(), "Link address", url);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                GeneralUtils.shareUrl(getContext(), "Share link via..", url);
            }
        });

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

}
