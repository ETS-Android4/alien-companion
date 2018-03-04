package com.gDyejeekis.aliencompanion.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.fragments.MessageFragment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MessageActivity extends SwipeBackActivity {

    public static boolean isActive = false;

    private MessageFragment messageFragment;
    private FragmentManager fm;
    private FrameLayout container;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isActive = true;
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_scrollable);
        initToolbar();
        container = (FrameLayout) findViewById(R.id.container_main);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        fm = getSupportFragmentManager();

        int resource;
        if(MyApplication.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MyApplication.dualPaneActive = true;
            View.inflate(this, R.layout.activity_dual_pane, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            MyApplication.dualPaneActive = false;
            View.inflate(this, R.layout.activity_single_pane, container);
            resource = R.id.fragmentHolder;
        }

        setupMainFragment(resource);
    }

    public void setupMainFragment(int container) {
        messageFragment = (MessageFragment) fm.findFragmentById(container);
        if(messageFragment == null) {
            messageFragment = new MessageFragment();
            fm.beginTransaction().add(container, messageFragment, "listFragment").commit();
        }
    }

    public void setupPostFragment(PostFragment postFragment) {
        PostFragment oldFragment = (PostFragment) fm.findFragmentByTag("postFragment");
        if(oldFragment!=null) fm.beginTransaction().remove(oldFragment).commit();
        fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(MyApplication.dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MyApplication.actionSort = true;
                    showMessagesOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
                    showMessagesOrCommentsPopup(findViewById(R.id.action_refresh));
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

    private void showMessagesOrCommentsPopup(final View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_messages_or_comments);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_messages:
                        if (MyApplication.actionSort) messageFragment.showCategoryPopup(v);
                        else messageFragment.refreshList();
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
            container.removeAllViews();
            fm.beginTransaction().remove(messageFragment).commitAllowingStateLoss();
            messageFragment = recreateMessageFragment(messageFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MyApplication.dualPaneActive = true;
                View.inflate(this, R.layout.activity_dual_pane, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = MainActivity.recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                MyApplication.dualPaneActive = false;
                View.inflate(this, R.layout.activity_single_pane, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, messageFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private MessageFragment recreateMessageFragment(MessageFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        MessageFragment newInstance = MessageFragment.newInstance(f.adapter, f.category, f.sort, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

    //@Override
    //public void onResume() {
    //    isActive = true;
    //    super.onResume();
    //}
//
    //@Override
    //public void onStop() {
    //    isActive = false;
    //    super.onStop();
    //}

    @Override
    public void onDestroy() {
        isActive = false;
        super.onDestroy();
    }

}
