package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 5/28/2017.
 */

public class ViewTableDialogFragment extends ScalableDialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_table, container, false);
        WebView webView = (WebView) view.findViewById(R.id.webView_table);
        webView.getSettings().setJavaScriptEnabled(false);

        String tableHtml = getArguments().getString("tableHtml");
        tableHtml = styleTableHtml(tableHtml);
        webView.loadData(tableHtml, "text/html", "UTF-8");

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    private String styleTableHtml(String tableHtml) {
        String textColor = String.format("#%06X", 0xFFFFFF & MyApplication.textPrimaryColor);
        String backgroundColor = MyApplication.nightThemeEnabled ? "#404040" : "#ffffff";
        return "<html><head>"
                + "<style type=\"text/css\">body{color:" + textColor + "; background-color:" + backgroundColor + ";}"
                + "</style></head>"
                + "<body>"
                + tableHtml
                + "</body></html>";
    }

    @Override
    protected void setDialogWidth() {
        int width = Math.round(getResources().getDisplayMetrics().widthPixels * 0.99f);
        Window window = getDialog().getWindow();
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
