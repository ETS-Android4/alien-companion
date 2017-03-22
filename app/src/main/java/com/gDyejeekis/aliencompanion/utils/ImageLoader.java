package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
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
        Thumbnail thumbnail;
        if(post.isSelf()) {
            thumbnail = new Thumbnail("self");
        }
        else if(post.isNSFW() && !MyApplication.showNSFWpreview) {
            thumbnail = new Thumbnail("nsfw");
        }
        else {
            FilenameFilter filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith("thumb") && filename.contains(post.getIdentifier()))
                        return true;
                    return false;
                }
            };
            File[] files = GeneralUtils.getActiveSyncedDataDir(context).listFiles(filenameFilter);
            if(files.length!=0) {
                thumbnail = new Thumbnail("file:" + files[0].getAbsolutePath());
                boolean hasThumbnail = false;
                try {
                    hasThumbnail = !ConvertUtils.getDomainName(post.getThumbnail()).equals("null");
                } catch (Exception e) {}
                thumbnail.setHasThumbnail(hasThumbnail);
            }
            else thumbnail = new Thumbnail();
        }
        return thumbnail;
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
