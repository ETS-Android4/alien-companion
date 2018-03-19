package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
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

import java.io.File;

/**
 * Created by George on 12/4/2016.
 */

public class DebugActivity extends ToolbarActivity {

    public static final String TAG = "DebugActivity";

    public static final String[] URL_TESTS_HARDCODED = {
            "https://www.reddit.com/r/Games/wiki/rules",
            "/u/10-15-19-26-32-34-68",
            "https://www.reddit.com/user/10-15-19-26-32-34-68",
            "https://www.reddit.com/r/reddit.com/",
            "https://www.reddit.com/r/reddit.com",
            "https://www.reddit.com/r/buildapcforme/",
            "https://www.reddit.com/r/buildapcforme",
            "https://www.reddit.com/r/KeepOurNetFree/80mkp0/",
            "https://www.reddit.com/r/KeepOurNetFree/80mkp0",
            "https://www.reddit.com/r/Art/comments/346tdo/10_years_of_progress_learning_to_draw_album_on/",
            "https://www.REDDIT.com/r/funny/COMMENTS/6geuo5/thats_my_chair/",
            "https://www.reddit.com/r/worldnews/comments/826074/young_people_without_family_wealth_are_right_to/dv870al/?sort=controversial&context=2",
            "https://www.reddit.com/r/AnimalsBeingJerks/comments/6g1g78/muffin_trying_to_keep_blueberry_out/",
            "http://gfycat.com/bothcircularcornsnake",
            "http://www.gfycat.com/nippykindlangur",
            "http://www.gfycat.com/brutalsavagerekt"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        initToolbar();
        setupLinkHandlerTest();
        setupUrlTests();
        setupStorageDebug();
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

    private void setupStorageDebug() {
        final Button logInternalBtn = findViewById(R.id.button_log_internal_storage);
        logInternalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = getCacheDir().getParentFile();
                Log.d(TAG, "------------------ Files in " + dir.getAbsolutePath() + " --------------------");
                File[] files = dir.listFiles();
                for (File file : files) {
                    Log.d(TAG, file.getName());
                }
            }
        });
    }
}
