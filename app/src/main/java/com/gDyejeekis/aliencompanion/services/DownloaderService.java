package com.gDyejeekis.aliencompanion.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.asynctask.GfycatTask;
import com.gDyejeekis.aliencompanion.asynctask.GiphyTask;
import com.gDyejeekis.aliencompanion.asynctask.GyazoTask;
import com.gDyejeekis.aliencompanion.asynctask.StreamableTask;
import com.gDyejeekis.aliencompanion.broadcast_receivers.SyncStateReceiver;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.models.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.retrieval.UserMixed;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.klinker.android.article.ArticleUtils;
import xyz.klinker.android.article.data.Article;
import xyz.klinker.android.article.data.DataSource;

/**
 * Created by sound on 9/25/2015.
 */
public class DownloaderService extends IntentService {

    public static final String TAG = "DownloaderService";

    private static final int FOREGROUND_ID = 574974;

    private static final int CHANGE_STATE_REQUEST_CODE = 59392;

    public static final String MEDIA_DIRECTORY_NAME = "synced_media";

    public static final String INDIVIDUALLY_SYNCED_FILENAME = "synced";

    public static final String LOCA_POST_LIST_SUFFIX = "-posts";

    public static final String LOCAL_THUMNAIL_SUFFIX = "thumb";

    private int MAX_PROGRESS;

    private int progress;

    public static NotificationCompat.Builder notifBuilder;

    public static boolean manuallyPaused = false;

    public static boolean manuallyCancelled = false;

    private HttpClient httpClient = new PoliteRedditHttpClient();

    private NotificationManager notificationManager;

    private PowerManager.WakeLock wakeLock;

    private WifiManager.WifiLock wifiLock;

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        manuallyCancelled = false;
        manuallyPaused = false;
        releaseWakelock();
        super.onDestroy();
    }

    @Override
    public void onHandleIntent(Intent i) {
        MyApplication.checkAccountInit(this, httpClient);

        SyncProfile profile = null;
        int profileId = i.getIntExtra("profileId", -1);
        if(profileId!=-1) {
            profile = SyncProfile.getSyncProfileById(this, profileId);
        }
        //else {
        //    profile = (SyncProfile) i.getSerializableExtra("profile");
        //}

        Submission submission = (Submission) i.getSerializableExtra("post");
        int savedCount = i.getIntExtra("savedCount", 0);

        if(profile != null) {
            SyncProfileOptions syncOptions;
            if(!profile.isUseGlobalSyncOptions() && profile.getSyncOptions()!=null) {
                syncOptions = profile.getSyncOptions();
            }
            else {
                syncOptions = new SyncProfileOptions();
            }
            MAX_PROGRESS = syncOptions.getSyncPostCount() + 1;

            if(!(syncOptions.isSyncOverWifiOnly() && !GeneralUtils.isConnectedOverWifi(this))) {
                String filename;
                // sync subreddits first
                for (String subreddit : profile.getSubreddits()) {
                    progress = 0;
                    filename = subreddit.toLowerCase();
                    notifBuilder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, filename, false));
                    acquireWakelock();

                    syncSubreddit(filename, notifBuilder, subreddit, SubmissionSort.HOT, null, false, syncOptions);
                }
                // sync multireddits
                for(String multireddit : profile.getMultireddits()) {
                    progress = 0;
                    filename = MyApplication.MULTIREDDIT_FILE_PREFIX + multireddit.toLowerCase();
                    notifBuilder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, filename, false));
                    acquireWakelock();

                    syncSubreddit(filename, notifBuilder, multireddit, SubmissionSort.HOT, null, true, syncOptions);
                }
            }

            if(i.getBooleanExtra("reschedule", false)) {
                profile.scheduleAllPendingIntents(this);
            }
        }
        else if(submission != null) {
            MAX_PROGRESS = 1;
            progress = 0;
            notifBuilder = new NotificationCompat.Builder(this);
            String title = (submission.getTitle().length() > 20) ? submission.getTitle().substring(0, 20) : submission.getTitle();
            startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, title, true));
            acquireWakelock();

            syncPost(notifBuilder, submission, INDIVIDUALLY_SYNCED_FILENAME, title, new SyncProfileOptions());
            addToIndividuallySyncedPosts(submission);
        }
        else if(savedCount != 0) {
            MAX_PROGRESS = savedCount + 1;
            progress = 0;

            SyncProfileOptions syncOptions = new SyncProfileOptions();
            String filename = INDIVIDUALLY_SYNCED_FILENAME;
            notifBuilder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, "saved", false));
            acquireWakelock();

            syncSaved(filename, notifBuilder, savedCount, syncOptions);
        }
        else {
            MAX_PROGRESS = MyApplication.syncPostCount + 1;
            progress = 0;
            String subreddit = i.getStringExtra("subreddit");
            boolean isMulti = i.getBooleanExtra("isMulti", false);
            String filename = "";
            if (isMulti) filename = MyApplication.MULTIREDDIT_FILE_PREFIX;
            filename = filename + ((subreddit != null) ? subreddit.toLowerCase() : "frontpage");

            notifBuilder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, filename, false));
            acquireWakelock();

            SubmissionSort submissionSort = (SubmissionSort) i.getSerializableExtra("sort");
            TimeSpan timeSpan = (TimeSpan) i.getSerializableExtra("time");

            syncSubreddit(filename, notifBuilder, subreddit, submissionSort, timeSpan, isMulti, new SyncProfileOptions());
        }
    }

    // call right after starting on foreground
    private void acquireWakelock() {
        String message;

        if(wakeLock == null) {
            Log.d(TAG, "Acquiring wakelock..");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "downloaderServiceWakelock");
            wakeLock.acquire();
            message = (wakeLock.isHeld()) ? "Wakelock acquired" : "Failed to acquire wakelock";
            Log.d(TAG, message);
        }

        if(wifiLock == null) {
            Log.d(TAG, "Acquiring wifilock..");
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "downloaderServiceWifilock");
            wifiLock.acquire();
            message = (wifiLock.isHeld()) ? "Wifilock acquired" : "Failed to acquire wifilock";
            Log.d(TAG, message);
        }
    }

    // call on onDestroy()
    private void releaseWakelock() {
        String message;

        if(wakeLock != null) {
            Log.d(TAG, "Releasing wakelock..");
            wakeLock.release();
            message = (wakeLock.isHeld()) ? "Failed to release wakelock" : "Wakelock released";
            Log.d(TAG, message);
            wakeLock = null;
        }

        if(wifiLock != null) {
            Log.d(TAG, "Releasing wifilock..");
            wifiLock.release();
            message = (wifiLock.isHeld()) ? "Failed to release wifilock" : "Wifilock released";
            Log.d(TAG, message);
            wifiLock = null;
        }
    }

    private void addToIndividuallySyncedPosts(Submission submission) {
        if(submission.getSyncedComments() == null) {
            Log.e(TAG, "Failed to retrieve comments for " + submission.getIdentifier());
            showFailedNotification("Error retrieving comments");
            return;
        }
        try {
            submission.setSyncedComments(null);
            List<Submission> submissions;

            File file = getPreferredStorageFile(INDIVIDUALLY_SYNCED_FILENAME + LOCA_POST_LIST_SUFFIX);
            try {
                submissions = (List<Submission>) GeneralUtils.readObjectFromFile(file);
            } catch (Exception e) {
                submissions = new ArrayList<>();
            }
            for(Submission post : submissions) {
                if(post.getIdentifier().equals(submission.getIdentifier())) {
                    submissions.remove(post);
                    break;
                }
            }
            submissions.add(0, submission);
            GeneralUtils.writeObjectToFile(submissions, file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error updating individually synced posts list");
            showFailedNotification("Error updating synced posts list");
        }
    }

    private void syncSaved(String filename, NotificationCompat.Builder builder, int savedCount, SyncProfileOptions syncOptions) {
        try {
            checkManuallyPaused();
            if(manuallyCancelled) {
                return;
            }
            UserMixed userMixed = new UserMixed(httpClient, MyApplication.currentUser);
            List<RedditItem> savedList = userMixed.ofUser(MyApplication.currentUser.getUsername(), UserSubmissionsCategory.SAVED, null, TimeSpan.ALL, -1, savedCount, null, null, false);
            Collections.reverse(savedList);
            increaseProgress(builder, "saved");
            for(RedditItem item : savedList) {
                Submission s;
                if(item instanceof Submission) {
                    s = (Submission) item;
                    syncPost(builder, s, filename, "saved", syncOptions);
                }
                // comment case
                else {
                    Comment comment = (Comment) item;
                    String url = "https://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().split("_")[1] + "/title_text/" + comment.getIdentifier(); //+ "?context=" + syncOptions.getSyncCommentCount();
                    Comments comments =  new Comments(httpClient, MyApplication.currentUser);
                    comments.setSyncRetrieval(true);
                    s = syncLinkedRedditPost(url, "reddit.com", filename, comments, syncOptions);
                }

                if(s != null) {
                    addToIndividuallySyncedPosts(s);
                }
                else {
                    Log.e(TAG, "Failed to sync saved post");
                }
            }
        } catch (RetrievalFailedException | RedditError e) {
            //e.printStackTrace();
            pauseSync(builder);
            syncSaved(filename, builder, savedCount, syncOptions);
        }
    }

    private void syncSubreddit(String filename, NotificationCompat.Builder builder, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, SyncProfileOptions syncOptions) {
        try {
            checkManuallyPaused();
            if(manuallyCancelled) {
                return;
            }
            Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
            List<RedditItem> posts;

            if (subreddit == null || subreddit.equalsIgnoreCase("frontpage"))
                posts = submissions.frontpage(submissionSort, timeSpan, -1, syncOptions.getSyncPostCount(), null, null, MyApplication.showHiddenPosts);
            else {
                if(isMulti) posts = submissions.ofMultireddit(subreddit, submissionSort, timeSpan, -1, syncOptions.getSyncPostCount(), null, null, MyApplication.showHiddenPosts);
                else posts = submissions.ofSubreddit(subreddit, submissionSort, timeSpan, -1, syncOptions.getSyncPostCount(), null, null, MyApplication.showHiddenPosts);
            }

            if(posts!=null) {
                deletePreviousComments(filename);
                deletePreviousImages(filename);
                writePostsToFile(posts, filename + LOCA_POST_LIST_SUFFIX);
                MAX_PROGRESS = posts.size() + 1;
                increaseProgress(builder, filename);
                for (RedditItem post : posts) {
                    Submission submission = (Submission) post;
                    syncPost(builder, submission, filename, filename, syncOptions);
                }
            }

        } catch (RetrievalFailedException | RedditError e) {
            //e.printStackTrace();
            pauseSync(builder);
            syncSubreddit(filename, builder, subreddit, submissionSort, timeSpan, isMulti, syncOptions);
        }
    }

    private void syncPost(NotificationCompat.Builder builder, Submission submission, String filename, String displayName, SyncProfileOptions syncOptions) {
        try {
            checkManuallyPaused();
            if(manuallyCancelled) {
                return;
            }
            Comments cmntsRetrieval = new Comments(httpClient, MyApplication.currentUser);
            cmntsRetrieval.setSyncRetrieval(true);
            if (syncOptions.isSyncThumbs()) {
                downloadPostThumbnail(submission, filename + submission.getIdentifier() + LOCAL_THUMNAIL_SUFFIX);
            }
            List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
            submission.setSyncedComments(comments);

            String url = submission.getURL();
            String domain = submission.getDomain();
            if (domain.contains("reddit.com") || domain.equals("redd.it")) {
                syncLinkedRedditPost(url, domain, filename, cmntsRetrieval, syncOptions);
            }
            else if (syncOptions.isSyncImages() && GeneralUtils.isImageLink(url, domain)) {
                if (GeneralUtils.canAccessExternalStorage(this)) { // TODO: 1/16/2017 remove this check later down the line
                    downloadPostImage(submission, filename);
                }
            }
            else if(syncOptions.isSyncVideo() && GeneralUtils.isVideoLink(url, domain)) {
                downloadPostVideo(submission, filename);
            }
            else if (syncOptions.isSyncWebpages() && GeneralUtils.isArticleLink(url, domain)) {
                downloadPostArticle(submission, filename);
            }

            writePostToFile(submission, filename + "-" + submission.getIdentifier());
            increaseProgress(builder, displayName);
        } catch (RetrievalFailedException | RedditError e) {
            //e.printStackTrace();
            pauseSync(builder);
            syncPost(builder, submission, filename, displayName, syncOptions);
        }
    }

    private void checkManuallyPaused() {
        if(manuallyPaused && !manuallyCancelled) {
            notifBuilder.setContentText("Sync paused").setSmallIcon(android.R.drawable.ic_media_pause);
            notificationManager.notify(FOREGROUND_ID, notifBuilder.build());
        }
        while(manuallyPaused && !manuallyCancelled) {
            SystemClock.sleep(100);
        }
    }

    private void pauseSync(NotificationCompat.Builder builder) {
        String pauseReason;
        long waitTime;
        if(!GeneralUtils.isNetworkAvailable(this)) {
            pauseReason = "Sync paused (network unavailable). Retrying..";
            waitTime = 1000;
        }
        else {
            pauseReason = "Sync paused (error connecting to reddit). Retrying..";
            waitTime = 2000;
        }

        builder.setContentText(pauseReason).setSmallIcon(android.R.drawable.stat_notify_error);
        notificationManager.notify(FOREGROUND_ID, builder.build());

        //wait x amount of time inbetween retries
        SystemClock.sleep(waitTime);
    }

    public static void manualSyncPause(Context context, NotificationManager notifManager) {
        manuallyPaused = true;
        notifBuilder.setContentText("Pausing..");
        notifBuilder.mActions.set(0, createResumeAction(context));
        notifManager.notify(FOREGROUND_ID, notifBuilder.build());
    }

    public static void manualSyncResume(Context context, NotificationManager notifManager) {
        manuallyPaused = false;
        notifBuilder.setContentText("Resuming..");
        notifBuilder.mActions.set(0, createPauseAction(context));
        notifManager.notify(FOREGROUND_ID, notifBuilder.build());
    }

    public static void manualSyncCancel(NotificationManager notifManager) {
        manuallyCancelled = true;
        notifBuilder.setContentText("Stopping sync..");
        notifManager.notify(FOREGROUND_ID, notifBuilder.build());
    }

    private Notification buildForegroundNotification(NotificationCompat.Builder b, String displayName, boolean indeterminateProgress) {
        b.setOngoing(true);
        b.setContentTitle("Alien Companion")
                .setContentText("Syncing " + displayName +"...")
                .setSmallIcon(android.R.drawable.stat_sys_download).setTicker("Syncing posts...")
                .setProgress(MAX_PROGRESS, progress, indeterminateProgress)
                .addAction(createPauseAction(this))
                .addAction(createCancelAction(this));
        return(b.build());
    }

    private void increaseProgress(NotificationCompat.Builder b, String displayName) {
        progress++;
        Log.d(TAG, progress + "/" + MAX_PROGRESS + " done");
        b.setContentText("Syncing " + displayName + "...").setSmallIcon(android.R.drawable.stat_sys_download).setProgress(MAX_PROGRESS, progress, false);
        notificationManager.notify(FOREGROUND_ID, b.build());
    }

    private void showFailedNotification(String reason) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Sync failed")
                .setContentText(reason)
                .setSmallIcon(android.R.drawable.stat_notify_error);
        notificationManager.notify(FOREGROUND_ID, builder.build());
    }

    public static android.support.v4.app.NotificationCompat.Action createPauseAction(Context context) {
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CHANGE_STATE_REQUEST_CODE,
                new Intent(SyncStateReceiver.SYNC_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_pause_white_48dp, "Pause", pIntent);
    }

    public static android.support.v4.app.NotificationCompat.Action createResumeAction(Context context) {
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CHANGE_STATE_REQUEST_CODE,
                new Intent(SyncStateReceiver.SYNC_RESUME), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_play_arrow_white_48dp, "Resume", pIntent);
    }

    public static android.support.v4.app.NotificationCompat.Action createCancelAction(Context context) {
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CHANGE_STATE_REQUEST_CODE,
                new Intent(SyncStateReceiver.SYNC_CANCEL), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_close_white_48dp, "Cancel", pIntent);
    }
    
    private void writePostsToFile(List<RedditItem> posts, String filename) {
        try {
            File file = getPreferredStorageFile(filename);
            Log.d(TAG, "Writing to " + file.getAbsolutePath());

            FileOutputStream fos;
            ObjectOutputStream oos;
            fos = new FileOutputStream(file);
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
            File file = getPreferredStorageFile(filename);
            Log.d(TAG, "Writing to " + file.getAbsolutePath());

            FileOutputStream fos;
            ObjectOutputStream oos;
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(post);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 8/3/2016 stop syncing and show failed notification
        }
    }

    private File getPreferredStorageFile(String filename) {
        File file;
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(this)) {
            File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
            file = (dirs.length > 1) ? new File(dirs[1], filename) : new File(dirs[0], filename);
        }
        else {
            file = new File(getFilesDir(), filename);
        }
        return file;
    }

    private Submission syncLinkedRedditPost(String url, String domain, String filename, Comments commentsRetrieval, SyncProfileOptions syncOptions) {
        Submission linkedpost = null;
        try {
            checkManuallyPaused();
            if(manuallyCancelled) {
                return null;
            }

            if (domain.equals("redd.it")) {
                linkedpost = new Submission(LinkHandler.getShortRedditId(url));
                List<Comment> comments = commentsRetrieval.ofSubmission(linkedpost, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(),
                        syncOptions.getSyncCommentSort());
                linkedpost.setSyncedComments(comments);
            } else {
                String[] postInfo = LinkHandler.getRedditPostInfo(url);
                if (postInfo != null) {
                    linkedpost = new Submission(postInfo[1]);
                    linkedpost.setSubreddit(postInfo[0]);
                    int parentsShown = (postInfo[3] == null) ? -1 : Integer.valueOf(postInfo[3]);
                    List<Comment> comments = commentsRetrieval.ofSubmission(linkedpost, postInfo[2], parentsShown, syncOptions.getSyncCommentDepth(),
                            syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
                    linkedpost.setSyncedComments(comments);
                }
            }

            if (linkedpost != null) {
                writePostToFile(linkedpost, filename + "-" + linkedpost.getIdentifier());
            }
        } catch (RetrievalFailedException | RedditError e) {
            //e.printStackTrace();
            pauseSync(notifBuilder);
            syncLinkedRedditPost(url, domain, filename, commentsRetrieval, syncOptions);
        }
        return linkedpost;
    }

    private void downloadPostArticle(Submission post, String filename) {
        try {
            ArticleUtils articleUtils = new ArticleUtils(LinkHandler.ARTICLE_API_KEY);
            Article article = articleUtils.loadArticleSync(post.getURL(), DataSource.getInstance(this), null, null);
            post.hasSyncedArticle = (article != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostVideo(Submission post, String filename) {
        String url = post.getURL();
        String domain = post.getDomain();

        File parentFolder;
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(this)) {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(this, null);
            //pictures folder within external files dir
            parentFolder = (externalDirs.length > 1) ? new File(externalDirs[1], "Pictures") : new File(externalDirs[0], "Pictures");
        }
        else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            //app folder inside public pictures directory
            parentFolder = new File(dir, "AlienCompanion");
        }

        if(!parentFolder.exists()) {
            parentFolder.mkdir();
        }
        //folder for the corresponding subreddit
        File subredditFolder = new File(parentFolder, filename);
        if(!subredditFolder.exists()) {
            subredditFolder.mkdir();
        }

        final String path = subredditFolder.getAbsolutePath();

        // VIDEOS
        if(url.endsWith(".mp4")) {
            downloadPostMediaToPath(url, path);
        }
        // STREAMABLE
        else if(domain.contains("streamable.com")) {
            try {
                url = StreamableTask.getStreamableDirectUrl(url);
                downloadPostMediaToPath(url, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadPostImage(Submission post, String filename) {
        String url = post.getURL();
        String domain = post.getDomain();

        File parentFolder;
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(this)) {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(this, null);
            //pictures folder within external files dir
            parentFolder = (externalDirs.length > 1) ? new File(externalDirs[1], "Pictures") : new File(externalDirs[0], "Pictures");
        }
        else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            //app folder inside public pictures directory
            parentFolder = new File(dir, "AlienCompanion");
        }

        if(!parentFolder.exists()) {
            parentFolder.mkdir();
        }
        //folder for the corresponding subreddit
        File subredditFolder = new File(parentFolder, filename);
        if(!subredditFolder.exists()) {
            subredditFolder.mkdir();
        }

        final String folderPath = subredditFolder.getAbsolutePath();

        // GFYCAT
        if (domain.contains("gfycat.com")) {
            try {
                url = GfycatTask.getGfycatDirectUrlSimple(url);
                downloadPostMediaToPath(url, folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // GYAZO
        else if(domain.contains("gyazo.com") && !LinkHandler.isRawGyazoUrl(url)) {
            try {
                url = GyazoTask.getGyazoDirectUrl(url);
                downloadPostMediaToPath(url, folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // GIPHY
        else if(domain.contains("giphy.com") && !LinkHandler.isMp4Giphy(url)) {
            try {
                url = GiphyTask.getGiphyDirectUrlSimple(url);
                downloadPostMediaToPath(url, folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // REDDIT
        else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
            downloadPostImageToFile(url, folderPath, LinkHandler.getReddituploadsFilename(url));
        }
        // IMAGES
        else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            downloadPostMediaToPath(url, folderPath);
        }
        // GIFs
        else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            if (domain.contains("imgur.com")) {
                url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                //url = url.replace(".gif", ".mp4");
            }
            downloadPostMediaToPath(url, folderPath);
        }
        // IMGUR
        else if (domain.contains("imgur.com")) {
            ImgurItem item = null;
            try {
                item = GeneralUtils.getImgurDataFromUrl(new ImgurHttpClient(), url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(item instanceof ImgurImage) {
                ImgurImage image = (ImgurImage) item;
                String link = (image.isAnimated()) ? image.getMp4() : image.getLink();
                downloadPostMediaToPath(link, folderPath);
            }
            else if(item instanceof ImgurAlbum) {
                downloadAlbumImages(item, filename, folderPath);
            }
            else if(item instanceof ImgurGallery) {
                ImgurGallery gallery = (ImgurGallery) item;
                if(gallery.isAlbum()) {
                    downloadAlbumImages(gallery, filename, folderPath);
                }
                else {
                    String link = (gallery.isAnimated()) ? gallery.getMp4() : gallery.getLink();
                    downloadPostMediaToPath(link, folderPath);
                }
            }
        }
    }

    private void downloadAlbumImages(ImgurItem item, String filename, String folderPath) {
        int i = 0;
        for(ImgurImage img : item.getImages()) {
            if(i >= MyApplication.syncAlbumImgCount) break;
            downloadPostMediaToPath((img.isAnimated()) ? img.getMp4() : img.getLink(), folderPath);
            String imgId = LinkHandler.getImgurImgId(img.getLink());
            if(MyApplication.syncAlbumImgCount > 1) {
                try {
                    GeneralUtils.downloadToFileSync("http://i.imgur.com/" + imgId + "s.jpg", new File(GeneralUtils.getActiveSyncedDataDir(this).getAbsolutePath(), filename + "-" + imgId + "-thumb.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
        int indexExlusive = (MyApplication.syncAlbumImgCount > item.getImages().size()) ? item.getImages().size() : MyApplication.syncAlbumImgCount;
        item.setImages(new ArrayList<ImgurImage>(item.getImages().subList(0, indexExlusive)));
        for(ImgurImage img : item.getImages()) {
            String imgLink = (img.isAnimated()) ? img.getMp4() : img.getLink();
            //img.setLink("file:" + folderPath + "/" + imgLink.replaceAll("https?://", "").replace("/", "(s)"));
            img.setLink(imgLink);
        }
        saveAlbumInfoToFile(item, filename + "-" + item.getId() + "-albumInfo");
    }

    private void saveAlbumInfoToFile(ImgurItem item, final String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(GeneralUtils.getActiveSyncedDataDir(this), filename));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(item);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostMediaToPath(String url, String dir) {
        try {
            // remove any url parameters
            try {
                url = url.substring(0, url.lastIndexOf("?"));
            } catch (Exception e){}
            final String filename = GeneralUtils.urlToFilename(url);
            final File file = new File(dir, filename);
            Log.d("DownloaderService", "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadToFileSync(url, file);

            GeneralUtils.addFileToMediaStore(this, file); // TODO: 1/21/2017  remove this later
            //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //Uri contentUri = Uri.fromFile(file);
            //mediaScanIntent.setData(contentUri);
            //sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImageToFile(String url, String dir, String imgFilename) {
        try {
            imgFilename = imgFilename.replaceAll("https?://", "").replace("/", "(s)");
            final File file = new File(dir, imgFilename);
            Log.d("DownloaderService", "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadToFileSync(url, file);

            GeneralUtils.addFileToMediaStore(this, file);
            //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //Uri contentUri = Uri.fromFile(file);
            //mediaScanIntent.setData(contentUri);
            //sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostThumbnail(Submission post, String filename) {

        if(!post.isSelf()) {
            File file = getPreferredStorageFile(filename);
            saveBitmapToDisk(getBitmapFromURL(post.getThumbnail()), file);
        }
    }

    private void saveBitmapToDisk(Bitmap bmp, File file) {
        FileOutputStream out = null;
        try {
            //out = openFileOutput(filename, Context.MODE_PRIVATE);
            out = new FileOutputStream(file);
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

    private void deletePreviousImages(final String filename) {
        CleaningUtils.clearSyncedImages(this, filename);
    }

    private void deletePreviousComments(final String subreddit) {
        CleaningUtils.clearSyncedPostsAndComments(this, subreddit);
    }

}
