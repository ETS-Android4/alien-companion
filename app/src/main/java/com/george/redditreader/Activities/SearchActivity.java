package com.george.redditreader.Activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SearchActivity extends SwipeBackActivity {

    public static boolean activityStarted;
    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MainActivity.swipeSetting);

        activityStarted = true;

        setupFragment();
    }

    private void setupFragment() {
        searchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(searchFragment == null) {
            searchFragment = new SearchFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, searchFragment, "listFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isFinishing())
            activityStarted = false;
    }

    public SearchFragment getSearchFragment() {
        return searchFragment;
    }

    public void previous() {
        onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    }

}
