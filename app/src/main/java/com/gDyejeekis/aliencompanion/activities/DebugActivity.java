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
import com.gDyejeekis.aliencompanion.utils.HtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.utils.SpanUtils;

/**
 * Created by George on 12/4/2016.
 */

public class DebugActivity extends ToolbarActivity {

    public static final String TAG = "DebugActivity";

    public static final String[] URL_TESTS_HARDCODED = {"https://i.reddituploads.com/5934d0a2442b41618d9db9c27425e285?fit=max&h=1536&w=1536&s=427c55349d61cc17aed8f1d30a81160a",
            "https://i.reddituploads.com/6f8e5f3f3d74460bae59047ecbf19559?fit=max&h=1536&w=1536&s=067b6ffbeac9bb3d3449bf0418bce96f",
            "https://i.reddituploads.com/8b91fc23ba1f41898c13bc3112c2d08a?fit=max&h=1536&w=1536&s=0c9dcd22bbe24037a72c83a5e876d66e",
            "https://i.reddituploads.com/5966b24e42d04943b51f13c1229026d6?fit=max&h=1536&w=1536&s=7f617e10a8cb7ca2ffc26fa02a6133c1"};

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
        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(urlsString, null, new HtmlTagHandler(urlTests.getPaint())));
        stringBuilder = SpanUtils.modifyURLSpan(this, stringBuilder);
        urlTests.setText(stringBuilder);
        urlTests.setMovementMethod(MyLinkMovementMethod.getInstance());
    }
}
