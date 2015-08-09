package com.george.redditreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.george.redditreader.Activities.BrowserActivity;
import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.Activities.SubredditActivity;
import com.george.redditreader.Activities.UserActivity;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Submission;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.net.URISyntaxException;
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
            domain = ConvertUtils.getDomainName(url);
        } catch (URISyntaxException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void handleIt() {
        Intent intent = null;
        Activity activity = (Activity) context;

        //Log.d("Link Full URL", url);
        if(domain == null) {
            intent = getNoDomainIntent(activity, url);
        }
        else {
            //Log.d("Link Domain", domain);
            if (domain.equals("youtube.com") || domain.equals("youtu.be") || domain.equals("m.youtube.com")) {
                String videoId = getYoutubeVideoId(url);
                int time = getYoutubeVideoTime(url);
                //Log.d("youtube video id", videoId);
                intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
            } else if (domain.equals("reddit.com") || domain.substring(3).equals("reddit.com")) {
                String postInfo[] = getRedditPostInfo(url);
                if(postInfo != null) { //case url of reddit post
                    intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("postInfo", postInfo);
                }
                else { //case url of subreddit/user
                    Pattern pattern = Pattern.compile("/(r|u|user)/[\\w\\.]+", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(url);
                    if(matcher.find()) intent = getNoDomainIntent(activity, matcher.group());
                }
            } else if (domain.equals("redd.it")) {
                intent = new Intent(activity, PostActivity.class);
                intent.putExtra("postId", url.substring(15));
            } else {
                intent = new Intent(activity, BrowserActivity.class);
                if (post != null) {
                    intent.putExtra("post", post);
                } else {
                    intent.putExtra("url", url);
                    intent.putExtra("domain", domain);
                }
            }
        }

        if(intent != null) activity.startActivity(intent);
        else ToastUtils.displayShortToast(context, "Could not resolve hyperlink");
    }

    //Get an intent for links like '/r/movies' or '/u/someuser' //TODO: maybe use regex here
    private Intent getNoDomainIntent(Activity activity, String link) {
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

    public String[] getRedditPostInfo(String url) {

        String[] postInfo = new String[4];

        String pattern = "/r/(.*)/comments/(\\w+)/\\w+(/\\w+)?(?:.*context=(\\d+))?";
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

    private String getYoutubeVideoId(String youtubeURL) {
        String pattern = "(?<=v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youtubeURL);

        if(matcher.find()){
            return matcher.group();
        }
        return "";

    }

    private int getYoutubeVideoTime(String youtubeURL) {
        int timeMillis = 0;

        String pattern = "(?<=t=)[^#\\&\\?\\n]*";

        Pattern timePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(youtubeURL);

        if(matcher.find()){
            String time = matcher.group();
            if(time.contains("h") || time.contains("m") || time.contains("s")) {
                String secondsPattern = "(?<=m?)(\\d{1,6})(?=s)";
                String minutesPattern = "(?<=h?)(\\d{1,6})(?=m)";
                String hoursPattern = "(\\d{1,6})(?=h)";

                Pattern compiledSeconds = Pattern.compile(secondsPattern);
                Pattern compiledMinutes = Pattern.compile(minutesPattern);
                Pattern compiledHours = Pattern.compile(hoursPattern);
                matcher = compiledSeconds.matcher(time);
                if(matcher.find())
                    timeMillis += Integer.parseInt(matcher.group()) * 1000;
                matcher = compiledMinutes.matcher(time);
                if(matcher.find())
                    timeMillis += Integer.parseInt(matcher.group()) * 60 * 1000;
                matcher = compiledHours.matcher(time);
                if(matcher.find())
                    timeMillis += Integer.parseInt(matcher.group()) * 60 * 60 * 1000;
            }
            else {
                timeMillis = Integer.parseInt(matcher.group()) * 1000;
            }
        }
        return timeMillis;
    }

}
