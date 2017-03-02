package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;

/**
 * Created by George on 12/4/2016.
 */

public class DebugActivity extends ToolbarActivity {

    public static final String TAG = "DebugActivity";

    public static final int MENU_ITEM_ID = TAG.hashCode();

    public static final String[] URL_TESTS_HARDCODED = {"http://a.pomf.se/njqqwq.webm", "http://web.archive.org/web/20150612051901/http://a.pomf.se/dwssfz.webm", "http://a.pomf.se/dwssfz.webm"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        initToolbar();
        setupLinkHandlerTest();
        setupUrlTests();
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    private void setupLinkHandlerTest() {
        final EditText urlField = (EditText) findViewById(R.id.editText_test_url);
        Button button = (Button) findViewById(R.id.button_handle_url);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlField.selectAll();
                String url = urlField.getText().toString();
                LinkHandler linkHandler = new LinkHandler(v.getContext(), url);
                linkHandler.handleIt();
            }
        });
    }

    private void setupUrlTests() {
        final TextView urlTests = (TextView) findViewById(R.id.textView_url_tests);
        String urlsString = "";
        for(int i=0;i<URL_TESTS_HARDCODED.length;i++) {
            urlsString += "<a href=\""+ URL_TESTS_HARDCODED[i] + "\">" + URL_TESTS_HARDCODED[i] + "</a>";
            urlsString += "<br/><br/>";
        }
        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(urlsString, null, new MyHtmlTagHandler()));
        stringBuilder = ConvertUtils.modifyURLSpan(this, stringBuilder);
        urlTests.setText(stringBuilder);
        urlTests.setMovementMethod(MyLinkMovementMethod.getInstance());
    }
}
