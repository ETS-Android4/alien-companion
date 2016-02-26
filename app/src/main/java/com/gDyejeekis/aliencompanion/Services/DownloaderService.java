package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.squareup.picasso.Picasso;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by sound on 9/25/2015.
 */
public class DownloaderService extends IntentService {

    private static final int FOREGROUND_ID = 574974;

    public static final String LOCAL_THUMNAIL_SUFFIX = "thumb";

    private int MAX_PROGRESS;

    private int progress;

    private HttpClient httpClient = new PoliteRedditHttpClient();

    private NotificationManager notificationManager;

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MAX_PROGRESS = MyApplication.syncPostCount + 1;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onHandleIntent(Intent i) {
        //Log.d("geo test", "downloading posts...");
        List<String> subreddits = i.getStringArrayListExtra("subreddits");
        if(subreddits != null) {
            for(String subreddit : subreddits) {
                progress = 0;
                String filename;
                boolean isMulti = false;
                if(subreddit.endsWith(" (multi)")) {
                    filename = MyApplication.MULTIREDDIT_FILE_PREFIX + subreddit;
                    isMulti = true;
                }
                else {
                    filename = subreddit;
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                startForeground(FOREGROUND_ID, buildForegroundNotification(builder, filename));

                syncSubreddit(filename, builder, subreddit, SubmissionSort.HOT, null, isMulti);
            }
        }
        else {
            progress = 0;
            String subreddit = i.getStringExtra("subreddit");
            boolean isMulti = i.getBooleanExtra("isMulti", false);
            String filename = "";
            if (isMulti) filename = MyApplication.MULTIREDDIT_FILE_PREFIX;
            filename = filename + ((subreddit != null) ? subreddit.toLowerCase() : "frontpage");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification(builder, filename));

            SubmissionSort submissionSort = (SubmissionSort) i.getSerializableExtra("sort");
            TimeSpan timeSpan = (TimeSpan) i.getSerializableExtra("time");

            syncSubreddit(filename, builder, subreddit, submissionSort, timeSpan, isMulti);
        }
    }

    private void syncSubreddit(String filename, NotificationCompat.Builder builder, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti) {
        try {
            Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
            Comments cmntsRetrieval = new Comments(httpClient, MyApplication.currentUser);
            List<RedditItem> posts;

            if (subreddit == null || subreddit.equals("frontpage"))
                posts = submissions.frontpage(submissionSort, timeSpan, -1, MyApplication.syncPostCount, null, null, MyApplication.showHiddenPosts);
            else {
                if(isMulti) posts = submissions.ofMultireddit(subreddit, submissionSort, timeSpan, -1, MyApplication.syncPostCount, null, null, MyApplication.showHiddenPosts);
                else posts = submissions.ofSubreddit(subreddit, submissionSort, timeSpan, -1, MyApplication.syncPostCount, null, null, MyApplication.showHiddenPosts);
            }

            if(posts!=null) {
                deletePreviousComments(filename);
                writePostsToFile(posts, filename);
                for (RedditItem post : posts) {
                    increaseProgress(builder);
                    Submission submission = (Submission) post;
                    if(MyApplication.syncThumbnails) {
                        downloadPostThumbnail(submission, filename + submission.getIdentifier() + LOCAL_THUMNAIL_SUFFIX);
                    }
                    List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, MyApplication.syncCommentDepth, MyApplication.syncCommentCount, MyApplication.syncCommentSort);
                    submission.setSyncedComments(comments);
                    writePostToFile(submission, filename + submission.getIdentifier());
                }
            }

        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        }
    }

    private Notification buildForegroundNotification(NotificationCompat.Builder b, String filename) {
        b.setOngoing(true);
        b.setContentTitle("Alien Companion")
                .setContentText("Syncing " + filename +"...")
                .setSmallIcon(android.R.drawable.stat_sys_download).setTicker("Syncing posts...").setProgress(MAX_PROGRESS, 0, false);
        return(b.build());
    }

    private void increaseProgress(NotificationCompat.Builder b) {
        progress++;
        b.setProgress(MAX_PROGRESS, progress, false);
        notificationManager.notify(FOREGROUND_ID, b.build());
    }

    private void writePostsToFile(List<RedditItem> posts, String filename) {
        try {
            Log.d("Geo test", "writing posts to " + filename);
            //File dir = new File(getFilesDir(), filename + "_posts");
            //dir.mkdirs();

            FileOutputStream fos;
            ObjectOutputStream oos;
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(posts);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePostToFile(Submission post, String filename) {
        try {
            Log.d("Geo test", "writing comments to " + filename);
            //File path = new File(directoryName, filename);

            FileOutputStream fos;
            ObjectOutputStream oos;
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(post);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadPostThumbnail(Submission post, String filename) {

        if(!post.isSelf()) {
            saveBitmapToDisk(getBitmapFromURL(post.getThumbnail()), filename);
        }
    }

    private void saveBitmapToDisk(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = openFileOutput(filename, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private void deletePreviousComments(final String subreddit) {
        //File dir = getFilesDir();
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.length()>=subreddit.length() && filename.substring(0, subreddit.length()).equals(subreddit)) return true;
                return false;
            }
        };
        File[] files = getFilesDir().listFiles(filenameFilter);
        for(File file : files) {
            Log.d("Geo test", "Deleting " + file.getName());
            file.delete();
        }
    }

}
