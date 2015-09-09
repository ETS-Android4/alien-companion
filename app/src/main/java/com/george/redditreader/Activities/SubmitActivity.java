package com.george.redditreader.Activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.george.redditreader.Fragments.SubmitCommentFragment;
import com.george.redditreader.Fragments.SubmitImageFragment;
import com.george.redditreader.Fragments.SubmitLinkFragment;
import com.george.redditreader.Fragments.SubmitTextFragment;
import com.george.redditreader.R;
import com.george.redditreader.enums.SubmitType;

public class SubmitActivity extends BackNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
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
