package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class PostActivity extends SwipeBackActivity {

    private PostFragment postFragment;

    public PostFragment getPostFragment() {
        return postFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        setupFragment();
    }

    private void setupFragment() {
        postFragment = (PostFragment) getFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(postFragment == null) {
            postFragment = new PostFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, postFragment, "postFragment").commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(HandleUrlActivity.notifySwitchedMode) {
            HandleUrlActivity.notifySwitchedMode = false;
            ToastUtils.displayShortToast(this, "Switched to online mode");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int menuResource;
        if(MyApplication.offlineModeEnabled) menuResource = R.menu.menu_post_offline;
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
    //public void onConfigurationChanged(Configuration newConfig) {
    //    super.onConfigurationChanged(newConfig);
//
    //    //if(MainActivity.dualPane) {
    //    //    if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    //    //        finish();
    //    //    }
    //    //}
    //}

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
