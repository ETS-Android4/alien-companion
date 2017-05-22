package com.gDyejeekis.aliencompanion.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserActivity extends SwipeBackActivity {

    private FragmentManager fm;
    private UserFragment userFragment;
    private PostFragment postFragment;
    private RelativeLayout container;
    private boolean addToSyncedVisible;

    public UserFragment getListFragment() {
        return userFragment;
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
            MainActivity.dualPaneActive = true;
            View.inflate(this, R.layout.activity_main_dual_panel, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            MainActivity.dualPaneActive = false;
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
        userFragment = (UserFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(userFragment == null) {
            userFragment = new UserFragment();
            getSupportFragmentManager().beginTransaction().add(container, userFragment, "listFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);

        MenuItem addToSynced = menu.findItem(R.id.action_add_to_synced);
        addToSynced.setVisible(addToSyncedVisible);

        return true;
    }

    public void setAddToSyncedVisible(boolean flag) {
        addToSyncedVisible = flag;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(MainActivity.dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MyApplication.actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
                    showPostsOrCommentsPopup(findViewById(R.id.action_refresh));
                    return true;
            }
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
                        if (MyApplication.actionSort) userFragment.showContentPopup(v);
                        else userFragment.refreshList();
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
            fm.beginTransaction().remove(userFragment).commitAllowingStateLoss();
            userFragment = recreateUserFragment(userFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MainActivity.dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = MainActivity.recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                MainActivity.dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, userFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private UserFragment recreateUserFragment(UserFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        UserFragment newInstance = UserFragment.newInstance(f.adapter, f.username, f.userOverviewSort, f.userContent, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }
}
