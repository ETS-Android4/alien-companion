package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by George on 1/3/2016.
 */
public class ImgurActivity extends SwipeBackActivity {

    private Submission post;
    private String url;

    @Override
    public void onCreate(Bundle bundle) {
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(bundle);
        setContentView(R.layout.activity_imgur);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        post = (Submission) getIntent().getSerializableExtra("post");
        url = getIntent().getStringExtra("url");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        if(post == null)
            menu.findItem(R.id.action_comments).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_comments:
                //action here
                return true;
            case R.id.action_refresh:
                //action here
                return true;
            case R.id.action_open_browser:
                //action here
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
