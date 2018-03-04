package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

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
        setContentView(R.layout.activity_reddit_content);
        initToolbar();

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        setupFragment();
    }

    private void setupFragment() {
        postFragment = (PostFragment) getSupportFragmentManager().findFragmentById(R.id.container_main);
        if(postFragment == null) {
            postFragment = new PostFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container_main, postFragment, "postFragment").commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(HandleUrlActivity.notifySwitchedMode) {
            HandleUrlActivity.notifySwitchedMode = false;
            ToastUtils.showToast(this, "Switched to online mode");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(MyApplication.volumeNavigation) {
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                postFragment.commentNavListener.nextComment();
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                postFragment.commentNavListener.previousComment();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        if(MyApplication.offlineModeEnabled) {
            MenuItem sortAction = menu.findItem(R.id.action_sort_comments);
            sortAction.setVisible(false);
        }
        Submission post = (Submission) getIntent().getSerializableExtra("post");
        if(post!=null && post.isLocked()) {
            MenuItem lockedAction = menu.findItem(R.id.action_post_locked);
            lockedAction.setVisible(true);
            MenuItem replyAction = menu.findItem(R.id.action_reply);
            replyAction.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_post_locked:
                ToastUtils.showSnackbarOverToast(this, "This post is locked. You won't be able to comment.");
                return true;
            default:
                return false;
        }
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
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
