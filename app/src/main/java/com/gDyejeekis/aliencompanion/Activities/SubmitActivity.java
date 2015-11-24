package com.gDyejeekis.aliencompanion.Activities;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.gDyejeekis.aliencompanion.Fragments.ComposeMessageFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitCommentFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitImageFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitLinkFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitTextFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

public class SubmitActivity extends BackNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) getTheme().applyStyle(R.style.selectedTheme_night, true);
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFragment();
    }

    private void setupFragment() {
        if(getFragmentManager().findFragmentByTag("submitFragment") == null) {
            SubmitType submitType = (SubmitType) getIntent().getSerializableExtra("submitType");
            Fragment fragment = null;
            switch (submitType) {
                case link:
                    getSupportActionBar().setTitle("Submit link");
                    fragment = new SubmitLinkFragment();
                    break;
                case self:
                    getSupportActionBar().setTitle("Submit text");
                    fragment = new SubmitTextFragment();
                    break;
                case image:
                    getSupportActionBar().setTitle("Submit image");
                    fragment = new SubmitImageFragment();
                    break;
                case comment:
                    getSupportActionBar().setTitle("Submit reply");
                    fragment = new SubmitCommentFragment();
                    break;
                case message:
                    getSupportActionBar().setTitle("Compose message");
                    fragment = new ComposeMessageFragment();
                    break;
            }
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, fragment, "submitFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit_post, menu);
        return true;
    }

}
