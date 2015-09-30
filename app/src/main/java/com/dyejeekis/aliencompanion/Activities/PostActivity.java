package com.dyejeekis.aliencompanion.Activities;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class PostActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MainActivity.fontStyle, true);
        if(MainActivity.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MainActivity.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MainActivity.swipeSetting);

        setupFragment();
    }

    private void setupFragment() {
        PostFragment postFragment = (PostFragment) getFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(postFragment == null) {
            postFragment = new PostFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, postFragment, "postFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int menuResource;
        if(MainActivity.offlineModeEnabled) menuResource = R.menu.menu_post_offline;
        else menuResource = R.menu.menu_post;
        getMenuInflater().inflate(menuResource, menu);
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

    //@Override
    //public void onBackPressed () {
    //    //MainActivity.commentsLoaded = false;
    //    //MainActivity.showFullCommentsButton = false;
    //    //PostFragment.commentLinkId = null;
    //    super.onBackPressed();
    //}

    //public void next() {
    //    commentsLoaded = false;
    //    finish();
    //    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    //}

    //public void previous() {
    //    onBackPressed();
    //    overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    //}

    //@Override
    //public void onDestroy() {
    //    super.onDestroy();
    //    Log.d("geo debug", "PostActivity onDestroy called");
    //}

}
