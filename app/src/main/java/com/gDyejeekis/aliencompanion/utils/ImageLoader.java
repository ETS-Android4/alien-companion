package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by George on 6/19/2015.
 */
public class ImageLoader {

    //TODO: Load and Cache Images with Universal Image Loader instead of Picasso

    public static void preloadUserImages(List<RedditItem> posts, Context context) {
        //if (BuildConfig.DEBUG) {
        //    Picasso.with(context).setIndicatorsEnabled(true);
        //    Picasso.with(context).setLoggingEnabled(true);
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
        //    Picasso.with(context).setIndicatorsEnabled(true);
        //    Picasso.with(context).setLoggingEnabled(true);
        //}
        for(RedditItem submission : posts) {
            if(submission.getThumbnailObject()==null) {
                Thumbnail thumbnail;
                if(MyApplication.offlineModeEnabled) { //load thumbnail from disk
                    thumbnail = getOfflineThumbnailObject(context, (Submission) submission);
                }
                else {
                    thumbnail = new Thumbnail(submission.getThumbnail());
                    try {
                        Picasso.with(context).load(thumbnail.getUrl()).fetch();
                        thumbnail.setHasThumbnail(true);
                    } catch (IllegalArgumentException e) {
                        thumbnail.setHasThumbnail(false);
                    }
                }
                submission.setThumbnailObject(thumbnail);
            }
            else { //thumbnail object already exists?
                try {
                    Picasso.with(context).load(submission.getThumbnailObject().getUrl()).fetch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Thumbnail getOfflineThumbnailObject(Context context, final Submission post) {
        if(post.isSelf()) {
            return new Thumbnail("self");
        }
        else if(post.isNSFW() && !MyApplication.showNSFWpreview) {
            return new Thumbnail("nsfw");
        }
        else {
            File thumbsDir = GeneralUtils.checkSyncedThumbnailsDir(context);
            if(thumbsDir != null) {
                File thumbFile = StorageUtils.findFile(thumbsDir, thumbsDir.getAbsolutePath(), post.getIdentifier());
                if (thumbFile != null) {
                    Thumbnail thumbnail = new Thumbnail("file:" + thumbFile.getAbsolutePath());
                    boolean hasThumbnail = false;
                    try {
                        hasThumbnail = !ConvertUtils.getDomainName(post.getThumbnail()).equals("null");
                    } catch (Exception e) {}
                    thumbnail.setHasThumbnail(hasThumbnail);
                    return thumbnail;
                }
            }
        }
        return new Thumbnail();
    }

    public static void preloadThumbnail(Submission submission, Context context) {
        if(submission.getThumbnailObject()==null) {
            //Log.d("geotest", "thumbnail object null");
            if(MyApplication.offlineModeEnabled) {
                submission.setThumbnailObject(getOfflineThumbnailObject(context, submission));
            }
            else {
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
        //else {
        //    Log.d("geotest", "thumbnail object exists");
        //}
    }

}
