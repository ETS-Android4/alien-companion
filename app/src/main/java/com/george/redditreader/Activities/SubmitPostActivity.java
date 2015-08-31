package com.george.redditreader.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.george.redditreader.Fragments.SubmitLinkFragment;
import com.george.redditreader.Fragments.SubmitTextFragment;
import com.george.redditreader.R;
import com.george.redditreader.enums.SubmitPostType;

public class SubmitPostActivity extends BackNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFragment();
    }

    private void setupFragment() {
        if(getFragmentManager().findFragmentByTag("submitFragment") == null) {
            SubmitPostType postType = (SubmitPostType) getIntent().getSerializableExtra("postType");
            switch (postType) {
                case link:
                    getSupportActionBar().setTitle("Submit link");
                    SubmitLinkFragment linkFragment = new SubmitLinkFragment();
                    getFragmentManager().beginTransaction().add(R.id.fragmentHolder, linkFragment, "submitFragment").commit();
                    break;
                case self:
                    getSupportActionBar().setTitle("Submit text");
                    SubmitTextFragment textFragment = new SubmitTextFragment();
                    getFragmentManager().beginTransaction().add(R.id.fragmentHolder, textFragment, "submitFragment").commit();
                    break;
                case image:
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit_post, menu);
        return true;
    }

}
