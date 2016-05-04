package com.gDyejeekis.aliencompanion;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;

import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.Services.MessageCheckService;
import com.gDyejeekis.aliencompanion.Services.PendingActionsService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

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

    public static final String currentVersion = "0.4";

    public static final int currentVersionCode = 12;

    public static final boolean showWelcomeMsgThisVersion = true;

    public static final boolean deleteAppDataThisVersion = true;

    public static final String[] defaultSubredditStrings = {"All", "pics", "videos", "gaming", "technology", "movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    public static final String SYNC_PROFILES_FILENAME = "SyncProfiles";

    public static final String OFFLINE_USER_ACTIONS_FILENAME = "OfflineActions";

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
    public static boolean syncWebpages;

    public static boolean newMessages;
    //public static boolean messageServiceActive;
    public static int messageCheckInterval;

    public static boolean pendingOfflineActions;
    //public static boolean offlineActionsServiceActive;
    public static int offlineActionsInterval;

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

    @Override
    public void onCreate() {
        super.onCreate();
        initStaticFields();

        scheduleOfflineActionsService(getApplicationContext());
    }

    private void initStaticFields() {
        textHintDark = getResources().getColor(R.color.hint_dark);
        textHintLight = getResources().getColor(R.color.hint_light);
        primaryColors = getResources().getStringArray(R.array.colorPrimaryValues);
        primaryDarkColors = getResources().getStringArray(R.array.colorPrimaryDarkValues);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkAppVersion();
        getDeviceId();
        getCurrentSettings();
        dualPaneCurrent = dualPane;
        currentFontStyle = fontStyle;
        currentFontFamily = fontFamily;

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

    private void checkAppVersion() {
        lastKnownVersionCode = prefs.getInt("versionCode", 0);
        if(lastKnownVersionCode!=currentVersionCode) {
            lastKnownVersionCode = currentVersionCode;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("versionCode", currentVersionCode);
            if(deleteAppDataThisVersion) {
                prefs.edit().clear().commit();
                clearApplicationData();
            }
            if(showWelcomeMsgThisVersion) {
                editor.putBoolean("welcomeMsg", false);
            }
            editor.commit();
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
        syncWebpages = prefs.getBoolean("syncWeb", false);
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
        fontFamily = Integer.parseInt(prefs.getString("fontFamily", "0"));
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

        //messageServiceActive = prefs.getBoolean("messageCheckActive", false);
        newMessages = prefs.getBoolean("newMessages", false);
        messageCheckInterval = Integer.valueOf(prefs.getString("messageCheckInterval", "15"));

        //offlineActionsServiceActive = prefs.getBoolean("offlineActionsActive", false);
        pendingOfflineActions = prefs.getBoolean("pendingActions", false);
        offlineActionsInterval = Integer.valueOf(prefs.getString("offlineActionsInterval", "15"));
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

    public static void scheduleMessageCheckService(final Context context) {
        Log.d(TAG, "Scheduling MessageCheckService..");
        if(MyApplication.currentAccount == null) {
            MyApplication.currentAccount = getCurrentAccount(context);
        }

        //int timer = 0;
        //while(MyApplication.currentAccount==null) {
        //    if(timer >= 5000) {
        //        Log.d(TAG, "Scheduling of MessageCheckService failed");
        //        return;
        //    }
        //    timer += 100;
        //    SystemClock.sleep(100);
        //}

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
        if(offlineActionsInterval != -1) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, 60 * 1000 * offlineActionsInterval, pIntent);
            Log.d(TAG, "OfflineActionsService scheduled to run every " + offlineActionsInterval + " minutes");
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
}
