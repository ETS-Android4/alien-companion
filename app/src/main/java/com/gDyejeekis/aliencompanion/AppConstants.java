package com.gDyejeekis.aliencompanion;

/**
 * Created by George on 2/24/2018.
 */

public class AppConstants {

    public static final String CURRENT_VERSION_NAME = "1.0.8";

    public static final int CURRENT_VERSION_CODE = 1008;

    public static final boolean SHOW_UPDATE_MESSAGE = false;

    public static final int UPDATE_MESSAGE_VERSION_CODE = 1000;

    public static final int CLEAR_APP_DATA_VERSION_CODE = 1000;

    public static final int NAV_DRAWER_CLOSE_TIME = 200;

    public static final int FAB_HIDE_ON_SCROLL_THRESHOLD = 1;

    public static final long IMAGES_CACHE_LIMIT = 50 * 1024 * 1024;

    public static final String SAVED_ACCOUNTS_FILENAME = "SavedAccounts";

    public static final String SYNC_PROFILES_FILENAME = "SyncProfiles";

    public static final String FILTER_PROFILES_FILENAME = "FilterProfiles";

    public static final String OFFLINE_USER_ACTIONS_FILENAME = "OfflineActions";

    public static final String SAVED_PICTURES_PUBLIC_DIR_NAME = "AlienCompanion";

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

    public static final int LIGHT_THEME = 0;

    public static final int MATERIAL_BLUE_THEME = 1;

    public static final int MATERIAL_GREY_THEME = 2;

    public static final int DARK_THEME = 3;

    public static final int DARK_THEME_LOW_CONTRAST = 4;

    public static final boolean USER_MARKDOWN_PARSER = false; //only enable this if/when markdown parsing is ready

    public static final int OFFLINE_ACTIONS_ATTEMPT_INTERVAL_MINUTES = 5; // interval (in minutes) beween repeated attempts to execute any failed offline actions

    public static final String BASE_64_ENCODED_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiNimLYQIP/Rfr7QNxczwzhpUyqNRxLPd3P5Tucmtl/UvM5ONcUsyS7FNsTAj8toZjwtfwhdLlg5V7BGiK+JCZLlKLrifW8jTYsz65gCgDGgj7WT9xPB6sdXx/hm/WuxiVFrPJv58/mp9t4hBU09bNRJFaQ3PyjyEnM57HOvpYuoIQ23PIH0Cij4dlZ+nmW/IbZ9bohsmr5wm/0JU338WLrMlM52gdxaUw8annIgNVVuNHXZOeNj3Ms5Vb4HAfYUYJB1hGiF+5ksD7h+5pEeB7av62rYiV2iWo3jfxZ8YPVSk5HhDFbH+wHZc9HC68+g0KnJYYKSfAPNlwASEiMDfSwIDAQAB";

    public static final String[] PROFANE_WORDS = {};

}
