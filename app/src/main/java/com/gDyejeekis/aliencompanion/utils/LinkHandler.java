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

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 6/11/2015.
 */
public class LinkHandler {

    public static final String TAG = "LinkHandler";

    private static final String YOUTUBE_API_KEY = "AIzaSyDAqkwJF2o2QmGsoyj-yPP8uCqMxytm15Y";

    public static final String GIPHY_API_KEY = "dc6zaTOxFJmzC";

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

    // return false if the url is to be handled by the open webview, true for any other custom handling
    public boolean handleIt() {
        boolean setImplicitViewIntent = false;
        try {
            Log.d(TAG, "FULL URL: " + url);
            Log.d(TAG, "DOMAIN: " + domain);
            Intent intent = null;
            if (domain == null) {
                intent = LinkUtils.getUserSubredditIntent(context, url);
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
                                intent = YouTubeStandalonePlayer.createPlaylistIntent((Activity) context, YOUTUBE_API_KEY, playlistId, 0, 0, true, true);
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
                                intent = YouTubeStandalonePlayer.createVideoIntent((Activity) context, YOUTUBE_API_KEY, videoId, time, true, true);
                            }
                        }
                    }
                    else setImplicitViewIntent = true;
                }
                else if(domainLC.contains("imgur.com")) {
                    if(MyApplication.handleImgur) {
                        //startInAppBrowser(activity, post, url, domain);
                        intent = getMediaActivityIntent();
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
                    else if(post.getRedditVideo()!=null) { // reddit video object found in post object so pass that as extra to media activity
                        intent = new Intent(context, MediaActivity.class);
                        intent.putExtra("redditVideo", post.getRedditVideo());
                        intent.putExtra("url", url);
                        intent.putExtra("domain", domainLC);
                    }
                    else { // post isn't null but redditVideo object is null, manually edit reddit video url and pass to media activity
                        intent = new Intent(context, MediaActivity.class);
                        if(!urlLC.endsWith("/DASH_2_4_M")) {
                            url += "/DASH_2_4_M";
                        }
                        intent.putExtra("url", url);
                        intent.putExtra("domain", domainLC);
                    }
                }
                else if(urlLC.endsWith(".png") || urlLC.endsWith(".jpg") || urlLC.endsWith(".jpeg") || domainLC.equals("i.reddituploads.com") || domainLC.equals("i.redditmedia.com")
                        || domainLC.contains("gyazo.com")) {
                    intent = getMediaActivityIntent();
                }
                else if(domainLC.contains("gfycat.com") || domainLC.contains("giphy.com") || urlLC.endsWith(".gif") || urlLC.endsWith(".gifv")/* || urlLC.endsWith(".webm") || urlLC.endsWith(".mp4")*/) {
                    intent = getMediaActivityIntent();
                }
                else if(domainLC.contains("streamable.com") || urlLC.endsWith(".mp4")) {
                    intent = getMediaActivityIntent();
                }
                else if(domainLC.equals("twitter.com")) {
                    if(MyApplication.handleTwitter) {
                        return startInAppBrowser();
                    }
                    else setImplicitViewIntent = true;
                }
                else if (domainLC.matches("^(\\w+\\.)?reddit\\.com") || domainLC.equals("redd.it")) {
                    // case post link
                    if(LinkUtils.isRedditPostUrl(urlLC)) {
                        intent = new Intent(context, PostActivity.class);
                        intent.putExtra("url", url);
                    }
                    // case (subreddit).reddit.com link
                    else if (domainLC.matches("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com")) {
                        Matcher matcher = Pattern.compile("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com").matcher(domainLC);
                        if(matcher.find()) {
                            intent = new Intent(context, SubredditActivity.class);
                            intent.putExtra("subreddit", matcher.group(1));
                        }
                    }
                    // case user/subreddit link
                    else if (LinkUtils.isUserSubredditUrl(urlLC)) {
                        intent = LinkUtils.getUserSubredditIntent(context, urlLC);
                    }
                    // case other reddit link not handled by the app natively
                    else {
                        if (MyApplication.handleOtherLinks) {
                            return startInAppBrowser();
                        }
                        else setImplicitViewIntent = true;
                    }
                }
                // if in offline mode start browser activity to look for synced artocle
                else if(MyApplication.offlineModeEnabled && LinkUtils.isArticleLink(urlLC, domainLC)) { // TODO: 7/30/2017 maybe check if synced article exists here instead of checking the link
                    return startBrowserActivity();
                }
                else if(LinkUtils.isIntentLink(url)) {
                    return handleAppLink();
                }
                else if(LinkUtils.isEmailAddress(url)) {
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    context.startActivity(intent);
                    return true;
                }
                else if (MyApplication.handleOtherLinks && !domainLC.equals("play.google.com") && !urlLC.endsWith(".pdf")) {
                    return startInAppBrowser();
                }
                else {
                    setImplicitViewIntent = true;
                }
            }

            if(setImplicitViewIntent) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            if(intent!=null) {
                context.startActivity(intent);
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
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(context, "Unknown uri intent scheme");
        }
        return false;
    }

    private boolean startInAppBrowser() {
        return (MyApplication.useCCT) ? startChromeCustomTabs() : startBrowserActivity();
    }

    private boolean startChromeCustomTabs() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(MyApplication.currentPrimaryColor);
        if(MyApplication.disableAnimations) {
            builder.setStartAnimations(context, -1, -1);
            builder.setExitAnimations(context, -1, -1);
        }
        else {
            builder.setStartAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right);
            builder.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right);
        }

        CustomTabsIntent customTabsIntent = builder.build();

        customTabsIntent.launchUrl(context, Uri.parse(url));
        return true;
    }

    private boolean startBrowserActivity() {
        if (browserActive) return false;
        browserActive = true;
        Intent intent = new Intent(context, BrowserActivity.class);
        if (post != null) {
            intent.putExtra("post", post);
        } else {
            intent.putExtra("url", url);
            intent.putExtra("domain", domain);
        }
        context.startActivity(intent);
        return true;
    }

    private Intent getMediaActivityIntent() {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("domain", domain);
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
