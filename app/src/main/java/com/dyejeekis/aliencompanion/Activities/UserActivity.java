package com.dyejeekis.aliencompanion.Activities;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dyejeekis.aliencompanion.Fragments.UserFragment;
import com.dyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserActivity extends SwipeBackActivity {

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

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MainActivity.swipeSetting);

        setupFragment();
    }

    private void setupFragment() {
        UserFragment userFragment = (UserFragment) getFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(userFragment == null) {
            userFragment = new UserFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, userFragment, "listFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
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

    public void previous() {
        onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
    }
}
