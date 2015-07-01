package com.george.redditreader.Utils;

import android.content.Context;

import com.george.redditreader.Models.Thumbnail;
import com.george.redditreader.api.entity.Submission;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by George on 6/19/2015.
 */
public class ImageLoader {

    //TODO: Load and Cache Images with Universal Image Loader instead of Picasso
    //public static List<Thumbnail> preloadImages(List<Submission> posts, Context context) {
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

    public static void preloadUserImages(List<Object> posts, Context context) {
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

    public static void preloadImages(List<Submission> posts, Context context) {
        //if (BuildConfig.DEBUG) {
        //    Picasso.with(activity).setIndicatorsEnabled(true);
        //    Picasso.with(activity).setLoggingEnabled(true);
        //}
        for(Submission submission : posts) {
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
