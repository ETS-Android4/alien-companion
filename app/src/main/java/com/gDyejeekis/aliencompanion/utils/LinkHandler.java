package com.gDyejeekis.aliencompanion.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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

import xyz.klinker.android.article.ArticleIntent;

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
        this.url = url;
        try {
            this.domain = ConvertUtils.getDomainName(url);
        } catch (URISyntaxException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    //returns false if the url is to be handled by the webview, true for custom handling
    public boolean handleIt() {
        boolean setImplicitIntent = false;
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
                            String playlistId = getYoutubePlaylistId(url);
                            if(playlistId.equals("")) {
                                Log.e(TAG, "Unable to validate YouTube playlist ID");
                                setImplicitIntent = true;
                            }
                            else {
                                Log.d(TAG, "YouTube playlist ID: " + playlistId);
                                intent = YouTubeStandalonePlayer.createPlaylistIntent(activity, YOUTUBE_API_KEY, playlistId, 0, 0, true, true);
                            }

                        }
                        else {
                            String videoId = getYoutubeVideoId(url);
                            if(videoId.equals("")) {
                                Log.e(TAG, "Unable to validate YouTube video ID");
                                setImplicitIntent = true;
                            }
                            else {
                                Log.d(TAG, "YouTube video ID: " + videoId);
                                int time = getYoutubeVideoTime(url);
                                intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
                            }
                        }
                    }
                    else setImplicitIntent = true;
                }
                else if(domainLC.contains("imgur.com")) {
                    if(MyApplication.handleImgur) {
                        //startInAppBrowser(activity, post, url, domain);
                        intent = getMediaActivityIntent(activity, url, domain);
                    }
                    else setImplicitIntent = true;
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
                    else setImplicitIntent = true;
                }
                else if (domainLC.matches("^(\\w+\\.)?reddit\\.com") /*domainLC.equals("reddit.com") || domainLC.substring(2).equals("reddit.com") || domainLC.substring(3).equals("reddit.com")*/) {
                    //case (subreddit).reddit.com link
                    if(domainLC.matches("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com")) {
                        Matcher matcher = Pattern.compile("^(?!\\bwww\\b|\\bnp\\b|\\bm\\b)(\\w+)\\.reddit\\.com").matcher(domainLC);
                        if(matcher.find()) {
                            intent = new Intent(activity, SubredditActivity.class);
                            intent.putExtra("subreddit", matcher.group(1));
                        }
                    }
                    //case user/subreddit link
                    else if(urlLC.matches("^(?:https?\\:\\/\\/)?(?:www\\.)?(?:reddit\\.com)?\\/(r|u|user)\\/(\\w+)")) {
                        intent = getUserSubredditIntent(activity, urlLC);
                    }
                    //case other reddit link not handled by the app
                    else if(urlLC.contains("/wiki/") || urlLC.contains("/about/") || urlLC.contains("/live/")) {
                        if (MyApplication.handleOtherLinks) {
                            if(!browserActive) {
                                startInAppBrowser(activity, post, url, domain);
                            }
                        }
                        else setImplicitIntent = true;
                    }
                    //case post link
                    else { // prepare explicit intent for reddit link
                        String postInfo[] = getRedditPostInfo(url);
                        if (postInfo != null) { //case url of reddit post
                            intent = new Intent(activity, PostActivity.class);
                            intent.putExtra("postInfo", postInfo);
                        }
                        // TODO: 8/1/2016 this might be unnecessary
                        else { //case url of subreddit/user
                            Pattern pattern = Pattern.compile("/(r|u|user)/[\\w\\.]+", Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(url);
                            if (matcher.find())
                                intent = getNoDomainIntent(activity, matcher.group());
                        }
                    }
                }
                else if (domainLC.equals("redd.it")) {
                    intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("postId", getShortRedditId(url));
                }
                else if (MyApplication.handleOtherLinks && !domainLC.equals("play.google.com") && !urlLC.endsWith(".pdf")) {
                    if(!browserActive) {
                        if( post != null &&
                                ( post.hasSyncedArticle || (MyApplication.handleArticles && GeneralUtils.isArticleLink(url, domain)) ) ) {
                            openImprovedArticle();
                            return true;
                        }
                        else {
                            startInAppBrowser(activity, post, url, domain);
                        }
                    }
                }
                else {
                    setImplicitIntent = true;
                }
            }

            if(setImplicitIntent) {
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

    private void openImprovedArticle() {
        ArticleIntent intent = new ArticleIntentBuilder(context, ARTICLE_API_KEY)
                .setToolbarColor(MyApplication.currentColor)
                //.setAccentColor(accentColor)
                .setTheme(getArticleViewTheme())
                .setTextSize(getArticleViewTextSize())     // 15 SP (default)
                .build();

        intent.launchUrl(context, Uri.parse(url));
    }

    private int getArticleViewTheme() {
        return (MyApplication.nightThemeEnabled) ? ArticleIntent.THEME_DARK : ArticleIntent.THEME_LIGHT;
    }

    private int getArticleViewTextSize() {
        int textSize;
        switch (MyApplication.fontStyle) {
            case R.style.FontStyle_Smallest:
                textSize = 13;
                break;
            case R.style.FontStyle_Smaller:
                textSize = 14;
                break;
            case R.style.FontStyle_Small:
                textSize = 15;
                break;
            case R.style.FontStyle_Medium:
                textSize = 16;
                break;
            case R.style.FontStyle_Large:
                textSize = 17;
                break;
            case R.style.FontStyle_Larger:
                textSize = 18;
                break;
            case R.style.FontStyle_Largest:
                textSize = 19;
                break;
            default:
                textSize = 16;
                break;
        }
        return textSize;
    }

    public static void startInAppBrowser(Activity activity, Submission post, String url, String domain) {
        if(MyApplication.useCCT) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(MyApplication.currentColor);
            if(MyApplication.disableAnimations) {
                builder.setStartAnimations(activity, -1, -1);
                builder.setExitAnimations(activity, -1, -1);
            }
            else {
                builder.setStartAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                builder.setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            CustomTabsIntent customTabsIntent = builder.build();

            customTabsIntent.launchUrl(activity, Uri.parse(url));
        }
        else {
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
    }

    private Intent getMediaActivityIntent(Activity activity, String url, String domain) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("domain", domain);

        return intent;
    }

    private Intent getUserSubredditIntent(Activity activity, String url) {
        Intent intent = null;

        String pattern = "^(?:https?\\:\\/\\/)?(?:www\\.)?(?:reddit\\.com)?\\/(r|u|user)\\/(\\w+)";

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

    //Get an intent for links like '/r/movies' or '/u/someuser' or /r/games/about/sidebar
    private Intent getNoDomainIntent(Activity activity, String url) {
        Intent intent = null;

        String pattern = "/(\\w)/(\\w+)/?(.*)";
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

    //Get an intent for links like '/r/movies' or '/u/someuser' (old method)
    private Intent getNoDomainIntentOld(Activity activity, String link) {
        Intent intent = null;
        link = link.toLowerCase();
        if(link.charAt(1) == 'r') {
            intent = new Intent(activity, SubredditActivity.class);
            intent.putExtra("subreddit", link.substring(3));
        }
        else if(link.charAt(1) == 'u') {
            intent = new Intent(activity, UserActivity.class);
            String username;
            if(link.charAt(2) == 's') username = link.substring(6);
            else username = link.substring(3);
            intent.putExtra("username", username);
        }
        return intent;
    }

    public static String[] getRedditPostInfo(String url) {

        String[] postInfo = new String[4];

        String pattern = "/r/(.*)/(?:comments|duplicates)/(\\w+)/?(?:\\w+)?/?(\\w+)?(?:.*context=(\\d+))?";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);
        if(matcher.find()) {
            postInfo[0] = matcher.group(1);
            postInfo[1] = matcher.group(2);
            postInfo[2] = matcher.group(3);
            postInfo[3] = matcher.group(4);
        }
        else return null;

        //for(String info : postInfo) {
        //    Log.e("reddit post info", info);
        //}

        return postInfo;
    }

    public static String getShortRedditId(String url) {
        String pattern = "redd\\.it/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGfycatId(String url) {
        String pattern = "gfycat\\.com/(\\w+)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGyazoId(String url) {
        String pattern = "gyazo\\.com/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGiphyId(String url) {
        String pattern = "giphy\\.com\\/(?:(?:media|gifs)\\/)?(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getStreamableId(String url) {
        String pattern = "streamable\\.com\\/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static boolean isRawGyazoUrl(String url) {
        return url.matches(".*(i|embed|bot)\\.gyazo\\.com\\/\\w+\\.(jpg|jpeg|png|gif|mp4)");
    }

    public static boolean isMp4Giphy(String url) {
        return url.matches(".*giphy\\.com\\/media\\/\\w+\\/giphy\\.mp4");
    }

    public static String getImgurImgId(String url) {
        String pattern = "imgur\\.com(?:\\/(?:a|gallery))?(?:\\/(?:topic|r)\\/\\w+)?\\/(\\w+)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String getYoutubePlaylistId(String youtubeURL) {
        String pattern = "^.*(youtu.be\\/|list=)([^#\\&\\?]*).*";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(decodeURL(youtubeURL));

        if(matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    public static String getYoutubeVideoId(String youtubeURL) {
        String pattern = "(youtu(?:\\.be|be\\.com)\\/(?:.*v(?:\\/|=)|(?:.*\\/)?)([\\w'-]+))";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher((decodeURL(youtubeURL)));

        if(matcher.find()){
            return matcher.group(2);
        }
        return "";
    }

    public static String getReddituploadsFilename(String url) {
        return url.substring(0, url.indexOf("?")).replaceAll("https?://", "").replace("/", "(s)").concat(".jpg");
    }

    private static String decodeURL(String url) {
        try {
            String decodedurl = URLDecoder.decode(url, "UTF-8");
            //Log.d("url decoder", decodedurl);
            return decodedurl;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    private int getYoutubeVideoTime(String youtubeURL) {
        int timeMillis = 0;

        try {
            String pattern = "(?<=t=)[^#\\&\\?\\n]*";

            Pattern timePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = timePattern.matcher(youtubeURL);

            if (matcher.find()) {
                String time = matcher.group();
                if (time.contains("h") || time.contains("m") || time.contains("s")) {
                    String secondsPattern = "(?<=m?)(\\d{1,6})(?=s)";
                    String minutesPattern = "(?<=h?)(\\d{1,6})(?=m)";
                    String hoursPattern = "(\\d{1,6})(?=h)";

                    Pattern compiledSeconds = Pattern.compile(secondsPattern);
                    Pattern compiledMinutes = Pattern.compile(minutesPattern);
                    Pattern compiledHours = Pattern.compile(hoursPattern);
                    matcher = compiledSeconds.matcher(time);
                    if (matcher.find())
                        timeMillis += Integer.parseInt(matcher.group()) * 1000;
                    matcher = compiledMinutes.matcher(time);
                    if (matcher.find())
                        timeMillis += Integer.parseInt(matcher.group()) * 60 * 1000;
                    matcher = compiledHours.matcher(time);
                    if (matcher.find())
                        timeMillis += Integer.parseInt(matcher.group()) * 60 * 60 * 1000;
                } else {
                    timeMillis = Integer.parseInt(matcher.group()) * 1000;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return timeMillis;
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
