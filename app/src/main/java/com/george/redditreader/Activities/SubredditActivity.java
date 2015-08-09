package com.george.redditreader.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.R;

public class SubredditActivity extends SwipeActivity {

    private PostListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFragment();
    }

    private void setupFragment() {
        listFragment = (PostListFragment) getFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(listFragment == null) {
            listFragment = new PostListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, listFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    public void previous() {
        onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    }
}
