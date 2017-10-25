package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.res.TypedArray;
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
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;

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
        tableHtml = tableHtml.replace("£", "&pound;").replace("€", "&euro;");
        tableHtml = styleTableHtml(tableHtml);
        webView.loadData(tableHtml, "text/html", "UTF-8");

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    private String styleTableHtml(String tableHtml) {
        String textColor = ConvertUtils.intColorToHex(MyApplication.textPrimaryColor);
        String textSize = getHtmlTableTextSize();
        String borderColor = ConvertUtils.intColorToHex(MyApplication.textSecondaryColor);
        String backgroundColor = MyApplication.nightThemeEnabled ? "#404040" : "#ffffff";
        return "<html><head>"
                + "<style type=\"text/css\">"
                + "body {color: " + textColor + "; background-color: " + backgroundColor + ";}"
                + "table {border-collapse: collapse; font-size: " + textSize + ";}"
                + "th, td {border: 1px solid" + borderColor + "; padding: 4px;}"
                + "</style></head>"
                + "<body>"
                + tableHtml
                + "</body></html>";
    }

    private String getHtmlTableTextSize() {
        int[] attr = {R.attr.font_small};
        TypedArray ta = getContext().obtainStyledAttributes(MyApplication.fontStyle, attr);
        String fontAttr = ta.getString(0);
        fontAttr = fontAttr.replace("sp", "");
        ta.recycle();
        int size = ConvertUtils.convertSpToPixels(Float.valueOf(fontAttr)/2, getContext());
        return size + "px";
    }

    @Override
    protected void setDialogWidth() {
        int width = Math.round(getResources().getDisplayMetrics().widthPixels * 0.99f);
        Window window = getDialog().getWindow();
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
