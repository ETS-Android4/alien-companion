package com.gDyejeekis.aliencompanion;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;

import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.models.SavedAccount;
import com.gDyejeekis.aliencompanion.services.MessageCheckService;
import com.gDyejeekis.aliencompanion.services.PendingActionsService;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import me.imid.swipebacklayout.lib.ViewDragHelper;
import okhttp3.OkHttpClient;

/**
 * Created by sound on 11/23/2015.
 */
public class MyApplication extends Application {

    public static final String TAG = "MyApplication";

    public static int smallCardLinkBackground;

    public static boolean actionSort = false;

    public static boolean showHiddenPosts = false;

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
    public static int colorPrimaryLight;
    public static int colorSecondary;
    public static int currentPrimaryColor;
    public static boolean primaryColorInDarkTheme;

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
    public static boolean userOver18;
    public static boolean showNsfwPosts;
    public static boolean showNsfwPreviews;
    public static boolean showNsfwSuggestions;
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
    public static int syncSelfTextLinkCount;
    public static int syncCommentLinkCount;
    public static boolean syncNewPostsOnly;
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

    public static OkHttpClient okHttpClient;

    public static boolean newMessages;
    public static int messageCheckInterval;

    public static boolean pendingOfflineActions;
    public static boolean autoExecuteOfflineActions;

    //not used
    public static boolean syncGif = false;

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

        okHttpClient = new OkHttpClient();
        authenticateWithFirebase();
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
        currentBaseTheme = prefs.getInt("baseTheme", AppConstants.LIGHT_THEME);
        int[] primaryColors = getPrimaryColors(context);
        int[] primaryDarkColors = getPrimaryDarkColors(context);
        int[] primaryLightColors = getPrimarLightColors(context);
        int colorIndex = getCurrentColorIndex(primaryColors);
        upvoteColor = context.getResources().getColor(R.color.upvoteColor);
        downvoteColor = context.getResources().getColor(R.color.downvoteColor);
        switch(currentBaseTheme) {
            case AppConstants.LIGHT_THEME:
                nightThemeEnabled = false;
                currentPrimaryColor = colorPrimary;
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
            case AppConstants.MATERIAL_BLUE_THEME:
                nightThemeEnabled = true;
                currentPrimaryColor = colorPrimary;
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
            case AppConstants.MATERIAL_GREY_THEME:
                nightThemeEnabled = true;
                currentPrimaryColor = colorPrimary;
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
            case AppConstants.DARK_THEME:
                nightThemeEnabled = true;
                currentPrimaryColor = primaryColorInDarkTheme ? colorPrimary : context.getResources().getColor(R.color.darkPrimary);
                colorPrimaryDark = primaryColorInDarkTheme ? primaryDarkColors[colorIndex] : Color.BLACK;
                colorPrimaryLight = primaryColorInDarkTheme ? primaryLightColors[colorIndex] : context.getResources().getColor(R.color.darkPrimaryLight);
                textPrimaryColor = context.getResources().getColor(R.color.darkPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.darkSecondaryText);
                textHintColor = context.getResources().getColor(R.color.darkHintText);
                linkColor = primaryColorInDarkTheme ? colorPrimary : context.getResources().getColor(R.color.darkLinkText);
                textColorStickied = context.getResources().getColor(R.color.darkStickiedText);
                textColorStickiedClicked = context.getResources().getColor(R.color.darkStickiedClickedText);
                commentPermaLinkBackgroundColor = context.getResources().getColor(R.color.darkCommentHighlight);
                smallCardLinkBackground = 0;
                //smallCardLinkBackground = context.getResources().getColor(R.color.darkSmallCardLinkBackground);
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                nightThemeEnabled = true;
                currentPrimaryColor = primaryColorInDarkTheme ? colorPrimary : context.getResources().getColor(R.color.darkPrimary);
                colorPrimaryDark = primaryColorInDarkTheme ? primaryDarkColors[colorIndex] : Color.BLACK;
                colorPrimaryLight = primaryColorInDarkTheme ? primaryLightColors[colorIndex] : context.getResources().getColor(R.color.darkPrimaryLight);
                textPrimaryColor = context.getResources().getColor(R.color.lowContrastPrimaryText);
                textSecondaryColor = context.getResources().getColor(R.color.lowContrastSecondaryText);
                textHintColor = context.getResources().getColor(R.color.lowContrastHintText);
                linkColor = primaryColorInDarkTheme ? colorPrimary : context.getResources().getColor(R.color.lowContrastLinkText);
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
            case AppConstants.LIGHT_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_day, true);
                break;
            case AppConstants.MATERIAL_BLUE_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_material_blue, true);
                break;
            case AppConstants.MATERIAL_GREY_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_material_grey, true);
                break;
            case AppConstants.DARK_THEME:
                activity.getTheme().applyStyle(R.style.selectedTheme_night, true);
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
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
        String key = isMulti ? AppConstants.MULTIREDDIT_FILE_PREFIX + reddit : reddit;
        return key + AppConstants.REMEMBER_VIEW_SUFFIX;
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
        final Context appContext = getApplicationContext();
        lastKnownVersionCode = prefs.getInt("versionCode", 0);
        if (lastKnownVersionCode != AppConstants.CURRENT_VERSION_CODE) {
            SharedPreferences.Editor editor = prefs.edit();
            if (lastKnownVersionCode < AppConstants.CLEAR_APP_DATA_VERSION_CODE || lastKnownVersionCode == 0) {
                if (lastKnownVersionCode != 0) {
                    editor.clear();
                    CleaningUtils.clearAccountData(appContext);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            FilenameFilter filter = new FilenameFilter() {
                                @Override
                                public boolean accept(File file, String s) {
                                    return !s.equals("shared_prefs") && !s.equals(AppConstants.SAVED_ACCOUNTS_FILENAME);
                                }
                            };
                            CleaningUtils.clearInternalStorageData(appContext, filter);
                            CleaningUtils.clearExternalStorageData(appContext);
                        }
                    });
                }
                boolean isLargeScreen = GeneralUtils.isLargeScreen(appContext);
                editor.putBoolean("dualPane", isLargeScreen);
                editor.putBoolean("autoHideToolbar", !isLargeScreen);
            }
            if (lastKnownVersionCode < AppConstants.UPDATE_MESSAGE_VERSION_CODE) {
                editor.putBoolean("welcomeMsg", false);
            }
            editor.putInt("versionCode", AppConstants.CURRENT_VERSION_CODE);
            editor.apply();
            lastKnownVersionCode = AppConstants.CURRENT_VERSION_CODE;
        }
    }

    public static void authenticateWithFirebase() {
        final String msg = "Firebase authentication signInAnonymously: ";
        final OnCompleteListener<AuthResult> listener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, msg + "success");
                } else {
                    Log.e(TAG, msg + "failure");
                    if (task.getException()!=null)
                        task.getException().printStackTrace();
                }
            }
        };
        authenticateWithFirebase(listener);
    }

    public static void authenticateWithFirebase(OnCompleteListener<AuthResult> listener) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Log.d(TAG, "User not signed in with Firebase");
                Log.d(TAG, "Initiating Firebase authentication..");
                auth.signInAnonymously().addOnCompleteListener(listener);
            } else {
                Log.d(TAG, "User signed in with Firebase");
                Log.d(TAG, "User ID: " + user.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        dualPane = false; //prefs.getBoolean("dualPane", false); // todo: re-enable once it's properly implemented (activity abstraction and fab menus)
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
        primaryColorInDarkTheme = prefs.getBoolean("colorDarkTheme", false);
        colorPrimary = prefs.getInt("colorPrimary", Color.parseColor("#2196F3"));
        colorSecondary = prefs.getInt("colorSecondary", Color.parseColor("#D32F2F"));
        swipeRefresh = prefs.getBoolean("swipeRefresh", true);
        postFabNavigation = prefs.getBoolean("postNav", true);
        commentFabNavigation = prefs.getBoolean("commentNav", true);
        volumeNavigation = prefs.getBoolean("volumeNav", false);
        autoHidePostFab = prefs.getBoolean("autoHidePostNav", false);
        autoHideCommentFab = prefs.getBoolean("autoHideCommentNav", false);
        drawerGravity = (prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        endlessPosts = prefs.getBoolean("endlessPosts", true);
        hqThumbnails = prefs.getBoolean("hqThumb", true);
        noThumbnails = prefs.getBoolean("noThumb", false);
        userOver18 = prefs.getBoolean("userOver18", false);
        showNsfwPosts = prefs.getBoolean("showNsfwPosts", false);
        showNsfwPreviews = prefs.getBoolean("showNsfwPreviews", false);
        showNsfwSuggestions = false; //prefs.getBoolean("showNsfwSuggestions", false);
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
        defaultCommentSort = CommentSort.getCommentSort(prefs.getString("defaultCommentSort", RedditConstants.DEFAULT_COMMENT_SORT.value()));
        syncPostCount = Integer.parseInt(prefs.getString("syncPostCount", "25"));
        syncCommentCount = Integer.parseInt(prefs.getString("syncCommentCount", "100"));
        syncCommentDepth = Integer.parseInt(prefs.getString("syncCommentDepth", "5"));
        syncCommentSort = CommentSort.getCommentSort(prefs.getString("syncCommentSort", RedditConstants.DEFAULT_COMMENT_SORT.value()));
        syncSelfTextLinkCount = Integer.parseInt(prefs.getString("syncSelfTextLinks", "0"));
        syncCommentLinkCount = Integer.parseInt(prefs.getString("syncCommentLinks", "0"));
        syncThumbnails = prefs.getBoolean("syncThumb", false);
        syncNewPostsOnly = prefs.getBoolean("syncNewOnly", false);
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
            int intervalMinutes = AppConstants.OFFLINE_ACTIONS_ATTEMPT_INTERVAL_MINUTES;
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000 * intervalMinutes, 60 * 1000 * intervalMinutes, pIntent);
            Log.d(TAG, "OfflineActionsService scheduled to run every " + intervalMinutes + " minutes");
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
            List<SavedAccount> savedAccounts = (List<SavedAccount>) GeneralUtils.readObjectFromFile(new File(context.getFilesDir(), AppConstants.SAVED_ACCOUNTS_FILENAME));
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
            FileInputStream fis = context.openFileInput(AppConstants.SAVED_ACCOUNTS_FILENAME);
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

    // return true for successful init, false for unsuccessful
    public static boolean checkAccountInit(Context context, HttpClient httpClient) {
        final int maxInitDuration = 750; // max duration of current account read attempts in milliseconds
        final long startTime = System.currentTimeMillis();
        try {
            while (currentAccount == null) {
                if ((System.currentTimeMillis()-startTime) > maxInitDuration) {
                    Log.e(TAG, "Account init timed out");
                    return false;
                }
                currentAccount = MyApplication.getCurrentAccount(context);
            }

            currentAccessToken = currentAccount.getToken().accessToken;
            if (currentAccount.loggedIn) {
                currentUser = new User(httpClient, currentAccount.getUsername(), currentAccount.getToken());
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown during account init");
            e.printStackTrace();
        }
        return false;
    }

}
