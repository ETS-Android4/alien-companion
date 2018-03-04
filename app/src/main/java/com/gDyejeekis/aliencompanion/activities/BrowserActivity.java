package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.fragments.ArticleFragment;
import com.gDyejeekis.aliencompanion.fragments.BrowserFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;

import java.io.File;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class BrowserActivity extends SwipeBackActivity {

    public static final String TAG = "BrowserActivity";

    public Submission post;
    public String url;
    public String domain;
    public boolean loadFromCache;
    public boolean loadSyncedArticle;
    public boolean canGoBack, canGoForward;

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_main);
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
        setContentView(R.layout.activity_toolbar_scrollable);
        initToolbar();

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        post = (Submission) getIntent().getSerializableExtra("post");
        if (post != null) {
            url = post.getURL();
            domain = post.getDomain();
        } else {
            url = getIntent().getStringExtra("url");
            domain = getIntent().getStringExtra("domain");
        }

        setupMainFragment();
    }

    private void setupMainFragment() {
        loadSyncedArticle = (MyApplication.offlineModeEnabled && syncedArticleExists());
        Fragment fragment = (loadSyncedArticle) ? new ArticleFragment() : new BrowserFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container_main, fragment).commitAllowingStateLoss();
    }

    public boolean syncedArticleExists() {
        final String articleId = String.valueOf(url.hashCode());
        File dir = GeneralUtils.getSyncedArticlesDir(this);
        File file = StorageUtils.findFile(dir, dir.getAbsolutePath(), articleId + AppConstants.SYNCED_ARTICLE_DATA_SUFFIX);
        return file!=null;
    }

    public void loadSyncedArticle() {
        loadFromCache = false;
        loadSyncedArticle = true;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction().replace(R.id.container_main, new ArticleFragment()).commitAllowingStateLoss();
    }

    public void loadOriginalPage() {
        loadFromCache = false;
        loadSyncedArticle = false;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction().replace(R.id.container_main, new BrowserFragment()).commitAllowingStateLoss();
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

        if(post == null) {
            menu.findItem(R.id.action_comments).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_comments:
                MyApplication.dualPaneActive = false; //set to false to open comments in a new activity
                Intent intent = new Intent(this, PostActivity.class);
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
