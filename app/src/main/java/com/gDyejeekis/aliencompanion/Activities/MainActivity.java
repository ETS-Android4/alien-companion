package com.gDyejeekis.aliencompanion.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.enums.MenuType;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerHeader;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubreddits;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.imid.swipebacklayout.lib.ViewDragHelper;


public class MainActivity extends AppCompatActivity {

    //public static final String currentVersion = "0.1.1";

    //public static final String[] defaultSubredditStrings = {"All", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};

    //public static final int NAV_DRAWER_CLOSE_TIME = 200;

    //public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    //public static final int homeAsUpIndicator = R.mipmap.ic_arrow_back_white_24dp;

    //public static boolean initialized;

    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private PostListFragment listFragment;
    //private PostFragment postFragment;
    private RecyclerView drawerContent;
    private NavDrawerAdapter adapter;
    private DrawerLayout.LayoutParams drawerParams;
    private NavigationView scrimInsetsFrameLayout;
    private Toolbar toolbar;
    private LinearLayout container;

    //public static boolean actionSort = false;

    //public static boolean showHiddenPosts;

    //public static SharedPreferences prefs;
    //public static String deviceID;
    //public static boolean nightThemeEnabled;
    //public static boolean offlineModeEnabled;
    //public static boolean dualPane;
    //public static boolean dualPaneCurrent;
    public static boolean dualPaneActive;
    //public static int screenOrientation;
    //public static int currentOrientation;
    //public static int fontStyle;
    //public static int currentFontStyle;
    //public static int colorPrimary;
    //public static int colorPrimaryDark;
    //public static int currentColor;
    //public static int swipeSetting;
    //public static boolean swipeRefresh;
    //public static int drawerGravity;
    //public static boolean endlessPosts;
    //public static boolean showNSFWpreview;
    //public static boolean hideNSFW;
    //public static int initialCommentCount;
    //public static int initialCommentDepth;
    //public static int textColor;
    //public static int textHintColor;
    //public static int linkColor;
    //public static int backgroundColor;
    //public static int commentPermaLinkBackgroundColor;
    //public static int syncPostCount;
    //public static int syncCommentCount;
    //public static int syncCommentDepth;
    //public static int currentPostListView;

    //public static SavedAccount currentAccount;
    //public static User currentUser;
    //public static String currentAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //getDeviceId();
        //getCurrentSettings();
        //dualPaneCurrent = MyApplication.dualPane;
        setOrientation();
        MyApplication.setThemeRelatedFields();
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
            //MyApplication.colorPrimary = Color.parseColor("#181818");
            //colorPrimaryDark = Color.BLACK;
            //textColor = Color.WHITE;
            //textHintColor = getResources().getColor(R.color.hint_dark);
            //linkColor = Color.parseColor("#0080FF");
            //backgroundColor = Color.BLACK;
            //commentPermaLinkBackgroundColor = Color.parseColor("#545454");
        }
        else {
            getTheme().applyStyle(R.style.selectedTheme_day, true);
            //colorPrimaryDark = getPrimaryDarkColor();
            //textColor = Color.BLACK;
            //textHintColor = getResources().getColor(R.color.hint_light);
            //linkColor = MyApplication.colorPrimary;
            //backgroundColor = Color.WHITE;
            //commentPermaLinkBackgroundColor = Color.parseColor("#FFFFDA");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plus);

        container = (LinearLayout) findViewById(R.id.container);
        //initialized = true;
        //showHiddenPosts = false;
        //currentFontStyle = MyApplication.fontStyle;
        //currentColor = MyApplication.colorPrimary;
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
            toolbar.setPopupTheme(R.style.OverflowStyleDark);
        }
        toolbar.setBackgroundColor(MyApplication.currentColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        fm = getFragmentManager();

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
        MyApplication.currentAccount = account;
        MyApplication.currentUser = (account.loggedIn) ? new User(null, account.getUsername(), account.getModhash(), account.getCookie()) : null;
        //initNavDrawerContent();
        if(MyApplication.currentUser!=null) {
            adapter.showUserMenuItems();
            adapter.updateSubredditItems(MyApplication.currentAccount.getSubreddits());
        }
        else {
            adapter.hideUserMenuItems();
            adapter.updateSubredditItems(MyApplication.currentAccount.getSubreddits());
            //List<String> subreddits = new ArrayList<>();
            //Collections.addAll(subreddits, defaultSubredditStrings);
            //adapter.updateSubredditItems(subreddits);
        }
        //homePage();
        try {
            listFragment.changeSubreddit(listFragment.subreddit);
            String message = (MyApplication.currentUser==null) ? "Logged out" : "Logged in as " + account.getUsername();
            ToastUtils.displayShortToast(this, message);
        } catch (NullPointerException e) {}
    }

    public void homePage() {
        if(listFragment!=null) listFragment.changeSubreddit(null);
    }

    //private void getDeviceId() {
    //    deviceID = prefs.getString("deviceID", "null");
    //    if(deviceID.equals("null")) {
    //        deviceID = UUID.randomUUID().toString();
    //        SharedPreferences.Editor editor = prefs.edit();
    //        editor.putString("deviceID", deviceID);
    //        editor.apply();
    //    }
    //}

    //public static void getCurrentSettings() {
    //    currentPostListView = prefs.getInt("postListView", R.layout.post_list_item);
    //    //Log.d("geo test", "settings saved");
    //    dualPane = prefs.getBoolean("dualPane", false);
    //    //dualPane = true;
    //    screenOrientation = Integer.parseInt(prefs.getString("screenOrientation", "2"));
    //    nightThemeEnabled = prefs.getBoolean("nightTheme", false);
    //    offlineModeEnabled = prefs.getBoolean("offlineMode", false);
    //    fontStyle = Integer.parseInt(prefs.getString("fontSize", "1"));
    //    switch (fontStyle) {
    //        case 0:
    //            fontStyle = R.style.FontStyle_Small;
    //            break;
    //        case 1:
    //            fontStyle = R.style.FontStyle_Medium;
    //            break;
    //        case 2:
    //            fontStyle = R.style.FontStyle_Large;
    //            break;
    //        case 3:
    //            fontStyle = R.style.FontStyle_ExtraLarge;
    //            break;
    //    }
    //    colorPrimary = Color.parseColor(prefs.getString("toolbarColor", "#2196F3"));
    //    swipeRefresh = prefs.getBoolean("swipeRefresh", true);
    //    drawerGravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
    //    endlessPosts = prefs.getBoolean("endlessPosts", true);
    //    showNSFWpreview = prefs.getBoolean("showNSFWthumb", false);
    //    hideNSFW = prefs.getBoolean("hideNSFW", false);
    //    swipeSetting = Integer.parseInt(prefs.getString("swipeBack", "0"));
    //    switch (swipeSetting) {
    //        case 0:
    //            swipeSetting = ViewDragHelper.EDGE_LEFT;
    //            break;
    //        case 1:
    //            swipeSetting = ViewDragHelper.EDGE_RIGHT;
    //            break;
    //        case 2:
    //            swipeSetting = ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT;
    //            break;
    //        case 3:
    //            swipeSetting = ViewDragHelper.STATE_IDLE;
    //    }
    //    initialCommentCount = Integer.parseInt(prefs.getString("initialCommentCount", "100"));
    //    initialCommentDepth = (Integer.parseInt(prefs.getString("initialCommentDepth", "3")));
    //    syncPostCount = Integer.parseInt(prefs.getString("syncPostCount", "25"));
    //    syncCommentCount = Integer.parseInt(prefs.getString("syncCommentCount", "100"));
    //    syncCommentDepth = Integer.parseInt(prefs.getString("syncCommentDepth", "3"));
    //}

    //private int getPrimaryDarkColor() {
    //    String[] primaryColors = getResources().getStringArray(R.array.colorPrimaryValues);
    //    int index = 0;
    //    for(String color : primaryColors) {
    //        if(Color.parseColor(color)==MyApplication.colorPrimary) break;
    //        index++;
    //    }
    //    String[] primaryDarkColors = getResources().getStringArray(R.array.colorPrimaryDarkValues);
    //    return Color.parseColor(primaryDarkColors[index]);
    //}

    @Override
    public void onResume() {
        super.onResume();

        listFragment.loadMore = MyApplication.endlessPosts;

        if(EditSubredditsActivity.changesMade) {
            EditSubredditsActivity.changesMade = false;
            getNavDrawerAdapter().updateSubredditItems(MyApplication.currentAccount.getSubreddits());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<SavedAccount> accounts = new ArrayList<SavedAccount>();
                    boolean check = true;
                    //if(currentAccount != null) {
                        for (NavDrawerAccount accountItem : adapter.accountItems) {
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

        if(MyApplication.currentOrientation != MyApplication.screenOrientation) setOrientation();

        if(MyApplication.dualPaneCurrent != MyApplication.dualPane) {
            MyApplication.dualPaneCurrent = MyApplication.dualPane;
            toggleDualPane();
        }

        if(MyApplication.currentFontStyle != MyApplication.fontStyle) {
            MyApplication.currentFontStyle = MyApplication.fontStyle;
            getTheme().applyStyle(MyApplication.fontStyle, true);
            listFragment.redrawList();
        }

        if(!MyApplication.nightThemeEnabled && MyApplication.currentColor != MyApplication.colorPrimary) {
            MyApplication.currentColor = MyApplication.colorPrimary;
            MyApplication.linkColor = MyApplication.colorPrimary;
            toolbar.setBackgroundColor(MyApplication.colorPrimary);
            MyApplication.colorPrimaryDark = MyApplication.getPrimaryDarkColor(getResources().getStringArray(R.array.colorPrimaryValues), getResources().getStringArray(R.array.colorPrimaryDarkValues));
            drawerLayout.setStatusBarBackgroundColor(MyApplication.colorPrimaryDark);
            listFragment.colorSchemeChanged();
            adapter.notifyDataSetChanged();
            //Log.d("geo test", "main color changed");
        }

        //final int gravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        if(MyApplication.drawerGravity != drawerParams.gravity) {
            //Log.d("geo test", "drawer gravity changed");
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
        // Inflate the menu; this adds items to the action bar if it is present.
        int menuResource;
        if(MyApplication.offlineModeEnabled) menuResource = R.menu.menu_main_offline;
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
                    MyApplication.actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
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
                        if (MyApplication.actionSort) listFragment.showSortPopup(v);
                        else listFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (MyApplication.actionSort) postFragment.showSortPopup(v);
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
            //MyApplication.currentUser = null; //user connects every time main activity is started - RE-ENABLE THIS IF NEEDED
            super.onBackPressed();
        }
    }

    private PostListFragment recreateListFragment(PostListFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostListFragment newInstance = PostListFragment.newInstance(f.postListAdapter, f.subreddit, f.submissionSort, f.timeSpan, f.currentLoadType, f.hasMore);
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
