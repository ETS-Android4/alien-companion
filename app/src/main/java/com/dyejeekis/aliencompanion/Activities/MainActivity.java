package com.dyejeekis.aliencompanion.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.dyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.Fragments.PostListFragment;
import com.dyejeekis.aliencompanion.Models.SavedAccount;
import com.dyejeekis.aliencompanion.Utils.ScrimInsetsFrameLayout;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.entity.User;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.dyejeekis.aliencompanion.enums.MenuType;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerHeader;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerItem;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubredditItem;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubreddits;
import com.dyejeekis.aliencompanion.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.imid.swipebacklayout.lib.ViewDragHelper;


public class MainActivity extends AppCompatActivity {

    public static final String[] defaultSubredditStrings = {"All", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    public static final int homeAsUpIndicator = R.mipmap.ic_arrow_back_white_24dp;

    public static boolean initialized;

    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private PostListFragment listFragment;
    //private PostFragment postFragment;
    private RecyclerView drawerContent;
    private NavDrawerAdapter adapter;
    private DrawerLayout.LayoutParams drawerParams;
    private ScrimInsetsFrameLayout scrimInsetsFrameLayout;
    private Toolbar toolbar;
    private LinearLayout container;

    public static boolean actionSort = false;

    public static boolean showHiddenPosts;

    public static SharedPreferences prefs;
    public static boolean nightThemeEnabled;
    public static boolean offlineModeEnabled;
    public static boolean dualPane;
    public static boolean dualPaneCurrent;
    public static boolean dualPaneActive;
    public static int screenOrientation;
    public static int currentOrientation;
    public static int fontStyle;
    public static int currentFontStyle;
    public static int colorPrimary;
    public static int colorPrimaryDark;
    public static int currentColor;
    public static int swipeSetting;
    public static boolean swipeRefresh;
    public static int drawerGravity;
    public static boolean endlessPosts;
    public static boolean showNSFWpreview;
    public static boolean hideNSFW;
    public static int initialCommentCount;
    public static int initialCommentDepth;
    public static int textColor;
    public static int backgroundColor;
    public static int commentPermaLinkBackgroundColor;
    public static int syncPostCount;
    public static int syncCommentCount;
    public static int syncCommentDepth;

    public static SavedAccount currentAccount;
    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getCurrentSettings();
        dualPaneCurrent = dualPane;
        setOrientation();
        getTheme().applyStyle(fontStyle, true);
        if(nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
            colorPrimary = Color.parseColor("#181818");
            colorPrimaryDark = Color.BLACK;
            textColor = Color.WHITE;
            backgroundColor = Color.BLACK;
            commentPermaLinkBackgroundColor = Color.parseColor("#545454");
        }
        else {
            getTheme().applyStyle(R.style.selectedTheme_day, true);
            textColor = Color.BLACK;
            backgroundColor = Color.WHITE;
            commentPermaLinkBackgroundColor = Color.parseColor("#FFFFDA");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plus);

        container = (LinearLayout) findViewById(R.id.container);
        initialized = true;
        showHiddenPosts = false;
        currentFontStyle = fontStyle;
        currentColor = colorPrimary;
        colorPrimaryDark = getPrimaryDarkColor();
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MainActivity.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(colorPrimary);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        fm = getFragmentManager();

        initNavDrawer();

        int resource;
        if(dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
        currentOrientation = screenOrientation;
        switch (screenOrientation) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
        }
    }

    public void setupPostFragment(PostFragment postFragment) {
        PostFragment oldFragment = (PostFragment) fm.findFragmentByTag("postFragment");
        if(oldFragment!=null) fm.beginTransaction().remove(oldFragment).commit();
        fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    private void setupMainFragment(int containerRes) {
        listFragment = (PostListFragment) fm.findFragmentById(containerRes);
        if(listFragment == null) {
            //Log.d("MainActivity", "Creating new fragment...");
            listFragment = new PostListFragment();
            fm.beginTransaction().add(containerRes, listFragment, "listFragment").commit();
        }
    }

    public void changeCurrentUser(SavedAccount account) {
        currentAccount = account;
        currentUser = (account!=null) ? new User(new RedditHttpClient(), account.getUsername(), account.getModhash(), account.getCookie()) : null;
        //initNavDrawerContent();
        if(currentUser!=null) {
            adapter.showUserMenuItems();
            adapter.updateSubredditItems(currentAccount.getSubreddits());
        }
        else {
            adapter.hideUserMenuItems();
            List<String> subreddits = new ArrayList<>();
            Collections.addAll(subreddits, defaultSubredditStrings);
            adapter.updateSubredditItems(subreddits);
        }
        homePage();
    }

    public void homePage() {
        if(listFragment!=null) {
            listFragment.setSubreddit(null);
            listFragment.setSubmissionSort(SubmissionSort.HOT);
            listFragment.refreshList();
        }
    }

    public static void getCurrentSettings() {
        //Log.d("geo test", "settings saved");
        dualPane = prefs.getBoolean("dualPane", true);
        //dualPane = true;
        screenOrientation = Integer.parseInt(prefs.getString("screenOrientation", "2"));
        nightThemeEnabled = prefs.getBoolean("nightTheme", false);
        offlineModeEnabled = prefs.getBoolean("offlineMode", false);
        fontStyle = Integer.parseInt(prefs.getString("fontSize", "1"));
        switch (fontStyle) {
            case 0:
                fontStyle = R.style.FontStyle_Small;
                break;
            case 1:
                fontStyle = R.style.FontStyle_Medium;
                break;
            case 2:
                fontStyle = R.style.FontStyle_Large;
                break;
            case 3:
                fontStyle = R.style.FontStyle_ExtraLarge;
                break;
        }
        colorPrimary = Color.parseColor(prefs.getString("toolbarColor", "#2196F3"));
        swipeRefresh = prefs.getBoolean("swipeRefresh", true);
        drawerGravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        endlessPosts = prefs.getBoolean("endlessPosts", true);
        showNSFWpreview = prefs.getBoolean("showNSFWthumb", false);
        hideNSFW = prefs.getBoolean("hideNSFW", false);
        swipeSetting = Integer.parseInt(prefs.getString("swipeBack", "0"));
        switch (swipeSetting) {
            case 0:
                swipeSetting = ViewDragHelper.EDGE_LEFT;
                break;
            case 1:
                swipeSetting = ViewDragHelper.EDGE_RIGHT;
                break;
            case 2:
                swipeSetting = ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT;
                break;
            case 3:
                swipeSetting = ViewDragHelper.STATE_IDLE;
        }
        initialCommentCount = Integer.parseInt(prefs.getString("initialCommentCount", "100"));
        initialCommentDepth = (Integer.parseInt(prefs.getString("initialCommentDepth", "3")));
        syncPostCount = Integer.parseInt(prefs.getString("syncPostCount", "25"));
        syncCommentCount = Integer.parseInt(prefs.getString("syncCommentCount", "100"));
        syncCommentDepth = Integer.parseInt(prefs.getString("syncCommentDepth", "3"));
    }

    private int getPrimaryDarkColor() {
        String[] primaryColors = getResources().getStringArray(R.array.colorPrimaryValues);
        int index = 0;
        for(String color : primaryColors) {
            if(Color.parseColor(color)==colorPrimary) break;
            index++;
        }
        String[] primaryDarkColors = getResources().getStringArray(R.array.colorPrimaryDarkValues);
        return Color.parseColor(primaryDarkColors[index]); //TODO: check indexoutofboundsexception
    }

    @Override
    public void onResume() {
        super.onResume();

        if(currentOrientation != screenOrientation) setOrientation();

        if(dualPaneCurrent != dualPane) {
            dualPaneCurrent = dualPane;
            toggleDualPane();
        }

        if(currentFontStyle != fontStyle) {
            currentFontStyle = fontStyle;
            getTheme().applyStyle(fontStyle, true);
            listFragment.refreshList();
        }

        if(!nightThemeEnabled && currentColor != colorPrimary) {
            currentColor = colorPrimary;
            toolbar.setBackgroundColor(colorPrimary);
            colorPrimaryDark = getPrimaryDarkColor();
            drawerLayout.setStatusBarBackgroundColor(colorPrimaryDark);
            listFragment.colorSchemeChanged();
            adapter.notifyDataSetChanged();
            //Log.d("geo test", "main color changed");
        }

        //final int gravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        if(drawerGravity != drawerParams.gravity) {
            //Log.d("geo test", "drawer gravity changed");
            drawerParams.gravity = drawerGravity;
            scrimInsetsFrameLayout.setLayoutParams(drawerParams);
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public boolean onOptionsItemSelected(MenuItem item) {
                    if (item != null && item.getItemId() == android.R.id.home) {
                        if (drawerLayout.isDrawerOpen(drawerGravity)) {
                            drawerLayout.closeDrawer(drawerGravity);
                        } else {
                            drawerLayout.openDrawer(drawerGravity);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        int menuResource;
        if(offlineModeEnabled) menuResource = R.menu.menu_main_offline;
        else menuResource = R.menu.menu_main;
        getMenuInflater().inflate(menuResource, menu);
        //toggleHiddenMenuItem = (MenuItem) findViewById(R.id.action_toggle_hidden);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    actionSort = false;
                    showPostsOrCommentsPopup(findViewById(R.id.action_refresh));
                    return true;
            }
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
                        if (actionSort) listFragment.showSortPopup(v);
                        else listFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (actionSort) postFragment.showSortPopup(v);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

        if(dualPane) {
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
            if(dualPane) {
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
        scrimInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        drawerLayout.setStatusBarBackgroundColor(colorPrimaryDark);

        drawerParams = new DrawerLayout.LayoutParams(calculateDrawerWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        final int gravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
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

        adapter = new NavDrawerAdapter(this);
        adapter.add(new NavDrawerHeader());
        adapter.addAll(getMenuItems());
        adapter.add(new NavDrawerSubreddits());
        adapter.addAll(getDefaultSubredditItems());

        drawerContent.setAdapter(adapter);

        adapter.importAccounts();
    }

    private List<NavDrawerItem> getMenuItems() {
        List<NavDrawerItem> menuItems = new ArrayList<>();
        menuItems.add(new NavDrawerMenuItem(MenuType.user));
        menuItems.add(new NavDrawerMenuItem(MenuType.subreddit));
        menuItems.add(new NavDrawerMenuItem(MenuType.settings));
        //menuItems.add(new NavDrawerMenuItem(MenuType.cached));

        return menuItems;
    }

    private List<NavDrawerItem> getDefaultSubredditItems() {
        List<NavDrawerItem> subredditItems = new ArrayList<>();
        subredditItems.add(new NavDrawerSubredditItem());
        for (String subreddit : defaultSubredditStrings) {
            subredditItems.add(new NavDrawerSubredditItem(subreddit));
        }

        return subredditItems;
    }

    private int calculateDrawerWidth() {
        final float scale = getResources().getDisplayMetrics().density;
        int drawerWidth = (int) (320 * scale + 0.5f);
        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);
        //int width = size.x;
        //int height = size.y;
//
        //drawerWidth = (width < height) ? Math.round(drawerSizeModifier*width) : Math.round(drawerSizeModifier*height);

        return drawerWidth;
    }

    public PostListFragment getListFragment() {
        return listFragment;
    }

    public RecyclerView getNavDrawerView() {
        return drawerContent;
    }

    public NavDrawerAdapter getNavDrawerAdapter() {
        return adapter;
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
            MainActivity.currentUser = null; //user connects every time main activity is started
            super.onBackPressed();
        }
    }

    private PostListFragment recreateListFragment(PostListFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostListFragment newInstance = PostListFragment.newInstance(f.postListAdapter, f.subreddit, f.submissionSort, f.timeSpan);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

    public static PostFragment recreatePostFragment(PostFragment f, FragmentManager fm) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostFragment newInstance = PostFragment.newInstance(f.postAdapter);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

    //@Override
    //public void onStop() {
    //    super.onStop();
    //    Log.d("geo test", "on stop called");
    //}

    //@Override
    //public void onDestroy() {
    //    super.onDestroy();
    //    Log.d("geo debug", "MainActivity onDestroy called");
    //}

}
