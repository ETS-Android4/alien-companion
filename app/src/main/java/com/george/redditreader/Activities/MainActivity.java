package com.george.redditreader.Activities;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
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
import android.view.ViewGroup;

import com.george.redditreader.Adapters.NavDrawerAdapter;
import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.LoadTasks.LoadUserTask;
import com.george.redditreader.Models.SavedAccount;
import com.george.redditreader.Utils.ScrimInsetsFrameLayout;
import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;
import com.george.redditreader.enums.MenuType;
import com.george.redditreader.Models.NavDrawer.NavDrawerHeader;
import com.george.redditreader.Models.NavDrawer.NavDrawerItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerMenuItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubredditItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubreddits;
import com.george.redditreader.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String[] defaultSubredditStrings = {"all", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "showerthoughts"};

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private PostListFragment listFragment;
    private RecyclerView drawerContent;
    private NavDrawerAdapter adapter;
    private DrawerLayout.LayoutParams drawerParams;
    private ScrimInsetsFrameLayout scrimInsetsFrameLayout;
    public static boolean showFullCommentsButton;

    public static SharedPreferences prefs;

    public static SavedAccount currentAccount;
    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plus);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        fm = getFragmentManager();

        initNavDrawer();

        setupMainFragment();
    }

    private void setupMainFragment() {
        listFragment = (PostListFragment) fm.findFragmentById(R.id.fragmentHolder);
        if(listFragment == null) {
            //Log.d("MainActivity", "Creating new fragment...");
            listFragment = new PostListFragment();
            fm.beginTransaction().add(R.id.fragmentHolder, listFragment).commit();
        }
    }

    public void changeCurrentUser(SavedAccount account) {
        currentAccount = account;
        currentUser = (account!=null) ? new User(new RedditHttpClient(), account.getUsername(), account.getPassword()) : null;
        initNavDrawerContent();
        listFragment.setSubreddit(null);
        listFragment.refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        final int gravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        if(gravity != drawerParams.gravity) {
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
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
    }

    private void initNavDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        scrimInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.black));

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

    private void initNavDrawerContent() {
        drawerContent = (RecyclerView) findViewById((R.id.drawer_content));
        drawerContent.setLayoutManager(new LinearLayoutManager(this));
        drawerContent.setHasFixedSize(true);

        adapter = new NavDrawerAdapter(this);
        adapter.add(new NavDrawerHeader());
        adapter.addAll(getMenuItems());
        adapter.add(new NavDrawerSubreddits());
        adapter.addAll(getDefaultSubredditItems());

        drawerContent.setAdapter(adapter);

        if(currentAccount != null) {
            LoadUserTask task = new LoadUserTask(this, adapter);
            task.execute();
        }
    }

    private List<NavDrawerItem> getMenuItems() {
        List<NavDrawerItem> menuItems = new ArrayList<>();
        menuItems.add(new NavDrawerMenuItem(MenuType.profile));
        menuItems.add(new NavDrawerMenuItem(MenuType.messages));
        menuItems.add(new NavDrawerMenuItem(MenuType.user));
        menuItems.add(new NavDrawerMenuItem(MenuType.subreddit));
        menuItems.add(new NavDrawerMenuItem(MenuType.settings));
        menuItems.add(new NavDrawerMenuItem(MenuType.cached));

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
            super.onBackPressed();
        }
    }

}
