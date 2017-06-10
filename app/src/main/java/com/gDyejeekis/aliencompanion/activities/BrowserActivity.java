package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.fragments.ArticleFragment;
import com.gDyejeekis.aliencompanion.fragments.BrowserFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class BrowserActivity extends SwipeBackActivity {

    public static final String TAG = "BrowserActivity";

    public boolean loadFromCache;

    public boolean loadSyncedArticle;

    public boolean canGoBack, canGoForward;

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.layout_fragment_holder);
        if(fragment instanceof BrowserFragment && ((BrowserFragment) fragment).webView.canGoBack()) {
            ((BrowserFragment) fragment).goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        initToolbar();

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        setupMainFragment();
    }

    private void setupMainFragment() {
        Submission post = (Submission) getIntent().getSerializableExtra("post");
        loadSyncedArticle = (MyApplication.offlineModeEnabled && post.hasSyncedArticle);
        Fragment fragment = (loadSyncedArticle) ? new ArticleFragment() : new BrowserFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_holder, fragment).commitAllowingStateLoss();
    }

    public void loadSyncedArticle() {
        loadFromCache = false;
        loadSyncedArticle = true;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction().replace(R.id.layout_fragment_holder, new ArticleFragment()).commitAllowingStateLoss();
    }

    public void loadOriginalPage() {
        loadFromCache = false;
        loadSyncedArticle = false;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction().replace(R.id.layout_fragment_holder, new BrowserFragment()).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        if(loadSyncedArticle || loadFromCache) {
            menu.findItem(R.id.action_load_cache).setTitle("Load live version");
        }

        MenuItem goBack = menu.findItem(R.id.action_back);
        goBack.setVisible(!loadSyncedArticle);
        goBack.setEnabled(canGoBack);
        goBack.setIcon(canGoBack ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_white_disabled_24dp);

        MenuItem goForward = menu.findItem(R.id.action_forward);
        goForward.setVisible(!loadSyncedArticle);
        goForward.setEnabled(canGoForward);
        goForward.setIcon(canGoForward ? R.drawable.ic_arrow_forward_white_24dp : R.drawable.ic_arrow_forward_white_disabled_24dp);

        MenuItem refresh = menu.findItem(R.id.action_refresh);
        refresh.setVisible(!loadSyncedArticle);

        if(getIntent().getSerializableExtra("post") == null) {
            menu.findItem(R.id.action_comments).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        else if(item.getItemId() == R.id.action_comments) {
            MainActivity.dualPaneActive = false; //set to false to open comments in a new activity
            Intent intent = new Intent(this, PostActivity.class);
            Submission post = (Submission) getIntent().getSerializableExtra("post");
            intent.putExtra("post", post);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }
}
