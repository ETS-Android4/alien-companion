package com.george.redditreader.Activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.george.redditreader.Fragments.PostFragment;
import com.george.redditreader.R;


public class PostActivity extends SwipeActivity {

    public static boolean commentsLoaded;
    public static boolean showFullComments;
    //public static boolean loadFromList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //loadFromList = (getIntent().getSerializableExtra("postUrl") == null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
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

        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed () {
        commentsLoaded = false;
        showFullComments = false;
        PostFragment.commentLinkId = null;
        super.onBackPressed();
    }

    //public void next() {
    //    commentsLoaded = false;
    //    finish();
    //    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    //}

    public void previous() {
        onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    }

}
