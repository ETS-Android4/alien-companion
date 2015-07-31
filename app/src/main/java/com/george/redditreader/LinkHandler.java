package com.george.redditreader;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.george.redditreader.Activities.BrowserActivity;
import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.api.entity.Submission;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

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

    //public LinkHandler(Activity activity) {
    //    this.activity = activity;
    //}

    public void handleIt() {
        String url = post.getURL();
        String domain = post.getDomain();
        Intent intent;

        Log.d("Link Domain", domain);
        Log.d("Link Full URL", url);

        if(domain.equals("youtube.com") || domain.equals("youtu.be") || domain.equals("m.youtube.com")) {
            String videoId = getYoutubeVideoId(url);
            int time = getYoutubeVideoTime(url);
            //Log.d("youtube video id", videoId);
            intent = YouTubeStandalonePlayer.createVideoIntent(activity, YOUTUBE_API_KEY, videoId, time, true, true);
        }
        else if(domain.equals("reddit.com") || domain.substring(3).equals("reddit.com")) {
            intent = new Intent(activity, PostActivity.class);
            intent.putExtra("postInfo", getRedditPostInfo(url));
        }
        else if(domain.equals("redd.it")) {
            intent = new Intent(activity, PostActivity.class);
            intent.putExtra("postId", url.substring(15));
        }
        else {
            intent = new Intent(activity, BrowserActivity.class);
            intent.putExtra("post", post);
        }
        activity.startActivity(intent);
    }

    public String[] getRedditPostInfo(String url) {

        String[] postInfo = new String[4];

        String pattern = "/r/(.*)/comments/(\\w+)/\\w+/(\\w+)(?:.*context=(\\d+))?";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if(matcher.find()) {
            postInfo[0] = matcher.group(1);
            postInfo[1] = matcher.group(2);
            postInfo[2] = matcher.group(3);
            postInfo[3] = matcher.group(4);
        }

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
