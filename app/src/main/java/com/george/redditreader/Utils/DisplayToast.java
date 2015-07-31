package com.george.redditreader.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by George on 6/19/2015.
 */
public class DisplayToast {

    public static final String LOADING_POSTS_ERROR = "Error Loading Posts";
    public static final String LOADING_COMMENTS_ERROR = "Error Loading Comments";
    public static final String LOADING_USER_ERROR = "Error Loading User";
    public static final String NO_RESULTS_FOUND = "No results for ";
    public static final String SUBREDDIT_NOT_FOUND = "Could not find subreddit";

    public static void postsLoadError(Context context) {
        try {
            Toast toast = Toast.makeText(context, LOADING_POSTS_ERROR, Toast.LENGTH_SHORT);
            toast.show();
        } catch (NullPointerException e) {}
    }

    public static void commentsLoadError(Context context) {
        try {
            Toast toast = Toast.makeText(context, LOADING_COMMENTS_ERROR, Toast.LENGTH_SHORT);
            toast.show();
        } catch (NullPointerException e) {}
    }

    public static void userLoadError(Context context) {
        try {
            Toast toast = Toast.makeText(context, LOADING_USER_ERROR, Toast.LENGTH_SHORT);
            toast.show();
        } catch (NullPointerException e) {}
    }

    public static void subredditNotFound(Context context) {
        try {
            Toast toast = Toast.makeText(context, SUBREDDIT_NOT_FOUND, Toast.LENGTH_SHORT);
            toast.show();
        } catch (NullPointerException e) {}
    }

    public static void noResults(Context context, String query) {
        try {
            Toast toast = Toast.makeText(context, NO_RESULTS_FOUND + query, Toast.LENGTH_SHORT);
            toast.show();
        } catch (NullPointerException e) {}
    }
}
