package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.BuildConfig;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.VerifyAccountDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerEmptySpace;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMultis;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerOther;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerOtherItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSeparator;
import com.gDyejeekis.aliencompanion.models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.MenuType;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerHeader;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSubreddits;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ToolbarActivity {

    public static final String TAG = "MainActivity";

    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private PostListFragment listFragment;
    private PostFragment postFragment;
    private RecyclerView drawerContent;
    private NavDrawerAdapter navDrawerAdapter;
    private DrawerLayout.LayoutParams drawerParams;
    private NavigationView scrimInsetsFrameLayout;
    //private Toolbar toolbar;
    private RelativeLayout container;

    // TODO: 3/27/2017 move this field to application class
    public static boolean dualPaneActive;

    public static boolean setupAccount = false;
    public static boolean notifyDrawerChanged = false;
    public static boolean notifySwitchedMode = false;
    public static String oauthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setOrientation();
        //MyApplication.setThemeRelatedFields(this);
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        //if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        //    // Activity was brought to front and not created,
        //    // Thus finishing this will get us to the last viewed activity
        //    Log.d(TAG, "Killing additional MainActivity that was brought to front");
        //    finish();
        //    return;
        //}
        setContentView(R.layout.activity_main_plus);
        initToolbar();

        container = (RelativeLayout) findViewById(R.id.container);
        //if(MyApplication.nightThemeEnabled) {
        //    getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
        //}

        fm = getSupportFragmentManager();

        initNavDrawer();

        int resource;
        if(MyApplication.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dualPaneActive = true;
            View.inflate(this, R.layout.activity_main_dual_panel, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            View.inflate(this, R.layout.activity_main, container);
            dualPaneActive = false;
            resource = R.id.fragmentHolder;
        }
        setupMainFragment(resource);
    }

    private void setOrientation() {
        MyApplication.currentOrientation = MyApplication.screenOrientation;
        switch (MyApplication.screenOrientation) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case 3:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case 4:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
        }
    }

    public void setupPostFragment(PostFragment fragment) {
        this.postFragment = fragment;
        fm.beginTransaction().replace(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    public PostFragment getPostFragment() {
        return postFragment;
    }

    private void setupMainFragment(int containerRes) {
        listFragment = (PostListFragment) fm.findFragmentById(containerRes);
        if(listFragment == null) {
            listFragment = new PostListFragment();
            fm.beginTransaction().add(containerRes, listFragment, "listFragment").commit();
        }
    }

    public void changeCurrentUser(SavedAccount account) {
        MyApplication.currentAccount = account;

        MyApplication.scheduleMessageCheckService(this);
        //MyApplication.currentUser = (account.loggedIn) ? new User(null, account.getUsername(), account.getModhash(), account.getCookie()) : null;
        if(account.loggedIn) {
            if(account.oauth2) {
                MyApplication.currentUser = new User(new PoliteRedditHttpClient(), account.getUsername(), account.getToken());
                MyApplication.currentAccessToken = account.getToken().accessToken;
            }
            else {
                MyApplication.currentUser = new User(new PoliteRedditHttpClient(), account.getUsername(), account.getModhash(), account.getCookie());
                MyApplication.currentAccessToken = null;
            }
        }
        else {
            MyApplication.currentUser = null;
            try {
                MyApplication.currentAccessToken = account.getToken().accessToken;
            } catch (NullPointerException e) {
                MyApplication.currentAccessToken = null;
            }
        }

        if(MyApplication.currentUser!=null) {
            navDrawerAdapter.showUserMenuItems();
        }
        else {
            navDrawerAdapter.hideUserMenuItems();
        }
        navDrawerAdapter.updateSubredditItems(MyApplication.currentAccount.getSubreddits());
        navDrawerAdapter.updateMultiredditItems(MyApplication.currentAccount.getMultireddits());

        try {
            if(listFragment.isMulti) homePage();
            else listFragment.changeSubreddit(listFragment.subreddit);
            String message = (MyApplication.currentUser==null) ? "Logged out" : "Logged in as " + account.getUsername();
            ToastUtils.showToast(this, message);
        } catch (NullPointerException e) {}
    }

    public void homePage() {
        listFragment.isMulti = false;
        listFragment.changeSubreddit(null);
    }

    public boolean isDrawerVisible() {
        return drawerLayout.isDrawerVisible(MyApplication.drawerGravity);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(HandleUrlActivity.notifySwitchedMode) {
            HandleUrlActivity.notifySwitchedMode = false;
            ToastUtils.showToast(this, "Switched to online mode");
        }

        if(notifySwitchedMode) {
            notifySwitchedMode = false;
            if(listFragment.subreddit!=null && listFragment.isOther && listFragment.subreddit.equals("synced")) {
                listFragment.changeSubreddit(null);
            }
        }

        listFragment.loadMore = MyApplication.endlessPosts;

        if(notifyDrawerChanged) {
            Log.d(TAG, "Notifying navigation drawer changed..");
            notifyDrawerChanged = false;
            getNavDrawerAdapter().notifyDataSetChanged();
        }

        if(!toolbarVisible && !MyApplication.autoHideToolbar) {
            showToolbar();
            getListFragment().updateSwipeRefreshOffset();
            if(getPostFragment()!=null) {
                getPostFragment().updateSwipeRefreshOffset();
            }
        }

        if(MyApplication.fabPostNavChanged) {
            MyApplication.fabPostNavChanged = false;
            getListFragment().initFabNavOptions(findViewById(android.R.id.content));
        }

        if(MyApplication.fabCommentNavChanged) {
            MyApplication.fabCommentNavChanged = false;
            if(getPostFragment()!=null) {
                getPostFragment().initFabNavOptions(findViewById(R.id.postFragmentHolder));
            }
        }

        if(setupAccount) {
            setupAccount = false;

            VerifyAccountDialogFragment dialog = new VerifyAccountDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("code", oauthCode);
            dialog.setArguments(bundle);
            dialog.show(getFragmentManager(), "dialog");

            oauthCode = null;
        }

        if(EditSubredditsActivity.changesMade) {
            EditSubredditsActivity.changesMade = false;
            getNavDrawerAdapter().updateSubredditItems(MyApplication.currentAccount.getSubreddits());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() { //TODO: use generalutils method (saveAccountChanges)
                    List<SavedAccount> accounts = new ArrayList<SavedAccount>();
                    boolean check = true;
                    //if(currentAccount != null) {
                        for (NavDrawerAccount accountItem : navDrawerAdapter.accountItems) {
                            if (accountItem.getAccountType() == NavDrawerAccount.TYPE_ACCOUNT || accountItem.getAccountType() == NavDrawerAccount.TYPE_LOGGED_OUT) {
                                SavedAccount accountToSave;
                                if (check && accountItem.getName().equals(MyApplication.currentAccount.getUsername())) {
                                    check = false;
                                    accountToSave = MyApplication.currentAccount;
                                } else accountToSave = accountItem.savedAccount;
                                accounts.add(accountToSave);
                            }
                        }
                        getNavDrawerAdapter().saveAccounts(accounts);
                    //}
                }
            });
        }

        if(EditMultisActivity.changesMade) {
            EditMultisActivity.changesMade = false;
            getNavDrawerAdapter().updateMultiredditItems(MyApplication.currentAccount.getMultireddits());
            GeneralUtils.saveAccountChanges(this);
        }

        if(MyApplication.currentOrientation != MyApplication.screenOrientation) setOrientation();

        if(MyApplication.dualPaneCurrent != MyApplication.dualPane) {
            MyApplication.dualPaneCurrent = MyApplication.dualPane;
            toggleDualPane();
        }

        if(MyApplication.currentFontStyle != MyApplication.fontStyle || MyApplication.currentFontFamily != MyApplication.fontFamily) {
            MyApplication.currentFontStyle = MyApplication.fontStyle;
            MyApplication.currentFontFamily = MyApplication.fontFamily;
            getTheme().applyStyle(MyApplication.fontStyle, true);
            getTheme().applyStyle(MyApplication.fontFamily, true);
            listFragment.redrawList();
        }

        // TODO: 1/9/2018 this check also needs to take into account if 'use primary color in dark modes' is enabled
        if(MyApplication.colorPrimaryChanged && (MyApplication.currentBaseTheme < MyApplication.DARK_THEME)) {
            MyApplication.colorPrimaryChanged = false;
            MyApplication.currentColor = MyApplication.colorPrimary;
            MyApplication.linkColor = MyApplication.colorPrimary;
            int[] primaryColors = MyApplication.getPrimaryColors(this);
            int[] primaryDarkColors = MyApplication.getPrimaryDarkColors(this);
            int[] primaryLightColors = MyApplication.getPrimarLightColors(this);
            int index = MyApplication.getCurrentColorIndex(primaryColors);
            MyApplication.colorPrimaryDark = primaryDarkColors[index];
            MyApplication.colorPrimaryLight = primaryLightColors[index];
            updateToolbarColors();
            drawerLayout.setStatusBarBackgroundColor(MyApplication.colorPrimaryDark);
            listFragment.colorPrimaryChanged();
            navDrawerAdapter.notifyDataSetChanged();
        }

        if(MyApplication.colorSecondaryChanged) {
            MyApplication.colorSecondaryChanged = false;
            listFragment.colorSecondaryChanged();
            if(MyApplication.postFabNavigation) {
                getListFragment().updateFabNavColors();
            }
        }

        if(MyApplication.drawerGravity != drawerParams.gravity) {
            drawerParams.gravity = MyApplication.drawerGravity;
            scrimInsetsFrameLayout.setLayoutParams(drawerParams);
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public boolean onOptionsItemSelected(MenuItem item) {
                    if (item != null && item.getItemId() == android.R.id.home) {
                        if (drawerLayout.isDrawerOpen(MyApplication.drawerGravity)) {
                            drawerLayout.closeDrawer(MyApplication.drawerGravity);
                        } else {
                            drawerLayout.openDrawer(MyApplication.drawerGravity);
                        }
                    }
                    return false;
                }
            };
            drawerLayout.setDrawerListener(drawerToggle);
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
        if(dualPaneActive && getPostFragment()!=null) {
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
                    } //TODO: find a more suitable anchor
                    return true;
            }
        }

        if(item.getItemId() == R.id.action_debug) {
            startActivity(new Intent(this, DebugActivity.class));
            return true;
        }

        return drawerToggle.onOptionsItemSelected(item);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

        if(MyApplication.dualPane) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(listFragment).commitAllowingStateLoss();
            listFragment = recreateListFragment(listFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, listFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private void toggleDualPane() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(listFragment).commitAllowingStateLoss();
            listFragment = recreateListFragment(listFragment);
            int resource;
            if(MyApplication.dualPane) {
                dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            }
            else {
                dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, listFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private void initNavDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        scrimInsetsFrameLayout = (NavigationView) findViewById(R.id.scrimInsetsFrameLayout);
        drawerLayout.setStatusBarBackgroundColor(MyApplication.colorPrimaryDark);

        drawerParams = new DrawerLayout.LayoutParams(calculateDrawerWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        final int gravity = (MyApplication.prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        drawerParams.gravity = gravity;
        scrimInsetsFrameLayout.setLayoutParams(drawerParams);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                if (item != null && item.getItemId() == android.R.id.home) {
                    if (drawerLayout.isDrawerOpen(gravity)) {
                        drawerLayout.closeDrawer(gravity);
                    } else {
                        drawerLayout.openDrawer(gravity);
                    }
                }
                return false;
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        initNavDrawerContent();
    }

    public void initNavDrawerContent() {
        drawerContent = (RecyclerView) findViewById((R.id.drawer_content));
        drawerContent.setLayoutManager(new LinearLayoutManager(this));
        drawerContent.setHasFixedSize(true);

        navDrawerAdapter = new NavDrawerAdapter(this);
        navDrawerAdapter.add(new NavDrawerHeader());
        navDrawerAdapter.add(new NavDrawerEmptySpace());
        navDrawerAdapter.addAll(getMenuItems());

        addNavDrawerSeparators();

        navDrawerAdapter.add(new NavDrawerSubreddits());
        navDrawerAdapter.addAll(getDefaultSubredditItems());

        addNavDrawerSeparators();

        navDrawerAdapter.add(new NavDrawerMultis());

        addNavDrawerSeparators();

        navDrawerAdapter.add(new NavDrawerOther());
        //if(MyApplication.offlineModeEnabled) {
            navDrawerAdapter.add(new NavDrawerOtherItem("Synced"));
        //}

        drawerContent.setAdapter(navDrawerAdapter);

        navDrawerAdapter.importAccounts();
    }

    private void addNavDrawerSeparators() {
        navDrawerAdapter.add(new NavDrawerEmptySpace());
        navDrawerAdapter.add(new NavDrawerSeparator());
        navDrawerAdapter.add(new NavDrawerEmptySpace());
    }

    private List<NavDrawerItem> getMenuItems() {
        List<NavDrawerItem> menuItems = new ArrayList<>();
        menuItems.add(new NavDrawerMenuItem(MenuType.user));
        menuItems.add(new NavDrawerMenuItem(MenuType.subreddit));
        menuItems.add(new NavDrawerMenuItem(MenuType.settings));

        return menuItems;
    }

    private List<NavDrawerItem> getDefaultSubredditItems() {
        List<NavDrawerItem> subredditItems = new ArrayList<>();
        subredditItems.add(new NavDrawerSubredditItem());
        for (String subreddit : MyApplication.defaultSubredditStrings) {
            subredditItems.add(new NavDrawerSubredditItem(subreddit));
        }

        return subredditItems;
    }

    private int calculateDrawerWidth() {
        final float scale = getResources().getDisplayMetrics().density;
        int drawerWidth = (int) (320 * scale + 0.5f);

        return drawerWidth;
        //return GeneralUtils.getPortraitWidth(this)/2;
    }

    public PostListFragment getListFragment() {
        return listFragment;
    }

    public RecyclerView getNavDrawerView() {
        return drawerContent;
    }

    public NavDrawerAdapter getNavDrawerAdapter() {
        return navDrawerAdapter;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawers();
        }
        else {
            //MyApplication.currentUser = null; //user connects every time main activity is started - RE-ENABLE THIS IF NEEDED
            super.onBackPressed();
        }
    }

    private PostListFragment recreateListFragment(PostListFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostListFragment newInstance = PostListFragment.newInstance(f.adapter, f.subreddit, f.isMulti, f.submissionSort, f.timeSpan, f.currentLoadType, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

    public static PostFragment recreatePostFragment(PostFragment f, FragmentManager fm) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostFragment newInstance = PostFragment.newInstance(f.postAdapter);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

}
