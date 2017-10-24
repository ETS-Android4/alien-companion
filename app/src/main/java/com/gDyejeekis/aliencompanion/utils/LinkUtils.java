package com.gDyejeekis.aliencompanion.utils;

import com.gDyejeekis.aliencompanion.api.entity.Submission;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 10/24/2017.
 */

public class LinkUtils {

    public static final String TAG = "LinkUtils";

    public static String getDomainName(String url) throws URISyntaxException {
        try {
            URI uri = new URI(url.replace("_", ""));
            String domain = uri.getHost(); // underscore character will cause getHost() to return null
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String URLEncodeString(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    private static String URLDecodeString(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String urlToFilename(String url) {
        String filename = removeUrlParameters(url);
        filename = filename.substring(filename.lastIndexOf("/") + 1);
        return filename;
    }

    public static String urlToFilenameOld(String url) {
        String filename = removeUrlParameters(url);
        filename = filename.replaceAll("https?://", "").replace("/", "(s)");
        return filename;
    }

    public static String removeUrlParameters(String url) {
        try {
            url = url.substring(0, url.lastIndexOf("?"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getShortRedditId(String url) {
        final String pattern = "redd\\.it/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGfycatId(String url) {
        final String pattern = "gfycat\\.com\\/(?:gifs\\/detail\\/)?(\\w+)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGyazoId(String url) {
        final String pattern = "gyazo\\.com/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getGiphyId(String url) {
        final String pattern = "giphy\\.com\\/(?:(?:media|gifs)\\/)?(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getStreamableId(String url) {
        final String pattern = "streamable\\.com\\/(\\w+)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static boolean isRawGyazoUrl(String url) {
        final String pattern = ".*(i|embed|bot)\\.gyazo\\.com\\/\\w+\\.(jpg|jpeg|png|gif|mp4)";
        return url.matches(pattern);
    }

    public static boolean isMp4Giphy(String url) {
        final String pattern = ".*giphy\\.com\\/media\\/\\w+\\/giphy\\.mp4";
        return url.matches(pattern);
    }

    public static String getImgurImgId(String url) {
        final String pattern = "imgur\\.com(?:\\/(?:a|gallery))?(?:\\/(?:topic|r)\\/\\w+)?\\/(\\w+)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String getYoutubePlaylistId(String youtubeURL) {
        final String pattern = "^.*(youtu.be\\/|list=)([^#\\&\\?]*).*";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(URLDecodeString(youtubeURL));

        if(matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    public static String getYoutubeVideoId(String youtubeURL) {
        final String pattern = "(youtu(?:\\.be|be\\.com)\\/(?:.*v(?:\\/|=)|(?:.*\\/)?)([\\w'-]+))";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher((URLDecodeString(youtubeURL)));

        if(matcher.find()){
            return matcher.group(2);
        }
        return "";
    }

    public static int getYoutubeVideoTime(String youtubeURL) {
        int timeMillis = 0;

        try {
            final String pattern = "(?<=t=)[^#\\&\\?\\n]*";

            Pattern timePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = timePattern.matcher(youtubeURL);

            if (matcher.find()) {
                String time = matcher.group();
                if (time.contains("h") || time.contains("m") || time.contains("s")) {
                    final String secondsPattern = "(?<=m?)(\\d{1,6})(?=s)";
                    final String minutesPattern = "(?<=h?)(\\d{1,6})(?=m)";
                    final String hoursPattern = "(\\d{1,6})(?=h)";

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

    // TODO: 9/16/2017 maybe write this properly (as long as input is a link reddit probably already validates email adresses)
    public static boolean isEmailAddress(String link) {
        return link.contains("@");
    }

    public static boolean isIntentLink(String url) {
        return url.toLowerCase().startsWith("intent://");
    }

    public static boolean isRedditPostUrl(String url) {
        return url.matches(".*reddit\\.com\\/r\\/\\w+\\/comments\\/\\w+.*") || url.matches("(https?:\\/\\/)?redd\\.it\\/\\w+.*");
    }

    public static Submission getRedditPostFromUrl(String url) {
        Submission post = null;
        url = url.toLowerCase();
        if(url.contains("v.redd.it")) {

        }
        else if(url.contains("redd.it")) {
            String id = LinkUtils.getShortRedditId(url);
            if(!id.isEmpty()) {
                post = new Submission(id);
            }
        }
        else {
            final String pattern = "/r/(.*)/(?:comments|duplicates)/(\\w+)/?(?:\\w+)?/?(\\w+)?(?:.*context=(\\d+))?";
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = compiledPattern.matcher(url);
            if(matcher.find()) {
                String subreddit = matcher.group(1);
                String postId = matcher.group(2);
                String linkedCommentId = matcher.group(3);
                String context = matcher.group(4);
                int parentsShown = context!=null ? Integer.valueOf(context) : -1;

                post = new Submission(postId);
                post.setSubreddit(subreddit);
                post.setLinkedCommentId(linkedCommentId);
                post.setParentsShown(parentsShown);
            }
        }
        return post;
    }

    public static boolean isImageLink(String url, String domain) {
        if(domain.contains("imgur.com")) return true;
        if(domain.contains("gfycat.com")) return true;
        if(domain.contains("giphy.com")) return true;
        if(domain.contains("gyazo.com")) return true;
        if(domain.contains("flickr.com")) return true;
        if(domain.contains("twimg.com")) return true;
        if(domain.contains("photobucket.com")) return true;
        if(domain.contains("deviantart.com")) return true;
        if(domain.equals("instagram.com")) return true;
        if(domain.equals("snapchat.com")) return true;
        if(domain.equals("trbimg.com")) return true;
        if(domain.equals("imgfly.net")) return true;
        if(domain.equals("9gag.com")) return true;
        if(domain.equals("i.redd.it")) return true;
        if(domain.equals("i.reddituploads.com")) return true;
        if(domain.equals("i.redditmedia.com")) return true;
        if(domain.equals("v.redd.it")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".jpg") || urlLc.endsWith(".png") || urlLc.endsWith(".gif") || urlLc.endsWith(".jpeg")) return true;
        return false;
    }

    public static boolean isArticleLink(String url, String domain) {
        if(isImageLink(url, domain)) return false;
        if(isVideoLink(url, domain)) return false;
        if(domain.contains("reddit.com") || domain.equals("redd.it")) return false;
        if(domain.equals("twitter.com")) return false;
        if(domain.contains("facebook")) return false;
        if(domain.contains("github.com")) return false;
        if(domain.equals("bitbucket.org")) return false;
        if(domain.equals("gitlab.com")) return false;
        if(domain.equals("store.steampowered.com")) return false;
        if(domain.equals("steamcommunity.com")) return false;
        if(domain.equals("origin.com")) return false;
        if(domain.equals("ubisoft.com")) return false;
        if(domain.equals("humblebundle.com")) return false;
        if(domain.equals("strawpoll.me")) return false;
        if(domain.equals("docs.google.com")) return false;
        if(domain.contains("mixtape.moe")) return false;
        if(domain.equals("play.google.com")) return false;
        if(url.endsWith(".pdf")) return false;
        return true;
    }

    public static boolean isGifLink(String url, String domain) {
        if(domain.contains("gfycat.com")) return true;
        if(domain.contains("giphy.com")) return true;
        String urlLc = url.toLowerCase();
        if(domain.contains("imgur.com") && urlLc.endsWith(".mp4")) return true;
        if(urlLc.endsWith(".gif") || urlLc.endsWith(".gifv")) return true;
        return false;
    }

    public static boolean isVideoLink(String url, String domain) {
        if(domain.contains("youtube") || domain.equals("youtu.be")) return true;
        if(domain.contains("streamable.com")) return true;
        if(domain.contains("pomf.se")) return true;
        if(domain.contains("dailymotion")) return true;
        if(domain.contains("vimeo.com")) return true;
        if(domain.equals("vid.me")) return true;
        if(domain.equals("vine.co")) return true;
        if(domain.equals("liveleak.com")) return true;
        if(domain.contains("twitch.tv")) return true;
        if(domain.equals("hitbox.tv")) return true;
        if(domain.equals("oddshot.tv")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".webm") || urlLc.endsWith(".mp4"))  return true;
        return false;
    }

}
