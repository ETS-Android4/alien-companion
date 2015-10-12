package com.dyejeekis.aliencompanion.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.dyejeekis.aliencompanion.Fragments.MessageFragment;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MessageActivity extends SwipeBackActivity {

    private MessageFragment messageFragment;
    private FragmentManager fm;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MainActivity.fontStyle, true);
        if(MainActivity.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        container = (LinearLayout) findViewById(R.id.container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MainActivity.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MainActivity.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MainActivity.swipeSetting);

        fm = getFragmentManager();

        int resource;
        if(MainActivity.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
        if(MainActivity.dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MainActivity.actionSort = true;
                    showMessagesOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MainActivity.actionSort = false;
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
                        if (MainActivity.actionSort) messageFragment.showCategoryPopup(v);
                        else messageFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (MainActivity.actionSort) postFragment.showSortPopup(v);
                            else postFragment.refreshComments();
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

        if(MainActivity.dualPane) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(messageFragment).commitAllowingStateLoss();
            messageFragment = recreateMessageFragment(messageFragment);
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
            fm.beginTransaction().add(resource, messageFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private MessageFragment recreateMessageFragment(MessageFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        MessageFragment newInstance = MessageFragment.newInstance(f.adapter, f.category, f.sort, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

}
