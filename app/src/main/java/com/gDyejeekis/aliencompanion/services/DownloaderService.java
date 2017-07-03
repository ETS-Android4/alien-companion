package com.gDyejeekis.aliencompanion.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.asynctask.GfycatTask;
import com.gDyejeekis.aliencompanion.asynctask.GiphyTask;
import com.gDyejeekis.aliencompanion.asynctask.GyazoTask;
import com.gDyejeekis.aliencompanion.asynctask.StreamableTask;
import com.gDyejeekis.aliencompanion.broadcast_receivers.SyncStateReceiver;
import com.gDyejeekis.aliencompanion.models.Article;
import com.gDyejeekis.aliencompanion.models.Profile;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.FilterUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

/**
 * Created by sound on 9/25/2015.
 */
public class DownloaderService extends IntentService {

    public static final String TAG = "DownloaderService";

    private static final int FOREGROUND_ID = 574974;

    private static final int CHANGE_STATE_REQUEST_CODE = 59392;

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
            profile = (SyncProfile) Profile.getProfileById(profileId, new File(getFilesDir(), MyApplication.SYNC_PROFILES_FILENAME));
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

            syncPost(notifBuilder, submission, MyApplication.INDIVIDUALLY_SYNCED_DIR_NAME, title, new SyncProfileOptions());
            addToIndividuallySyncedPosts(submission);
        }
        else if(savedCount != 0) {
            MAX_PROGRESS = savedCount + 1;
            progress = 0;

            SyncProfileOptions syncOptions = new SyncProfileOptions();
            notifBuilder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, "saved", false));
            acquireWakelock();

            syncSaved(MyApplication.INDIVIDUALLY_SYNCED_DIR_NAME, notifBuilder, savedCount, syncOptions);
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
        try {
            submission.setSyncedComments(null);
            List<Submission> submissions;

            File syncedListFile = new File(GeneralUtils.getPreferredSyncDir(this), MyApplication.INDIVIDUALLY_SYNCED_DIR_NAME +
                    MyApplication.SYNCED_POST_LIST_SUFFIX);
            try {
                submissions = (List<Submission>) GeneralUtils.readObjectFromFile(syncedListFile);
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
            GeneralUtils.writeObjectToFile(submissions, syncedListFile);
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
                    if(s.getSyncedComments() != null) {
                        addToIndividuallySyncedPosts(s);
                    }
                    else {
                        Log.e(TAG, "Failed to retrieve comments for " + s.getIdentifier());
                    }
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
                posts = FilterUtils.checkProfiles(this, posts, subreddit == null ? "frontpage" : subreddit, isMulti);
                CleaningUtils.clearAllSyncedData(this, filename);
                MAX_PROGRESS = posts.size() + 1;
                writePostListToFile(posts, filename);
                increaseProgress(builder, filename);
                for (RedditItem post : posts) {
                    syncPost(builder, (Submission) post, filename, filename, syncOptions);
                    if(!manuallyCancelled) {
                        // update post list after syncing each post
                        writePostListToFile(posts, filename);
                    }
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
                downloadPostThumbnail(submission, filename);
            }
            List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
            submission.setSyncedComments(comments);

            if(!submission.isSelf()) {
                String url = submission.getURL();
                String domain = submission.getDomain();
                if (domain.contains("reddit.com") || domain.equals("redd.it")) {
                    syncLinkedRedditPost(url, domain, filename, cmntsRetrieval, syncOptions);
                } else if (syncOptions.isSyncImages() && GeneralUtils.isImageLink(url, domain)) {
                    downloadPostImage(submission, filename);
                } else if (syncOptions.isSyncVideo() && GeneralUtils.isVideoLink(url, domain)) {
                    downloadPostVideo(submission, filename);
                } else if (syncOptions.isSyncWebpages() && GeneralUtils.isArticleLink(url, domain)) {
                    downloadPostArticle(submission, filename);
                }
            }

            writePostToFile(submission, filename);
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
    
    private void writePostListToFile(List<RedditItem> posts, String filename) {
        try {
            File dir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedRedditDataDir(this), filename);
            File file = new File(dir, filename + MyApplication.SYNCED_POST_LIST_SUFFIX);
            Log.d(TAG, "Writing post list to " + file.getAbsolutePath());
            GeneralUtils.writeObjectToFile(posts, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writePostToFile(Submission post, String filename) {
        try {
            File dir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedRedditDataDir(this), filename);
            File file = new File(dir, post.getIdentifier());
            Log.d(TAG, "Writing post to " + file.getAbsolutePath());
            GeneralUtils.writeObjectToFile(post, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // set comments to null to garbage collect and not keep in post list file
        post.setSyncedComments(null);
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
                writePostToFile(linkedpost, filename);
            }
        } catch (RetrievalFailedException | RedditError e) {
            //e.printStackTrace();
            pauseSync(notifBuilder);
            syncLinkedRedditPost(url, domain, filename, commentsRetrieval, syncOptions);
        }
        return linkedpost;
    }

    private void downloadPostArticle(Submission post, String filename) {
        Log.d(TAG, "Syncing article for " + post.getIdentifier() + ", src: " + post.getURL());

        File subredditDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedArticlesDir(this), filename);
        if(subredditDir == null) {
            return;
        }

        try {
            HtmlFetcher fetcher = new HtmlFetcher();
            // set cache. e.g. take the map implementation from google collections:
            // fetcher.setCache(new MapMaker().concurrencyLevel(20).maximumSize(count).
            //    expireAfterWrite(minutes, TimeUnit.MINUTES).makeMap();

            JResult res = fetcher.fetchAndExtract(post.getURL(), 5000, true);
            String text = res.getText();
            String title = res.getTitle();
            String imageUrl = res.getImageUrl();
            Article article = new Article(title, text, imageUrl);
            if(article!= null) {
                GeneralUtils.writeObjectToFile(article, new File(subredditDir, post.getIdentifier() + MyApplication.SYNCED_ARTICLE_DATA_SUFFIX));
                // catch all exceptions related to article image download, not as important
                try {
                    String imageSource = article.getImageSource();
                    Log.d(TAG, "article image source: " + imageSource);
                    GeneralUtils.downloadToFileSync(imageSource, new File(subredditDir, post.getIdentifier() +
                    MyApplication.SYNCED_ARTICLE_IMAGE_SUFFIX));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                post.hasSyncedArticle = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            post.hasSyncedArticle = false;
        }
    }

    private void downloadPostVideo(Submission post, String filename) {
        Log.d(TAG, "Syncing video for " + post.getIdentifier() + ", src: " + post.getURL());

        File file = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(this), filename);
        if(file == null) {
            return;
        }
        final String path = file.getAbsolutePath();

        String url = post.getURL();
        String domain = post.getDomain();
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
        Log.d(TAG, "Syncing image for " + post.getIdentifier() + ", src: " + post.getURL());

        File file = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(this), filename);
        if(file == null) {
            return;
        }
        final String path = file.getAbsolutePath();

        String url = post.getURL();
        String domain = post.getDomain();
        // GFYCAT
        if (domain.contains("gfycat.com")) {
            try {
                url = GfycatTask.getGfycatDirectUrlSimple(url);
                downloadPostMediaToPath(url, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // GYAZO
        else if(domain.contains("gyazo.com") && !LinkHandler.isRawGyazoUrl(url)) {
            try {
                url = GyazoTask.getGyazoDirectUrl(url);
                downloadPostMediaToPath(url, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // GIPHY
        else if(domain.contains("giphy.com") && !LinkHandler.isMp4Giphy(url)) {
            try {
                url = GiphyTask.getGiphyDirectUrlSimple(url);
                downloadPostMediaToPath(url, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // REDDIT
        else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
            downloadPostImageToFile(url, path, LinkHandler.getReddituploadsFilename(url));
        }
        // IMAGES
        else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            downloadPostMediaToPath(url, path);
        }
        // GIFs
        else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            if (domain.contains("imgur.com")) {
                url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                //url = url.replace(".gif", ".mp4");
            }
            downloadPostMediaToPath(url, path);
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
                downloadPostMediaToPath(link, path);
            }
            else if(item instanceof ImgurAlbum) {
                downloadAlbumImages(item, filename, path);
            }
            else if(item instanceof ImgurGallery) {
                ImgurGallery gallery = (ImgurGallery) item;
                if(gallery.isAlbum()) {
                    downloadAlbumImages(gallery, filename, path);
                }
                else {
                    String link = (gallery.isAnimated()) ? gallery.getMp4() : gallery.getLink();
                    downloadPostMediaToPath(link, path);
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
                    GeneralUtils.downloadToFileSync("http://i.imgur.com/" + imgId + "s.jpg", new File(GeneralUtils.getPreferredSyncDir(this).getAbsolutePath(), filename + "-" + imgId + "-thumb.jpg"));
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
            img.setLink(imgLink);
        }
        saveAlbumInfoToFile(item, filename);
    }

    private void saveAlbumInfoToFile(ImgurItem item, final String filename) {
        try {
            File dir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(this), filename);
            File file = new File(dir, filename + "-" + item.getId() + MyApplication.IMGUR_INFO_FILE_NAME);
            GeneralUtils.writeObjectToFile(item, file);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImageToFile(String url, String dir, String filename) {
        try {
            final File file = new File(dir, filename);
            Log.d("DownloaderService", "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadToFileSync(url, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostThumbnail(Submission post, String filename) {
        if(!post.isSelf()) {
            File dir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedThumbnailsDir(this), filename);
            if(dir!=null) {
                downloadPostImageToFile(post.getThumbnail(), dir.getAbsolutePath(), post.getIdentifier() + MyApplication.SYNCED_THUMBNAIL_SUFFIX);
            }
        }
    }

}
