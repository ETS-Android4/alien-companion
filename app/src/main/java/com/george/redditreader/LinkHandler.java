package com.george.redditreader;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.george.redditreader.Activities.BrowserActivity;
import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.utils.RedditConstants;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 6/11/2015.
 */
public class LinkHandler {

    private static final String YOUTUBE_API_KEY = "AIzaSyDAqkwJF2o2QmGsoyj-yPP8uCqMxytm15Y"; //TODO: get different api key before release
    private Activity activity;
    private Submission post;

    public LinkHandler(Activity activity, Submission post) {
        this.activity = activity;
        this.post = post;
    }

    public void handleIt() {
        String url = post.getURL();
        String domain = post.getDomain();
        Intent intent;

        Log.d("Link Domain", domain);
        Log.d("Link Full URL", url);

        if(domain.equals("youtube.com") || domain.equals("youtu.be")) {
            String videoId = getYoutubeVideoId(url);
            int time = getYoutubeVideoTime(url);
            //Log.d("youtube video id", videoId);
            intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
        }
        else if(domain.equals("reddit.com") || domain.equals("redd.it") || domain.substring(3).equals("reddit.com")) { //TODO: use REGEX to retrieve link parameters
            intent = new Intent(activity, PostActivity.class);
            intent.putExtra("postUrl", getRedditPostUrl(url, domain));
        }
        else {
            intent = new Intent(activity, BrowserActivity.class);
            intent.putExtra("post", post);
        }
        activity.startActivity(intent);
    }

    public String getRedditPostUrl(String url, String domain) {
        String endpoint = ".json?depth=" + RedditConstants.MAX_COMMENT_DEPTH +
                "&limit=" + RedditConstants.MAX_LIMIT_COMMENTS;

        String postUrl = null;
        if(domain.equals("redd.it")) postUrl = "/comments/" + url.substring(15) + endpoint;
        else {
            String pattern = "context=\\w+";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(url);
            if (matcher.find()) endpoint = endpoint + "&" + matcher.group();

            pattern = "/r/[\\w/]*";
            compiledPattern = Pattern.compile(pattern);
            matcher = compiledPattern.matcher(url);
            if(matcher.find()) postUrl = matcher.group() + endpoint;
        }

        return postUrl;
    }

    private String getYoutubeVideoId(String youtubeURL) {
        String pattern = "(?<=v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
//
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youtubeURL);
//
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
//
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
