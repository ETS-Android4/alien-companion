package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class BrowserActivity extends SwipeBackActivity {

    public boolean loadFromCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        if(MyApplication.currentColor==0) MyApplication.setThemeRelatedFields();
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int resource = (loadFromCache) ? R.menu.menu_browser_alt : R.menu.menu_browser;
        getMenuInflater().inflate(resource, menu);

        if(getIntent().getSerializableExtra("post") == null)
            menu.findItem(R.id.action_comments).setVisible(false);
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

}
