package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by sound on 9/25/2015.
 */
public class DownloaderService extends IntentService {

    private static final int FOREGROUND_ID = 574974;

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
        progress = 0;
        String subreddit = i.getStringExtra("subreddit");
        String filename = (subreddit!=null) ? subreddit.toLowerCase() : "frontpage";
        //if (subreddit != null) filename = subreddit.toLowerCase();
        //else filename = "frontpage";

        NotificationCompat.Builder builder =new NotificationCompat.Builder(this);
        startForeground(FOREGROUND_ID, buildForegroundNotification(builder, filename));

        SubmissionSort submissionSort = (SubmissionSort) i.getSerializableExtra("sort");
        TimeSpan timeSpan = (TimeSpan) i.getSerializableExtra("time");
        assert submissionSort!=null && timeSpan!=null;

        //HttpClient httpClient = new RedditHttpClient();

        Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
        Comments cmntsRetrieval = new Comments(httpClient, MyApplication.currentUser);
        List<RedditItem> posts = null;

        try {
            if (subreddit == null)
                posts = submissions.frontpage(submissionSort, timeSpan, -1, MyApplication.syncPostCount, null, null, MyApplication.showHiddenPosts);
            else
                posts = submissions.ofSubreddit(subreddit, submissionSort, timeSpan, -1, MyApplication.syncPostCount, null, null, MyApplication.showHiddenPosts);

            if(posts!=null) {
                deletePreviousComments(filename);
                writePostsToFile(posts, filename);
                for (RedditItem post : posts) {
                    increaseProgress(builder);
                    Submission submission = (Submission) post;
                    List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, MyApplication.syncCommentDepth, MyApplication.syncCommentCount, MyApplication.syncCommentSort);
                    submission.setSyncedComments(comments);
                    writePostToFile(submission, filename + submission.getIdentifier());
                }
            }

        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        }

        //if(posts!=null) {
        //    writePostsToFile(posts, filename);
        //    Log.d("geo test", "download complete");
        //}
        //else {
        //    Log.d("geo test", "download failed");
        //}
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
            //Log.d("Geo test", "writing comments to " + filename);
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

    private void deletePreviousComments(final String subreddit) {
        File dir = getFilesDir();
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.length()>=subreddit.length() && filename.substring(0, subreddit.length()).equals(subreddit)) return true;
                return false;
            }
        };
        File[] files = dir.listFiles(filenameFilter);
        for(File file : files) {
            //Log.d("Geo test", "Deleting " + file.getName());
            file.delete();
        }
    }
}
