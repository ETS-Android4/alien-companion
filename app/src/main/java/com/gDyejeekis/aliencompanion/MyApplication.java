package com.gDyejeekis.aliencompanion;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Gravity;

import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import me.imid.swipebacklayout.lib.ViewDragHelper;

/**
 * Created by sound on 11/23/2015.
 */
public class MyApplication extends Application {

    public static final String lastKnownVersion = "0.2.3";

    public static final String currentVersion = "0.3";

    public static final String[] defaultSubredditStrings = {"All", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    public static final String SYNC_PROFILES_FILENAME = "SyncProfiles";

    public static final String MULTIREDDIT_FILE_PREFIX = "multi=";

    public static final int homeAsUpIndicator = R.mipmap.ic_arrow_back_white_24dp;

    public static int textHintDark;

    public static int textHintLight;

    public static String[] primaryColors;

    public static String[] primaryDarkColors;

    public static boolean actionSort = false;

    public static boolean showHiddenPosts = false;

    public static final boolean useMarkdownParsing = false; //only enable this if/when markdown parsing is ready

    public static SharedPreferences prefs;
    public static String deviceID;
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
    public static int textHintColor;
    public static int linkColor;
    public static int commentPermaLinkBackgroundColor;
    public static int syncPostCount;
    public static int syncCommentCount;
    public static int syncCommentDepth;
    public static int currentPostListView;
    public static CommentSort defaultCommentSort;
    public static CommentSort syncCommentSort;
    public static boolean useCCT;
    public static boolean handleYouTube;
    public static boolean handleImgur;
    public static boolean handleTwitter;
    public static boolean handleOtherLinks;
    public static boolean hqThumbnails;
    public static boolean noThumbnails;
    public static boolean syncThumbnails;
    public static boolean dismissImageOnTap;
    public static boolean dismissGifOnTap;
    public static boolean syncImages;
    public static int syncAlbumImgCount;
    public static boolean syncOverWifiOnly;

    //not used
    public static boolean syncWebpages = false;
    public static boolean syncGif = false;
    public static int syncImagesInCommentsCount = 0;

    //public static List<SavedAccount> savedAccounts;
    public static SavedAccount currentAccount;
    public static User currentUser;
    public static String currentAccessToken;
    public static boolean renewingToken = false;
    public static boolean accountChanges = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initStaticFields();
    }

    private void initStaticFields() {
        textHintDark = getResources().getColor(R.color.hint_dark);
        textHintLight = getResources().getColor(R.color.hint_light);
        primaryColors = getResources().getStringArray(R.array.colorPrimaryValues);
        primaryDarkColors = getResources().getStringArray(R.array.colorPrimaryDarkValues);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getDeviceId();
        getCurrentSettings();
        dualPaneCurrent = dualPane;
        currentFontStyle = fontStyle;

        //savedAccounts = readAccounts();
    }

    public static void setThemeRelatedFields() {
        if(nightThemeEnabled) {
            currentColor = Color.parseColor("#181818");
            colorPrimaryDark = Color.BLACK;
            textColor = Color.WHITE;
            textHintColor = textHintDark;
            linkColor = Color.parseColor("#0080FF");
            commentPermaLinkBackgroundColor = Color.parseColor("#545454");
        }
        else {
            currentColor = colorPrimary;
            colorPrimaryDark = getPrimaryDarkColor(primaryColors, primaryDarkColors);
            textColor = Color.BLACK;
            textHintColor = textHintLight;
            linkColor = MyApplication.colorPrimary;
            commentPermaLinkBackgroundColor = Color.parseColor("#FFFFDA");
        }
        //currentColor = colorPrimary;
    }

    private void getDeviceId() {
        deviceID = prefs.getString("deviceID", "null");
        if(deviceID.equals("null")) {
            deviceID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("deviceID", deviceID);
            editor.apply();
        }
    }

    public static void getCurrentSettings() {
        syncImages = prefs.getBoolean("syncImg", false);
        syncOverWifiOnly = prefs.getBoolean("syncWifi", true);
        syncAlbumImgCount = Integer.valueOf(prefs.getString("syncAlbum", "1"));
        dismissImageOnTap = prefs.getBoolean("imageTap", true);
        dismissGifOnTap = prefs.getBoolean("gifTap", true);
        currentPostListView = prefs.getInt("postListView", R.layout.post_list_item);
        //Log.d("geo test", "settings saved");
        dualPane = prefs.getBoolean("dualPane", false);
        //dualPane = true;
        screenOrientation = Integer.parseInt(prefs.getString("screenOrientation", "2"));
        nightThemeEnabled = prefs.getBoolean("nightTheme", false);
        offlineModeEnabled = prefs.getBoolean("offlineMode", false);
        fontStyle = Integer.parseInt(prefs.getString("fontSize", "2"));
        switch (fontStyle) {
            case 0:
                fontStyle = R.style.FontStyle_Smallest;
                break;
            case 1:
                fontStyle = R.style.FontStyle_Smaller;
                break;
            case 2:
                fontStyle = R.style.FontStyle_Small;
                break;
            case 3:
                fontStyle = R.style.FontStyle_Medium;
                break;
            case 4:
                fontStyle = R.style.FontStyle_Large;
                break;
            case 5:
                fontStyle = R.style.FontStyle_Larger;
                break;
            case 6:
                fontStyle = R.style.FontStyle_Largest;
                break;
        }
        colorPrimary = Color.parseColor(prefs.getString("toolbarColor", "#00BCD4"));
        swipeRefresh = prefs.getBoolean("swipeRefresh", true);
        drawerGravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        endlessPosts = prefs.getBoolean("endlessPosts", true);
        hqThumbnails = prefs.getBoolean("hqThumb", true);
        noThumbnails = prefs.getBoolean("noThumb", false);
        showNSFWpreview = prefs.getBoolean("showNSFWthumb", false);
        hideNSFW = prefs.getBoolean("hideNSFW", true);
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
        initialCommentDepth = (Integer.parseInt(prefs.getString("initialCommentDepth", "5")));
        int index = Integer.parseInt(prefs.getString("defaultCommentSort", "1"));
        switch (index) {
            case 1:
                defaultCommentSort = CommentSort.TOP;
                break;
            case 2:
                defaultCommentSort = CommentSort.BEST;
                break;
            case 3:
                defaultCommentSort = CommentSort.NEW;
                break;
            case 4:
                defaultCommentSort = CommentSort.OLD;
                break;
            case 5:
                defaultCommentSort = CommentSort.CONTROVERSIAL;
                break;
        }
        syncPostCount = Integer.parseInt(prefs.getString("syncPostCount", "25"));
        syncCommentCount = Integer.parseInt(prefs.getString("syncCommentCount", "100"));
        syncCommentDepth = Integer.parseInt(prefs.getString("syncCommentDepth", "5"));
        syncThumbnails = prefs.getBoolean("syncThumb", false);
        index = Integer.parseInt(prefs.getString("syncCommentSort", "1"));
        switch (index) {
            case 1:
                syncCommentSort = CommentSort.TOP;
                break;
            case 2:
                syncCommentSort = CommentSort.BEST;
                break;
            case 3:
                syncCommentSort = CommentSort.NEW;
                break;
            case 4:
                syncCommentSort = CommentSort.OLD;
                break;
            case 5:
                syncCommentSort = CommentSort.CONTROVERSIAL;
                break;
        }
        handleYouTube = prefs.getBoolean("handleYoutube", true);
        handleImgur = prefs.getBoolean("handleImgur", true);
        handleTwitter = prefs.getBoolean("handleTwitter", true);
        handleOtherLinks = prefs.getBoolean("handleOther", true);
        useCCT = prefs.getBoolean("useCCT", false);
    }

    public static int getPrimaryDarkColor(String[] primaryColors, String[] primaryDarkColors) {
        //String[] primaryColors = getResources().getStringArray(R.array.colorPrimaryValues);
        int index = 0;
        for(String color : primaryColors) {
            if(Color.parseColor(color)==MyApplication.colorPrimary) break;
            index++;
        }
        //String[] primaryDarkColors = getResources().getStringArray(R.array.colorPrimaryDarkValues);
        return Color.parseColor(primaryDarkColors[index]); //TODO: check indexoutofboundsexception
    }

    public static SavedAccount getCurrentAccount(Context context) {
        SavedAccount currentAccount = null;

        String accountName = MyApplication.prefs.getString("currentAccountName", "Logged out");

        List<SavedAccount> savedAccounts = readAccounts(context);

        if(savedAccounts!=null) {
            for(SavedAccount account : savedAccounts) {
                if(account.getUsername().equals(accountName)) {
                    currentAccount = account;
                    break;
                }
            }
        }

        return currentAccount;
    }

    public static List<SavedAccount> readAccounts(Context context) {
        try {
            FileInputStream fis = context.openFileInput(MyApplication.SAVED_ACCOUNTS_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SavedAccount> savedAccounts = (List<SavedAccount>) is.readObject();
            is.close();
            fis.close();
            return savedAccounts;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
