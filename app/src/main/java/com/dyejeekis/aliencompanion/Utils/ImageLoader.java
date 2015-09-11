package com.dyejeekis.aliencompanion.Utils;

import android.content.Context;

import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Models.Thumbnail;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by George on 6/19/2015.
 */
public class ImageLoader {

    //TODO: Load and Cache Images with Universal Image Loader instead of Picasso
    //public static List<Thumbnail> preloadThumbnails(List<Submission> posts, Context context) {
    //    //if (BuildConfig.DEBUG) {
    //    //    Picasso.with(activity).setIndicatorsEnabled(true);
    //    //    Picasso.with(activity).setLoggingEnabled(true);
    //    //}
    //    List<Thumbnail> thumbnails = new ArrayList<>();
    //    for(Submission post : posts) {
    //        Thumbnail thumbnail = new Thumbnail(post.getThumbnail());
    //        try {
    //            Picasso.with(context).load(post.getThumbnail()).fetch();
    //            thumbnail.setHasThumbnail(true);
    //        } catch (IllegalArgumentException e) {
    //            thumbnail.setHasThumbnail(false);
    //        }
    //        thumbnails.add(thumbnail);
    //    }
    //    return  thumbnails;
    //}

    public static void preloadUserImages(List<RedditItem> posts, Context context) {
        //if (BuildConfig.DEBUG) {
        //    Picasso.with(activity).setIndicatorsEnabled(true);
        //    Picasso.with(activity).setLoggingEnabled(true);
        //}
        for(Object post : posts) {
            if(post instanceof Submission) {
                Submission submission = (Submission) post;
                Thumbnail thumbnail = new Thumbnail(submission.getThumbnail());
                try {
                    Picasso.with(context).load(thumbnail.getUrl()).fetch();
                    thumbnail.setHasThumbnail(true);
                } catch (IllegalArgumentException e) {
                    thumbnail.setHasThumbnail(false);
                }
                submission.setThumbnailObject(thumbnail);
            }
        }
    }

    public static void preloadThumbnails(List<RedditItem> posts, Context context) {
        //if (BuildConfig.DEBUG) {
        //    Picasso.with(activity).setIndicatorsEnabled(true);
        //    Picasso.with(activity).setLoggingEnabled(true);
        //}
        for(RedditItem submission : posts) {
            Thumbnail thumbnail = new Thumbnail(submission.getThumbnail());
            try {
                Picasso.with(context).load(thumbnail.getUrl()).fetch();
                thumbnail.setHasThumbnail(true);
            } catch (IllegalArgumentException e) {
                thumbnail.setHasThumbnail(false);
            }
            submission.setThumbnailObject(thumbnail);
        }
    }

    public static void preloadThumbnail(Submission submission, Context context) {
        Thumbnail thumbnail = new Thumbnail(submission.getThumbnail());
        try {
            Picasso.with(context).load(thumbnail.getUrl()).fetch();
            thumbnail.setHasThumbnail(true);
        } catch (IllegalArgumentException e) {
            thumbnail.setHasThumbnail(false);
        }
        submission.setThumbnailObject(thumbnail);
    }
}
