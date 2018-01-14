package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.BuildConfig;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SubredditActivity extends SwipeBackActivity {

    private PostListFragment listFragment;
    private PostFragment postFragment;
    private FragmentManager fm;
    private RelativeLayout container;
    //private boolean dualPaneActive;

    public PostListFragment getListFragment() {
        return listFragment;
    }

    public PostFragment getPostFragment() {
        return postFragment;
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        initToolbar();
        container = (RelativeLayout) findViewById(R.id.container);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        fm = getSupportFragmentManager();

        int resource;
        if(MyApplication.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MyApplication.dualPaneActive = true;
            View.inflate(this, R.layout.activity_main_dual_panel, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            MyApplication.dualPaneActive = false;
            View.inflate(this, R.layout.activity_main, container);
            resource = R.id.fragmentHolder;
        }

        setupMainFragment(resource);
    }

    public void setupPostFragment(PostFragment postFragment) {
        this.postFragment = postFragment;
        fm.beginTransaction().replace(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    private void setupMainFragment(int container) {
        listFragment = (PostListFragment) fm.findFragmentById(container);
        if(listFragment == null) {
            listFragment = new PostListFragment();
            fm.beginTransaction().add(container, listFragment, "listFragment").commit();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(MyApplication.offlineModeEnabled) {
            menu.removeItem(R.id.action_sort);
            menu.removeItem(R.id.action_search);
            menu.removeItem(R.id.action_view_sidebar);
            menu.removeItem(R.id.action_submit_post);
        }
        else {
            menu.removeItem(R.id.action_view_synced);
            menu.removeItem(R.id.action_pending_actions);
            menu.removeItem(R.id.action_clear_synced);
        }

        if(!BuildConfig.DEBUG) {
            menu.removeItem(R.id.action_debug);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(MyApplication.dualPaneActive && getPostFragment()!= null) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MyApplication.actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
                    try {
                        showPostsOrCommentsPopup(findViewById(R.id.action_refresh));
                    } catch (Exception e) {
                        showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    }
                    return true;
            }
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                return false;
        }
    }

    private void showPostsOrCommentsPopup(final View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_posts_or_comments);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_posts:
                        if (MyApplication.actionSort) listFragment.showSortPopup(v);
                        else listFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (MyApplication.actionSort) postFragment.showSortPopup(v);
                            else postFragment.refreshPostAndComments();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(MyApplication.dualPane) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(listFragment).commitAllowingStateLoss();
            listFragment = recreateListFragment(listFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MyApplication.dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = MainActivity.recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                MyApplication.dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, listFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private PostListFragment recreateListFragment(PostListFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostListFragment newInstance = PostListFragment.newInstance(f.adapter, f.subreddit, f.isMulti, f.submissionSort, f.timeSpan, f.currentLoadType, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }
}
