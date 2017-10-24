package com.gDyejeekis.aliencompanion.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.gDyejeekis.aliencompanion.activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.activities.MediaActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 6/11/2015.
 */
public class LinkHandler {

    public static final String TAG = "LinkHandler";

    private static final String YOUTUBE_API_KEY = "AIzaSyDAqkwJF2o2QmGsoyj-yPP8uCqMxytm15Y"; //TODO: get different api key before release

    public static final String ARTICLE_API_KEY = "2f271e88a87b7bd125f99988d5daf2f3";

    public static final String GIPHY_PUBLIC_BETA_API_KEY = "dc6zaTOxFJmzC";

    public static final String GIPHY_PRODUCTION_API_KEY = "";

    public static final String GIPHY_API_KEY = GIPHY_PUBLIC_BETA_API_KEY;

    private Context context;
    private Submission post;
    private String url;
    private String domain;

    private boolean browserActive; // flag that checks if an instance of browserFragment is already open and visible

    public LinkHandler(Context context, Submission post) {
        this.context = context;
        this.post = post;
        url = post.getUrl();
        domain = post.getDomain();
    }

    public LinkHandler(Context context, String url, String domain) {
        this.context = context;
        this.url = url;
        this.domain = domain;
    }

    public LinkHandler(Context context, String url) {
        this.context = context;
        if(LinkUtils.isEmailAddress(url)) {
            this.url = url;
            this.domain = url.substring(url.indexOf("@"));
        }
        else if(LinkUtils.isIntentLink(url)) {
            this.url = url;
            this.domain = "";
        }
        else {
            if (!url.matches("http(s)?\\:\\/\\/.*")) {
                url = "http://" + url;
            }
            this.url = url;
            try {
                this.domain = LinkUtils.getDomainName(url);
            } catch (URISyntaxException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    // returns false if the url is to be handled by the webview, true for custom handling
    public boolean handleIt() {
        boolean setImplicitViewIntent = false;
        try {
            Intent intent = null;
            Activity activity = (Activity) context;

            Log.d(TAG, "FULL URL: " + url);
            Log.d(TAG, "DOMAIN: " + domain);
            if (domain == null) {
                intent = getNoDomainIntent(activity, url);
            }
            else {
                String domainLC = domain.toLowerCase();
                String urlLC = url.toLowerCase();
                if (domainLC.contains("youtube.com") || domainLC.equals("youtu.be")) {
                    if (MyApplication.handleYouTube) {
                        if(urlLC.contains("playlist")) {
                            String playlistId = LinkUtils.getYoutubePlaylistId(url);
                            if(playlistId.equals("")) {
                                Log.e(TAG, "Unable to validate YouTube playlist ID");
                                setImplicitViewIntent = true;
                            }
                            else {
                                Log.d(TAG, "YouTube playlist ID: " + playlistId);
                                intent = YouTubeStandalonePlayer.createPlaylistIntent(activity, YOUTUBE_API_KEY, playlistId, 0, 0, true, true);
                            }

                        }
                        else {
                            String videoId = LinkUtils.getYoutubeVideoId(url);
                            if(videoId.equals("")) {
                                Log.e(TAG, "Unable to validate YouTube video ID");
                                setImplicitViewIntent = true;
                            }
                            else {
                                Log.d(TAG, "YouTube video ID: " + videoId);
                                int time = LinkUtils.getYoutubeVideoTime(url);
                                intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
                            }
                        }
                    }
                    else setImplicitViewIntent = true;
                }
                else if(domainLC.contains("imgur.com")) {
                    if(MyApplication.handleImgur) {
                        //startInAppBrowser(activity, post, url, domain);
                        intent = getMediaActivityIntent(activity, url, domain);
                    }
                    else setImplicitViewIntent = true;
                }
                else if(domainLC.equals("v.redd.it")) {
                    if(post == null) { // no post object meaning this is a link from text (e.g. comments)
                        // open media viewer in offline mode (only video is synced from text url) and comments in online mode
                        intent = MyApplication.offlineModeEnabled ? new Intent(context, MediaActivity.class): new Intent(context, PostActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("domain", domainLC);
                    }
                    else if(post.getRedditVideo()!=null) { // link is from post object so open media viewer
                        intent = new Intent(context, MediaActivity.class);
                        intent.putExtra("redditVideo", post.getRedditVideo());
                    }
                    else {
                        ToastUtils.showToast(context, "Api error - reddit video url not found");
                    }
                }
                else if(urlLC.endsWith(".png") || urlLC.endsWith(".jpg") || urlLC.endsWith(".jpeg") || domainLC.equals("i.reddituploads.com") || domainLC.equals("i.redditmedia.com")
                        || domainLC.contains("gyazo.com")) {
                    intent = getMediaActivityIntent(activity, url, domain);
                }
                else if(domainLC.contains("gfycat.com") || domainLC.contains("giphy.com") || urlLC.endsWith(".gif") || urlLC.endsWith(".gifv")/* || urlLC.endsWith(".webm") || urlLC.endsWith(".mp4")*/) {
                    intent = getMediaActivityIntent(activity, url, domain);
                }
                else if(domainLC.contains("streamable.com") || urlLC.endsWith(".mp4")) {
                    intent = getMediaActivityIntent(activity, url, domain);
                }
                else if(domainLC.equals("twitter.com")) {
                    if(MyApplication.handleTwitter) {
                        if(!browserActive) {
                            startInAppBrowser(activity, post, url, domain);
                        }
                    }
                    else setImplicitViewIntent = true;
                }
                else if (domainLC.matches("^(\\w+\\.)?reddit\\.com") || domainLC.equals("redd.it")) {
                    // case post link
                    if(LinkUtils.isRedditPostUrl(urlLC)) {
                        intent = new Intent(activity, PostActivity.class);
                        intent.putExtra("url", url);
                    }
                    // case (subreddit).reddit.com link
                    else if(domainLC.matches("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com")) {
                        Matcher matcher = Pattern.compile("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com").matcher(domainLC);
                        if(matcher.find()) {
                            intent = new Intent(activity, SubredditActivity.class);
                            intent.putExtra("subreddit", matcher.group(1));
                        }
                    }
                    // case user/subreddit link
                    else if(urlLC.matches("^(?:https?\\:\\/\\/)?(?:www\\.)?(?:reddit\\.com)?\\/(r|u|user)\\/(\\w+)")) {
                        intent = getUserSubredditIntent(activity, urlLC);
                    }
                    // case other reddit link not handled by the app natively
                    else if(urlLC.contains("/wiki/") || urlLC.contains("/about/") || urlLC.contains("/live/")) {
                        if (MyApplication.handleOtherLinks) {
                            if(!browserActive) {
                                startInAppBrowser(activity, post, url, domain);
                            }
                        }
                        else setImplicitViewIntent = true;
                    }
                }
                // if in offline mode start browser activity to look for synced artocle
                else if(MyApplication.offlineModeEnabled && LinkUtils.isArticleLink(urlLC, domainLC)) { // TODO: 7/30/2017 maybe check if synced article exists here instead of checking the link
                    startBrowserActivity(activity, post, url, domain);
                }
                else if(LinkUtils.isIntentLink(url)) {
                    return handleAppLink();
                }
                else if(LinkUtils.isEmailAddress(url)) {
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    activity.startActivity(intent);
                    return true;
                }
                else if (MyApplication.handleOtherLinks && !domainLC.equals("play.google.com") && !urlLC.endsWith(".pdf")) {
                    if(!browserActive) {
                        startInAppBrowser(activity, post, url, domain);
                    }
                }
                else {
                    setImplicitViewIntent = true;
                }
            }

            if(setImplicitViewIntent) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            if(intent!=null) {
                activity.startActivity(intent);
                return true;
            }

        } catch (ActivityNotFoundException e) {
            ToastUtils.showToast(context, "No activity found to handle Intent");
            e.printStackTrace();
        }
        return false;
    }

    private boolean handleAppLink() {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if(intent!=null) {
                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (info != null) {
                    context.startActivity(intent);
                } else {
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    //url = fallbackUrl;
                    //return false;

                    // or call external broswer
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                    context.startActivity(browserIntent);
                }
                return true;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openImprovedArticle() {
        // TODO: 5/29/2017
    }

    public static void startInAppBrowser(Activity activity, Submission post, String url, String domain) {
        if(MyApplication.useCCT) {
           startChromeCustomTabs(activity, post, url, domain);
        }
        else {
            startBrowserActivity(activity, post, url, domain);
        }
    }

    public static void startChromeCustomTabs(Activity activity, Submission post, String url, String domain) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(MyApplication.currentColor);
        if(MyApplication.disableAnimations) {
            builder.setStartAnimations(activity, -1, -1);
            builder.setExitAnimations(activity, -1, -1);
        }
        else {
            builder.setStartAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right);
            builder.setExitAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right);
        }

        CustomTabsIntent customTabsIntent = builder.build();

        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    public static void startBrowserActivity(Activity activity, Submission post, String url, String domain) {
        Intent intent = new Intent(activity, BrowserActivity.class);
        if (post != null) {
            intent.putExtra("post", post);
        }
        else {
            intent.putExtra("url", url);
            intent.putExtra("domain", domain);
        }
        activity.startActivity(intent);
    }

    private Intent getMediaActivityIntent(Activity activity, String url, String domain) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("domain", domain);

        return intent;
    }

    private Intent getUserSubredditIntent(Activity activity, String url) {
        Intent intent = null;

        final String pattern = "^(?:https?\\:\\/\\/)?(?:www\\.)?(?:reddit\\.com)?\\/(r|u|user)\\/(\\w+)";

        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            if(matcher.group(1).equals("r")) {
                intent = new Intent(activity, SubredditActivity.class);
                intent.putExtra("subreddit", matcher.group(2));
            }
            else {
                intent = new Intent(activity, UserActivity.class);
                intent.putExtra("username", matcher.group(2));
            }
        }

        return intent;
    }

    // Get an intent for links like '/r/movies' or '/u/someuser' or /r/games/about/sidebar
    private Intent getNoDomainIntent(Activity activity, String url) {
        Intent intent = null;

        final String pattern = "/(\\w)/(\\w+)/?(.*)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if(matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            String more = matcher.group(3);
            if(more.length()>0) {
                this.url = "http://reddit.com" + url;
                this.domain = "reddit.com";
                if(MyApplication.handleOtherLinks) {
                    startInAppBrowser(activity, post, this.url, domain);
                }
                else {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.url));
                }
            }
            else if(type.equalsIgnoreCase("r")) {
                intent = new Intent(activity, SubredditActivity.class);
                intent.putExtra("subreddit", name.toLowerCase());
            }
            else if(type.equalsIgnoreCase("u") || type.equalsIgnoreCase("user")) {
                intent = new Intent(activity, UserActivity.class);
                intent.putExtra("username", name.toLowerCase());
            }
        }
        else {
            ToastUtils.showToast(activity, "Url not supported");
        }

        return intent;
    }

    public boolean isBrowserActive() {
        return browserActive;
    }

    public void setBrowserActive(boolean browserActive) {
        this.browserActive = browserActive;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }

}
