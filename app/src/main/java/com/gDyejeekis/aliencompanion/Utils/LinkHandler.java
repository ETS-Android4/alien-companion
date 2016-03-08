package com.gDyejeekis.aliencompanion.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.Activities.PostActivity;
import com.gDyejeekis.aliencompanion.Activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
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

    private static final String YOUTUBE_API_KEY = "AIzaSyDAqkwJF2o2QmGsoyj-yPP8uCqMxytm15Y"; //TODO: get different api key before release

    private Context context;
    private Submission post;
    private String url;
    private String domain;

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

    public void handleIt() {
        boolean setImplicitIntent = false;
        try {
            Intent intent = null;
            Activity activity = (Activity) context;

            Log.d("Link Full URL", url);
            if (domain == null) {
                intent = getNoDomainIntent(activity, url);
            } else {
                //Log.d("Link Domain", domain);
                if (domain.contains("youtube.com") || domain.equals("youtu.be") /*(domain.equals("youtube.com") || domain.equals("youtu.be") || domain.equals("m.youtube.com"))*/) {
                    if (MyApplication.handleYouTube) {
                        String videoId = getYoutubeVideoId(url);
                        int time = getYoutubeVideoTime(url);
                        //Log.d("youtube video id", videoId);
                        intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
                    }
                    else setImplicitIntent = true;
                }
                else if(domain.equals("imgur.com") || domain.equals("gfycat.com") || url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gifv") || url.endsWith(".gifv")) {
                    if(MyApplication.handleImgur) {
                        startInAppBrowser(activity, post, url, domain);
                        //intent = getImageActivityIntent(activity, url, domain);
                    }
                    else setImplicitIntent = true;
                }
                else if(domain.equals("twitter.com")) {
                    if(MyApplication.handleTwitter) {
                        startInAppBrowser(activity, post, url, domain);
                    }
                    else setImplicitIntent = true;
                }
                else if (domain.equals("reddit.com") || domain.substring(3).equals("reddit.com")) {
                    if(url.contains("/wiki/") || url.contains("/about/")) {
                        if (MyApplication.handleOtherLinks) startInAppBrowser(activity, post, url, domain);
                        else setImplicitIntent = true;
                    }
                    else { // prepare explicit intent for reddit link
                        String postInfo[] = getRedditPostInfo(url);
                        if (postInfo != null) { //case url of reddit post
                            intent = new Intent(activity, PostActivity.class);
                            intent.putExtra("postInfo", postInfo);
                        } else { //case url of subreddit/user
                            Pattern pattern = Pattern.compile("/(r|u|user)/[\\w\\.]+", Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(url);
                            if (matcher.find())
                                intent = getNoDomainIntent(activity, matcher.group());
                        }
                    }
                } else if (domain.equals("redd.it")) {
                    intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("postId", url.substring(15));
                } else if (MyApplication.handleOtherLinks && !domain.equals("play.google.com")) {
                    startInAppBrowser(activity, post, url, domain);
                }
                else {
                    setImplicitIntent = true;
                }
            }

            if(setImplicitIntent) intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if(intent!=null) activity.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            ToastUtils.displayShortToast(context, "No activity found to handle Intent");
            e.printStackTrace();
        }
    }

    public static void startInAppBrowser(Activity activity, Submission post, String url, String domain) {
        if(MyApplication.useCCT) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(MyApplication.currentColor);
            builder.setStartAnimations(activity, -1, -1);
            builder.setExitAnimations(activity, -1, -1);

            CustomTabsIntent customTabsIntent = builder.build();

            customTabsIntent.launchUrl(activity, Uri.parse(url));
        }
        else {
            Intent intent = new Intent(activity, BrowserActivity.class);
            if (post != null) {
                intent.putExtra("post", post);
            } else {
                intent.putExtra("url", url);
                intent.putExtra("domain", domain);
            }
            activity.startActivity(intent);
        }
    }

    private Intent getImageActivityIntent(Activity activity, String url, String domain) {
        Intent intent = new Intent(activity, ImageActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("domain", domain);

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
            ToastUtils.displayShortToast(activity, "Url not supported");
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
        Pattern compiledPattern = Pattern.compile(pattern);
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

    public static String getImgurImgId(String url) {
        String pattern = "imgur\\.com\\/(\\w+)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()) {
            return matcher.group(1);
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

    public static String decodeURL(String url) {
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

}
