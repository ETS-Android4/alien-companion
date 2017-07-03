package com.gDyejeekis.aliencompanion;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;

import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.models.SavedAccount;
import com.gDyejeekis.aliencompanion.services.MessageCheckService;
import com.gDyejeekis.aliencompanion.services.PendingActionsService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
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

    public static final String TAG = "MyApplication";

    public static final String currentVersion = "1.0";

    public static final int currentVersionCode = 100;

    public static final int showWelcomeMsgVersionCode = 30;

    public static final int clearAppDataVersionCode = 20;

    public static final String[] defaultSubredditStrings = {"All", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final int FAB_HIDE_ON_SCROLL_THRESHOLD = 10;

    public static final int TOOLBAR_HIDE_ON_SCROLL_THRESHOLD = 10;

    public static final long IMAGES_CACHE_LIMIT = 50 * 1024 * 1024;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    public static final String SYNC_PROFILES_FILENAME = "SyncProfiles";

    public static final String FILTER_PROFILES_FILENAME = "FilterProfiles";

    public static final String OFFLINE_USER_ACTIONS_FILENAME = "OfflineActions";

    public static final String SYNCED_MEDIA_DIR_NAME = "Media";

    public static final String SYNCED_ARTICLES_DIR_NAME = "Articles";

    public static final String SYNCED_REDDIT_DATA_DIR_NAME = "RedditData";

    public static final String SYNCED_THUMBNAILS_DIR_NAME = "Thumbs";

    public static final String INDIVIDUALLY_SYNCED_DIR_NAME = "synced";

    public static final String SYNCED_ARTICLE_DATA_SUFFIX = "-article";

    public static final String SYNCED_ARTICLE_IMAGE_SUFFIX = "-image.jpg";

    public static final String SYNCED_POST_LIST_SUFFIX = "-posts";

    public static final String SYNCED_THUMBNAIL_SUFFIX = "-thumb";

    public static final String MULTIREDDIT_FILE_PREFIX = "multi=";

    public static final String IMGUR_INFO_FILE_NAME = "-imgurInfo";

    public static final String REMEMBER_VIEW_SUFFIX = "-view";

    public static final int homeAsUpIndicator = R.drawable.ic_arrow_back_white_24dp;

    public static final int LIGHT_THEME = 0;

    public static final int MATERIAL_BLUE_THEME = 1;

    public static final int MATERIAL_GREY_THEME = 2;

    public static final int DARK_THEME = 3;

    public static final int DARK_THEME_LOW_CONTRAST = 4;

    public static int smallCardLinkBackground;

    public static boolean actionSort = false;

    public static boolean showHiddenPosts = false;

    public static final boolean useMarkdownParsing = false; //only enable this if/when markdown parsing is ready

    public static SharedPreferences prefs;
    public static int lastKnownVersionCode;
    public static String deviceID;
    public static boolean showedWelcomeMessage;
    public static boolean nightThemeEnabled;
    public static boolean offlineModeEnabled;
    public static boolean dualPane;
    public static boolean dualPaneCurrent;
    public static boolean dualPaneActive;
    public static int screenOrientation;
    public static int currentOrientation;
    public static int fontStyle;
    public static int fontFamily;
    public static int currentFontStyle;
    public static int currentFontFamily;
    public static int colorPrimary;
    public static boolean colorInDarkThemes;
    public static int colorPrimaryDark;
    public static int colorPrimaryLight;
    public static int colorSecondary;
    public static int currentColor; // TODO: 2/11/2017 prolly delet this
    // TODO: 2/24/2017 prolly move these to main activity
    public static boolean colorPrimaryChanged;
    public static boolean colorSecondaryChanged;
    public static boolean fabPostNavChanged;
    public static boolean fabCommentNavChanged;

    public static int swipeSetting;
    public static boolean swipeRefresh;
    public static boolean postFabNavigation;
    public static boolean commentFabNavigation;
    public static boolean volumeNavigation;
    public static boolean autoHidePostFab;
    public static boolean autoHideCommentFab;
    public static boolean autoHideToolbar;
    public static int drawerGravity;
    public static boolean endlessPosts;
    public static boolean showNSFWpreview;
    public static boolean hideNSFW;
    public static int initialCommentCount;
    public static int initialCommentDepth;
    public static int textPrimaryColor;
    public static int textSecondaryColor;
    public static int textHintColor;
    public static int linkColor;
    public static int textColorStickied;
    public static int textColorStickiedClicked;
    public static int upvoteColor;
    public static int downvoteColor;
    public static int commentPermaLinkBackgroundColor;
    public static int syncPostCount;
    public static int syncCommentCount;
    public static int syncCommentDepth;
    public static int syncCommentLinkCount;
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
    public static boolean dismissInfoOnTap;
    public static boolean syncImages;
    public static int syncAlbumImgCount;
    public static boolean syncVideo;
    public static boolean syncOverWifiOnly;
    public static boolean syncWebpages;
    public static boolean preferExternalStorage;
    public static boolean longTapSwitchMode;
    public static boolean handleArticles;
    public static boolean rememberPostListView;
    public static boolean askedRememberPostView;
    public static boolean disableAnimations;
    public static boolean isLargeScreen;
    public static boolean isVeryLargeScreen;

    public static int currentBaseTheme;

    public static boolean newMessages;
    //public static boolean messageServiceActive;
    public static int messageCheckInterval;

    public static boolean pendingOfflineActions;
    //public static boolean offlineActionsServiceActive;
    public static final int offlineActionsInterval = 5; //how often (minutes) the app should attempt to execute any failed offline actions
    public static boolean autoExecuteOfflineActions;

    //not used
    public static boolean syncGif = false;
    public static int syncImagesInCommentsCount = 0;

    //public static List<SavedAccount> savedAccounts;

    //this is horrible
    public static SavedAccount currentAccount;
    public static User currentUser;
    public static String currentAccessToken;
    public static boolean renewingToken = false;
    public static boolean renewingUserToken = false;
    public static boolean accountChanges = false;
    public static String accountUsernameChanged;
    public static String newAccountAccessToken;

    public static boolean themeFieldsInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initStaticFields();

        scheduleOfflineActionsService(getApplicationContext());
    }

    private void initStaticFields() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkAppVersion();
        getDeviceId();
        getCurrentSettings();
        dualPaneCurrent = dualPane;
        currentFontStyle = fontStyle;
        currentFontFamily = fontFamily;

        //savedAccounts = readAccounts();
    }

    public static int[] getPrimaryColors(Context context) {
        return context.getResources().getIntArray(R.array.colorPrimaryValues);
    }

    public static int[] getPrimaryDarkColors(Context context) {
        return context.getResources().getIntArray(R.array.colorPrimaryDarkValues);
    }

    public static int[] getPrimarLightColors(Context context) {
        return context.getResources().getIntArray(R.array.colorPrimaryLightValues);
    }

    public static void setThemeRelatedFields(Context context) {
        themeFieldsInitialized = true;
        isLargeScreen = GeneralUtils.isLargeScreen(context);
        isVeryLargeScreen = GeneralUtils.isVeryLargeScreen(context);
        currentBaseTheme = prefs.getInt("baseTheme", LIGHT_THEME);
        int[] primaryColors = getPrimaryColors(context);
        int[] primaryDarkColors = getPrimaryDarkColors(context);
        int[] primaryLightColors = getPrimarLightColors(context);
        int colorIndex = getCurrentColorIndex(primaryColors);
        upvoteColor = context.getResources().getColor(R.color.upvoteColor);
        downvoteColor = context.getResources().getColor(R.color.downvoteColor);
        switch(currentBaseTheme) {
            case LIGHT_THEME:
                nightThemeEnabled = false;
                currentColor = colorPrimary;
                colorPrimaryDark = primaryDarkColors[colorIndex];
                colorPrimaryLight = primaryLightColors[colorIndex];
                textPrimaryColor = context.getResources().getColor(R.color.lightPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.lightSecondaryText);
                textHintColor = context.getResources().getColor(R.color.lightHintText);
                linkColor = MyApplication.colorPrimary;
                textColorStickied = context.getResources().getColor(R.color.lightStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.lightStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.lightCommentHighlight);
                smallCardLinkBackground = context.getResources().getColor(R.color.lightSmallCardLinkBackground);
                break;
            case MATERIAL_BLUE_THEME:
                nightThemeEnabled = true;
                currentColor = colorPrimary;
                colorPrimaryDark = primaryDarkColors[colorIndex];
                colorPrimaryLight = primaryLightColors[colorIndex];
                textPrimaryColor = context.getResources().getColor(R.color.materialBluePrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.materialBlueSecondaryText);
                textHintColor = context.getResources().getColor(R.color.materialBlueHintText);
                linkColor = MyApplication.colorPrimary;
                textColorStickied = context.getResources().getColor(R.color.materialBlueStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.materialBlueStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.materialBlueCommentHighlight);
                smallCardLinkBackground = context.getResources().getColor(R.color.materialBlueSmallCardLinkBackground);
                break;
            case MATERIAL_GREY_THEME:
                nightThemeEnabled = true;
                currentColor = colorPrimary;
                colorPrimaryDark = primaryDarkColors[colorIndex];
                colorPrimaryLight = primaryLightColors[colorIndex];
                textPrimaryColor = context.getResources().getColor(R.color.materialGreyPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.materialGreySecondaryText);
                textHintColor = context.getResources().getColor(R.color.materialGreyHintText);
                linkColor = MyApplication.colorPrimary;
                textColorStickied = context.getResources().getColor(R.color.materialGreyStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.materialGreyStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.materialGreyCommentHighlight);
                smallCardLinkBackground = context.getResources().getColor(R.color.materialGreySmallCardLinkBackground);
                break;
            case DARK_THEME:
                nightThemeEnabled = true;
                currentColor = colorInDarkThemes ? colorPrimary : context.getResources().getColor(R.color.darkPrimary);
                colorPrimaryDark = colorInDarkThemes ? primaryDarkColors[colorIndex] : Color.BLACK;
                colorPrimaryLight = colorInDarkThemes ? primaryLightColors[colorIndex] : context.getResources().getColor(R.color.darkPrimaryLight);
                textPrimaryColor = context.getResources().getColor(R.color.darkPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.darkSecondaryText);
                textHintColor = context.getResources().getColor(R.color.darkHintText);
                linkColor = colorInDarkThemes ? colorPrimary : context.getResources().getColor(R.color.darkLinkText);
                textColorStickied = context.getResources().getColor(R.color.darkStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.darkStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.darkCommentHighlight);
                smallCardLinkBackground = 0;
                //smallCardLinkBackground = context.getResources().getColor(R.color.darkSmallCardLinkBackground);
                break;
            case DARK_THEME_LOW_CONTRAST:
                nightThemeEnabled = true;
                currentColor = colorInDarkThemes ? colorPrimary : context.getResources().getColor(R.color.darkPrimary);
                colorPrimaryDark = colorInDarkThemes ? primaryDarkColors[colorIndex] : Color.BLACK;
                colorPrimaryLight = colorInDarkThemes ? primaryLightColors[colorIndex] : context.getResources().getColor(R.color.darkPrimaryLight);
                textPrimaryColor = context.getResources().getColor(R.color.lowContrastPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.lowContrastSecondaryText);
                textHintColor = context.getResources().getColor(R.color.lowContrastHintText);
                linkColor = colorInDarkThemes ? colorPrimary : context.getResources().getColor(R.color.lowContrastLinkText);
                textColorStickied = context.getResources().getColor(R.color.lowContrastStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.lowContrastStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.darkCommentHighlight);
                smallCardLinkBackground = 0;
                //smallCardLinkBackground = context.getResources().getColor(R.color.darkSmallCardLinkBackground);
                break;
        }
    }

    public static void applyCurrentTheme(Activity activity) {
        if(!themeFieldsInitialized) {
            Log.d(TAG, "Theme related fields not initialized, initializing..");
            setThemeRelatedFields(activity);
        }
        activity.getTheme().applyStyle(MyApplication.fontStyle, true);
        activity.getTheme().applyStyle(MyApplication.fontFamily, true);
        setPendingTransitions(activity);
        if(nightThemeEnabled) {
            activity.getTheme().applyStyle(R.style.PopupDarkTheme, true);
        }
        switch(currentBaseTheme) {
            case LIGHT_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_day, true);
                break;
            case MATERIAL_BLUE_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_material_blue, true);
                break;
            case MATERIAL_GREY_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_material_grey, true);
                break;
            case DARK_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_night, true);
                break;
            case DARK_THEME_LOW_CONTRAST:
                activity.getTheme().applyStyle(R.style.selectedTheme_night_low_contrast, true);
                break;
        }
    }

    public static void setPendingTransitions(Activity activity) {
        if(disableAnimations) {
            activity.overridePendingTransition(-1, -1);
        }
        else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    public static int getCurrentColorIndex(int[] primaryColors) {
        int index = 0;
        for (int color : primaryColors) {
            if (color == MyApplication.colorPrimary) {
                return index;
            }
            index++;
        }

        return 7;
    }

    public static String getSubredditSpecificViewKey(String reddit, boolean isMulti) {
        String key = isMulti ? MULTIREDDIT_FILE_PREFIX + reddit : reddit;
        return key + MyApplication.REMEMBER_VIEW_SUFFIX;
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

    private void checkAppVersion() {
        lastKnownVersionCode = prefs.getInt("versionCode", 0);
        if(lastKnownVersionCode!=currentVersionCode) {
            SharedPreferences.Editor editor = prefs.edit();
            if(lastKnownVersionCode < clearAppDataVersionCode) {
                editor.clear();
                clearApplicationData();
                editor.putBoolean("dualPane", GeneralUtils.isLargeScreen(getApplicationContext()));
            }
            if(lastKnownVersionCode < showWelcomeMsgVersionCode) {
                editor.putBoolean("welcomeMsg", false);
            }
            editor.putInt("versionCode", currentVersionCode);
            editor.commit();
            lastKnownVersionCode = currentVersionCode;
        }
    }

    private void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.d(TAG, "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static void getCurrentSettings() {
        showedWelcomeMessage = prefs.getBoolean("welcomeMsg", false);
        syncImages = prefs.getBoolean("syncImg", false);
        syncVideo = prefs.getBoolean("syncVideo", false);
        syncWebpages = prefs.getBoolean("syncWeb", false);
        syncOverWifiOnly = prefs.getBoolean("syncWifi", true);
        preferExternalStorage = prefs.getBoolean("prefExternal", false);
        syncAlbumImgCount = Integer.valueOf(prefs.getString("syncAlbum", "5"));
        autoHideToolbar = prefs.getBoolean("autoHideToolbar", true);
        dismissImageOnTap = prefs.getBoolean("imageTap", true);
        dismissGifOnTap = prefs.getBoolean("gifTap", true);
        dismissInfoOnTap = prefs.getBoolean("infoTap", true);
        longTapSwitchMode = prefs.getBoolean("longTapSwitch", true);
        currentPostListView = Integer.parseInt(prefs.getString("defaultView", String.valueOf(PostViewType.list.value())));
        rememberPostListView = prefs.getBoolean("rememberView", false);
        askedRememberPostView = prefs.getBoolean("askedRememberView", false);
        dualPane = prefs.getBoolean("dualPane", false);
        screenOrientation = Integer.parseInt(prefs.getString("screenOrientation", "4"));
        disableAnimations = prefs.getBoolean("noAnimation", false);
        offlineModeEnabled = prefs.getBoolean("offlineMode", false);
        fontStyle = Integer.parseInt(prefs.getString("fontSize", "3"));
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
        fontFamily = Integer.parseInt(prefs.getString("fontFamily", "1"));
        switch (fontFamily) {
            case 0:
                fontFamily = R.style.FontFamily_SansSerifRegular;
                break;
            case 1:
                fontFamily = R.style.FontFamily_SansSerifLight;
                break;
            case 2:
                fontFamily = R.style.FontFamily_SansSerifCondensed;
                break;
            case 3:
                fontFamily = R.style.FontFamily_SansSerifCondensedLight;
                break;
        }
        colorInDarkThemes = prefs.getBoolean("colorDarkTheme", false);
        colorPrimary = prefs.getInt("colorPrimary", Color.parseColor("#00BCD4"));
        colorSecondary = prefs.getInt("colorSecondary", Color.parseColor("#FF5252"));
        swipeRefresh = prefs.getBoolean("swipeRefresh", true);
        postFabNavigation = prefs.getBoolean("postNav", true);
        commentFabNavigation = prefs.getBoolean("commentNav", true);
        volumeNavigation = prefs.getBoolean("volumeNav", false);
        autoHidePostFab = prefs.getBoolean("autoHidePostNav", true);
        autoHideCommentFab = prefs.getBoolean("autoHideCommentNav", true);
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
        syncCommentLinkCount = Integer.parseInt(prefs.getString("syncCommentLinks", "0"));
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
        handleArticles = false;//prefs.getBoolean("handleArticles", false);

        newMessages = prefs.getBoolean("newMessages", false);
        messageCheckInterval = Integer.valueOf(prefs.getString("messageCheckInterval", "15"));

        pendingOfflineActions = prefs.getBoolean("pendingActions", false);
        //offlineActionsInterval = Integer.valueOf(prefs.getString("offlineActionsInterval", "15"));
        autoExecuteOfflineActions = prefs.getBoolean("autoOfflineActions", true);
    }

    public static void scheduleMessageCheckService(final Context context) {
        Log.d(TAG, "Scheduling MessageCheckService..");
        checkAccountInit(context, new PoliteRedditHttpClient());

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MessageCheckService.class);
        PendingIntent pIntent = PendingIntent.getService(context, MessageCheckService.SERVICE_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if(MyApplication.currentAccount.loggedIn && messageCheckInterval != -1) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 1000, 60 * 1000 * messageCheckInterval, pIntent);
            Log.d(TAG, "MessegeCheckService scheduled to run every " + messageCheckInterval + " minutes");
        }
        else {
            manager.cancel(pIntent);
            Log.d(TAG, "MessageCheckService unscheduled");
        }
    }

    public static void scheduleOfflineActionsService(final Context context) {
        Log.d(TAG, "Scheduling OfflineActionsService..");
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PendingActionsService.class);
        PendingIntent pIntent = PendingIntent.getService(context, PendingActionsService.SERVICE_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if(autoExecuteOfflineActions && pendingOfflineActions) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000 * offlineActionsInterval, 60 * 1000 * offlineActionsInterval, pIntent);
            Log.d(TAG, "OfflineActionsService scheduled to run every " + offlineActionsInterval + " minutes");
            Log.d(TAG, "..until all pending offline actions are successfully executed");
        }
        else {
            manager.cancel(pIntent);
            Log.d(TAG, "OfflineActionsService unscheduled");
        }
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

    public static SavedAccount getSavedAccountByName(Context context, String accountName) {
        try {
            List<SavedAccount> savedAccounts = (List<SavedAccount>) GeneralUtils.readObjectFromFile(new File(context.getFilesDir(), MyApplication.SAVED_ACCOUNTS_FILENAME));
            for(SavedAccount account : savedAccounts) {
                if(account.getUsername().equals(accountName)) {
                    return account;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveAccountChanges(Context context, String accountName) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void checkAccountInit(Context context, HttpClient httpClient) {
        if (MyApplication.currentAccount == null) {
            MyApplication.currentAccount = MyApplication.getCurrentAccount(context);
            MyApplication.currentAccessToken = MyApplication.currentAccount.getToken().accessToken;
            if(MyApplication.currentAccount.loggedIn) {
                MyApplication.currentUser = new User(httpClient, MyApplication.currentAccount.getUsername(), MyApplication.currentAccount.getToken());
            }
        }
    }

}
