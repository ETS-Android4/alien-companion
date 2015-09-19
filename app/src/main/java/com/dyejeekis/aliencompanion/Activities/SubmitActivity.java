package com.dyejeekis.aliencompanion.Activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.dyejeekis.aliencompanion.Fragments.SubmitCommentFragment;
import com.dyejeekis.aliencompanion.Fragments.SubmitImageFragment;
import com.dyejeekis.aliencompanion.Fragments.SubmitLinkFragment;
import com.dyejeekis.aliencompanion.Fragments.SubmitTextFragment;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.enums.SubmitType;

public class SubmitActivity extends BackNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MainActivity.fontStyle, true);
        if(MainActivity.nightThemeEnabled) getTheme().applyStyle(R.style.selectedTheme_night, true);
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MainActivity.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
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
                    //SubmitLinkFragment linkFragment = new SubmitLinkFragment();
                    //getFragmentManager().beginTransaction().add(R.id.fragmentHolder, linkFragment, "submitFragment").commit();
                    fragment = new SubmitLinkFragment();
                    break;
                case self:
                    getSupportActionBar().setTitle("Submit text");
                    //SubmitTextFragment textFragment = new SubmitTextFragment();
                    //getFragmentManager().beginTransaction().add(R.id.fragmentHolder, textFragment, "submitFragment").commit();
                    fragment = new SubmitTextFragment();
                    break;
                case image:
                    getSupportActionBar().setTitle("Submit image");
                    fragment = new SubmitImageFragment();
                    break;
                case comment:
                    getSupportActionBar().setTitle("Submit reply");
                    //SubmitCommentFragment commentFragment = new SubmitCommentFragment();
                    //getFragmentManager().beginTransaction().add(R.id.fragmentHolder, commentFragment, "submitFragment").commit();
                    fragment = new SubmitCommentFragment();
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
