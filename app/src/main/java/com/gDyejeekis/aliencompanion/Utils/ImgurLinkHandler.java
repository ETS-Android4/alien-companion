package com.gDyejeekis.aliencompanion.Utils;

import android.app.Activity;

import com.gDyejeekis.aliencompanion.api.entity.Submission;

/**
 * Created by George on 1/6/2016.
 */
public class ImgurLinkHandler {

    private Activity activity;
    private String originalUrl;
    private String domain;
    private Submission post;

    public ImgurLinkHandler(Activity activity, String url, String domain) {
        this.activity = activity;
        this.originalUrl = url;
        this.domain = domain;
    }

    public ImgurLinkHandler(Activity context, Submission post) {
        this.activity = context;
        this.post = post;
        this.originalUrl = post.getURL();
        this.domain = post.getDomain();
    }

    public static String modifyGifvUrl(String url) {
        return url.replaceFirst("\\.gifv", ".gif");
    }

    public static void handleGifv(Activity activity, Submission post, String url, String domain) {
        if(post!=null) {
            post.setURL(modifyGifvUrl(post.getURL()));
            LinkHandler.startInAppBrowser(activity, post, null, null);
        }
        else {
            LinkHandler.startInAppBrowser(activity, null, modifyGifvUrl(url), domain);
        }
    }
}
