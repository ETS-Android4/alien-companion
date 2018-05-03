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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
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
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FilenameFilter;
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

    private static final boolean SYNC_OVER_MAX_LIMIT_LISTING = true;

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
        final String profileId = i.getStringExtra("profileId");
        if (profileId!=null) {
            profile = (SyncProfile) Profile.getProfileById(profileId, new File(getFilesDir(), AppConstants.SYNC_PROFILES_FILENAME));
        }
        Submission submission = (Submission) i.getSerializableExtra("post");
        int savedCount = i.getIntExtra("savedCount", 0);

        if (profile != null) {
            SyncProfileOptions syncOptions;
            if (!profile.isUseGlobalSyncOptions() && profile.getSyncOptions()!=null) {
                syncOptions = profile.getSyncOptions();
            } else {
                syncOptions = new SyncProfileOptions();
            }
            MAX_PROGRESS = syncOptions.getSyncPostCount() + 1;

            if (!(syncOptions.isSyncOverWifiOnly() && !GeneralUtils.isConnectedOverWifi(this))) {
                String filename;
                // sync subreddits first
                for (String subreddit : profile.getSubreddits()) {
                    progress = 0;
                    filename = subreddit.toLowerCase();
                    notifBuilder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(filename, false));
                    acquireWakelock();

                    syncSubredditControlled(filename, subreddit, SubmissionSort.HOT, null, false, syncOptions);
                }
                // sync multireddits
                for (String multireddit : profile.getMultireddits()) {
                    progress = 0;
                    filename = AppConstants.MULTIREDDIT_FILE_PREFIX + multireddit.toLowerCase();
                    notifBuilder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(filename, false));
                    acquireWakelock();

                    syncSubredditControlled(filename, multireddit, SubmissionSort.HOT, null, true, syncOptions);
                }
            }

            if (i.getBooleanExtra("reschedule", false)) profile.scheduleAllPendingIntents(this);
        } else if (submission != null) {
            MAX_PROGRESS = 1;
            progress = 0;
            notifBuilder = new NotificationCompat.Builder(this);
            String title = (submission.getTitle().length() > 20) ? submission.getTitle().substring(0, 20) : submission.getTitle();
            startForeground(FOREGROUND_ID, buildForegroundNotification(title, true));
            acquireWakelock();

            syncPostControlled(submission, AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME, title, new SyncProfileOptions());
            addToIndividuallySyncedPosts(submission);
        } else if (savedCount != 0) {
            MAX_PROGRESS = savedCount + 1;
            progress = 0;

            SyncProfileOptions syncOptions = new SyncProfileOptions();
            notifBuilder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification("saved", false));
            acquireWakelock();

            syncSavedControlled(AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME, savedCount, syncOptions);
        } else {
            MAX_PROGRESS = MyApplication.syncPostCount + 1;
            progress = 0;
            String subreddit = i.getStringExtra("subreddit");
            boolean isMulti = i.getBooleanExtra("isMulti", false);
            SyncProfileOptions syncOptions = (SyncProfileOptions) i.getSerializableExtra("syncOptions");
            if (syncOptions==null) syncOptions = new SyncProfileOptions();
            String filename = "";
            if (isMulti) filename = AppConstants.MULTIREDDIT_FILE_PREFIX;
            filename = filename + ((subreddit != null) ? subreddit.toLowerCase() : "frontpage");

            notifBuilder = new NotificationCompat.Builder(this);
            startForeground(FOREGROUND_ID, buildForegroundNotification(filename, false));
            acquireWakelock();

            SubmissionSort submissionSort = (SubmissionSort) i.getSerializableExtra("sort");
            TimeSpan timeSpan = (TimeSpan) i.getSerializableExtra("time");

            syncSubredditControlled(filename, subreddit, submissionSort, timeSpan, isMulti, syncOptions);
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

    /*
     * adds post to the top of individually synced post list (doesn't sync the actual post)
     */
    private void addToIndividuallySyncedPosts(Submission submission) {
        submission.setSyncedComments(null);
        try {
            List<Submission> submissions;
            File syncedDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedRedditDataDir(this), AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME);
            if(syncedDir == null) {
                throw new RuntimeException();
            }

            File syncedListFile = new File(syncedDir, AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME +
                    AppConstants.SYNCED_POST_LIST_SUFFIX);
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
            showFailedNotification();
        }
    }

    // unsafe method
    private List<RedditItem> retrieveSavedPostsToSync(User user, int savedCount, Submission after) {
        UserMixed userMixed = new UserMixed(httpClient, user);
        List<RedditItem> posts = userMixed.ofUser(user.getUsername(), UserSubmissionsCategory.SAVED, null,
                TimeSpan.ALL, -1, savedCount, after, null, false);

        if (posts!=null && !posts.isEmpty()) {
            if (SYNC_OVER_MAX_LIMIT_LISTING && savedCount > RedditConstants.MAX_LIMIT_LISTING) {
                posts.addAll(retrieveSavedPostsToSync(user, savedCount - RedditConstants.MAX_LIMIT_LISTING,
                        (Submission) posts.get(posts.size()-1)));
            }
        }
        return posts;
    }

    private void syncSavedControlled(String filename, int savedCount, SyncProfileOptions syncOptions) {
        boolean success;
        do {
            checkManuallyPaused();
            if (manuallyCancelled) {
                return;
            }
            success = syncSaved(filename, savedCount, syncOptions);
            if (!success) pauseSync();
        } while (!success);
    }

    private boolean syncSaved(String filename, int savedCount, SyncProfileOptions syncOptions) {
        try {
            final String displayName = "saved";
            List<RedditItem> savedList = retrieveSavedPostsToSync(MyApplication.currentUser, savedCount, null);
            Collections.reverse(savedList);
            MAX_PROGRESS = savedList.size() + 1;
            increaseProgress(displayName);
            for(RedditItem item : savedList) {
                Submission s = null;
                boolean skippedPost = false;
                if(syncOptions.isSyncNewPostsOnly()) {
                    String postId = null;
                    if (item instanceof Submission) {
                        postId = item.getIdentifier();
                    } else if (item instanceof Comment) {
                        postId = ((Comment) item).getLinkId();
                    }

                    if(postId!=null) {
                        if(isPostSynced(postId, filename)) {
                            skippedPost = true;
                        }
                        else {
                            s = syncSavedPost(item, filename, displayName, syncOptions);
                        }
                    }
                }
                else {
                    s = syncSavedPost(item, filename, displayName, syncOptions);
                }

                if(s != null) {
                    if(s.getSyncedComments() != null) {
                        addToIndividuallySyncedPosts(s);
                    }
                    else {
                        Log.e(TAG, "Failed to retrieve comments for " + s.getIdentifier());
                    }
                }
                else if(skippedPost) {
                    Log.d(TAG, "Skipped post (already synced)");
                }
                else {
                    Log.e(TAG, "Failed to sync saved post");
                }
            }
            return true;
        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * syncs and returns given saved post/comment, checks for pause/cancellation, auto-pauses on error, increments progress
     */
    private Submission syncSavedPost(RedditItem item, String filename, String displayName, SyncProfileOptions syncOptions) {
        Submission s;
        if(item instanceof Submission) {
            s = (Submission) item;
            syncPostControlled(s, filename, displayName, syncOptions);
        }
        // comment case
        else {
            Comment comment = (Comment) item;
            String url = "https://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().split("_")[1] + "/title_text/" + comment.getIdentifier(); //+ "?context=" + syncOptions.getSyncCommentCount();
            s = syncLinkedRedditPostControlled(url, "reddit.com", filename, syncOptions);
            increaseProgress(displayName);
        }
        return s;
    }

    // unsafe method
    private List<RedditItem> retrievePostsToSync(String subreddit,  SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, int syncPostCount, Submission after) {
        Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
        List<RedditItem> posts;
        if (subreddit == null || subreddit.equalsIgnoreCase("frontpage"))
            posts = submissions.frontpage(submissionSort, timeSpan, -1, syncPostCount, after, null, MyApplication.showHiddenPosts);
        else {
            if(isMulti) posts = submissions.ofMultireddit(subreddit, submissionSort, timeSpan, -1, syncPostCount, after, null, MyApplication.showHiddenPosts);
            else posts = submissions.ofSubreddit(subreddit, submissionSort, timeSpan, -1, syncPostCount, after, null, MyApplication.showHiddenPosts);
        }

        if (posts!=null && !posts.isEmpty()) {
            if (SYNC_OVER_MAX_LIMIT_LISTING && syncPostCount > RedditConstants.MAX_LIMIT_LISTING)
                posts.addAll(retrievePostsToSync(subreddit, submissionSort, timeSpan, isMulti,
                        syncPostCount - RedditConstants.MAX_LIMIT_LISTING, (Submission) posts.get(posts.size()-1)));
        }
        return posts;
    }

    private void syncSubredditControlled(String filename, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, SyncProfileOptions syncOptions) {
        boolean success;
        do {
            checkManuallyPaused();
            if (manuallyCancelled) {
                return;
            }
            success = syncSubreddit(filename, subreddit, submissionSort, timeSpan, isMulti, syncOptions);
            if (!success) pauseSync();
        } while (!success);
    }

    private boolean syncSubreddit(String filename, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, SyncProfileOptions syncOptions) {
        try {
            List<RedditItem> posts = retrievePostsToSync(subreddit, submissionSort, timeSpan, isMulti, syncOptions.getSyncPostCount(), null);

            if(posts!=null && !posts.isEmpty()) {
                posts = FilterUtils.checkProfiles(this, posts, subreddit == null ? "frontpage" : subreddit, isMulti);
                if(syncOptions.isSyncNewPostsOnly()) {
                    clearUnlistedSyncedPosts(posts, filename);
                }
                else {
                    CleaningUtils.clearAllSyncedData(this, filename);
                }
                MAX_PROGRESS = posts.size() + 1;
                writePostListToFile(posts, filename);
                increaseProgress(filename);
                for (RedditItem item : posts) {
                    Submission post = (Submission) item;
                    if(syncOptions.isSyncNewPostsOnly() && isPostSynced(post.getIdentifier(), filename)) {
                        updateSyncedPostDetails(post, filename, filename);
                    }
                    else {
                        syncPostControlled(post, filename, filename, syncOptions);
                    }
                    // set comments to null before we update the post list file
                    post.setSyncedComments(null);
                    // update synced post details in list
                    writePostListToFile(posts, filename);
                }
            }
            return true;
        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * updates synced post details, checks for pause/cancellation, increments progress
     */
    private void updateSyncedPostDetails(Submission updated, String filename, String displayName) {
        checkManuallyPaused();
        if (manuallyCancelled) {
            return;
        }
        try {
            //Log.d(TAG, "Updating synced post details " + updated.getIdentifier());
            File file = new File(GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(this), filename), updated.getIdentifier());
            Submission post = (Submission) GeneralUtils.readObjectFromFile(file);
            post.updateSubmission(updated);
            GeneralUtils.writeObjectToFile(post, file);
            increaseProgress(displayName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncPostControlled(Submission submission, String filename, String displayName, SyncProfileOptions syncOptions) {
        boolean success;
        do {
            checkManuallyPaused();
            if (manuallyCancelled) {
                return;
            }
            success = syncPost(submission, filename, displayName, syncOptions);
            if (!success) pauseSync();
        } while (!success);
    }

    private boolean syncPost(Submission submission, String filename, String displayName, SyncProfileOptions syncOptions) {
        try {
            Comments cmntsRetrieval = new Comments(httpClient, MyApplication.currentUser);
            cmntsRetrieval.setSyncRetrieval(true);
            if (syncOptions.isSyncThumbs()) {
                downloadPostThumbnail(submission, filename);
            }
            List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
            submission.setSyncedComments(comments);
            writePostToFile(submission, filename);

            if(!submission.isSelf()) {
                syncUrl(submission.getURL(), submission.getDomain(), filename, syncOptions);
            }
            else if(syncOptions.getSyncSelfTextLinkCount() > 0 && submission.getSelftextHTML() != null) {
                syncSelfTextLinks(submission, filename, syncOptions);
            }

            if(syncOptions.getSyncCommentLinkCount() > 0) {
                syncCommentLinks(submission, filename, syncOptions);
            }

            increaseProgress(displayName);
            return true;
        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) { // illegal argument exception means incorrect json response format, no point in retrying
            e.printStackTrace();
            return true;
        }
        return false;
    }

    /*
     * syncs urls from the (synced) self-text of the given self-post, checks for pause/cancellation
     */
    private void syncSelfTextLinks(Submission post, String filename, SyncProfileOptions syncOptions) {
        final int syncLimit = syncOptions.getSyncSelfTextLinkCount();
        int syncCount = 0;
        Document doc = Jsoup.parse(post.getSelftextHTML());
        Elements links = doc.select("a[href]");
        for(Element element : links) {
            if(syncCount < syncLimit) {
                try {
                    String url = element.attr("href");
                    syncUrl(url, LinkUtils.getDomainName(url), filename, syncOptions);
                    syncCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                break;
            }
        }
    }

    /*
     * syncs urls from the (synced) comments of the given post, checks for pause/cancellation
     */
    private void syncCommentLinks(Submission post, String filename, SyncProfileOptions syncOptions) {
        final int syncLimit = syncOptions.getSyncCommentLinkCount();
        int syncCount = 0;
        for(Comment comment : post.getSyncedComments()) {
            syncCount = syncCommentLinksRecursive(comment, syncCount, syncLimit, filename, syncOptions);
        }
    }


    /*
     * syncs comment links and links in replies recursively, returns current sync count, checks for pause/cancellation
     */
    private int syncCommentLinksRecursive(Comment comment, int syncCount, final int syncLimit, String filename, SyncProfileOptions syncOptions) {
        if(syncCount < syncLimit) {
            Document doc = Jsoup.parse(comment.getBodyHTML());
            Elements links = doc.select("a[href]");
            for (Element element : links) {
                try {
                    String url = element.attr("href");
                    syncUrl(url, LinkUtils.getDomainName(url), filename, syncOptions);
                    syncCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(syncCount >= syncLimit) {
                    return syncCount;
                }
            }

            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                for (Comment c : comment.getReplies()) {
                    syncCount = syncCommentLinksRecursive(c, syncCount, syncLimit, filename, syncOptions);
                }
            }

        }
        return syncCount;
    }

    /*
     * syncs the given url depending on sync options used, checks for pause/cancellation
     */
    private void syncUrl(String url, String domain, String filename, SyncProfileOptions syncOptions) {
        checkManuallyPaused();
        if(manuallyCancelled) {
            return;
        }

        if (LinkUtils.isRedditPostUrl(url)) {
            syncLinkedRedditPostControlled(url, domain, filename, syncOptions);
        } else if (syncOptions.isSyncImages() && LinkUtils.isImageLink(url, domain)) {
            syncImage(url, filename, syncOptions);
        } else if (syncOptions.isSyncVideo() && LinkUtils.isVideoLink(url, domain)) {
            syncVideo(url, filename);
        } else if (syncOptions.isSyncWebpages() && LinkUtils.isArticleLink(url, domain)) {
            syncArticle(url, filename);
        }
    }

    private Submission syncLinkedRedditPostControlled(String url, String domain, String filename, SyncProfileOptions syncOptions) {
        Submission post;
        do {
            checkManuallyPaused();
            if (manuallyCancelled) {
                return null;
            }
            post = syncLinkedRedditPost(url, domain, filename, syncOptions);
            if (post == null) pauseSync();
        } while (post == null);
        return post;
    }

    private Submission syncLinkedRedditPost(String url, String domain, String filename, SyncProfileOptions syncOptions) {
        try {
            Submission linkedPost;
            Comments cmntsRetrieval = new Comments(httpClient, MyApplication.currentUser);
            cmntsRetrieval.setSyncRetrieval(true);

            linkedPost = LinkUtils.getRedditPostFromUrl(url);
            if (linkedPost != null) {
                CommentSort commentSort = linkedPost.getPreferredSort()==null ? syncOptions.getSyncCommentSort() : linkedPost.getPreferredSort();
                List<Comment> comments = cmntsRetrieval.ofSubmission(linkedPost, linkedPost.getLinkedCommentId(), linkedPost.getParentsShown(), syncOptions.getSyncCommentDepth(),
                        syncOptions.getSyncCommentCount(), commentSort);
                linkedPost.setSyncedComments(comments);
                // TODO: 7/30/2017 thumbnails don't seem to sync or load properly here
                //if (syncOptions.isSyncThumbs()) {
                //    downloadPostThumbnail(linkedPost, filename);
                //}
                writePostToFile(linkedPost, filename);
            }
            return linkedPost;
        } catch (RetrievalFailedException | RedditError e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) { // incorrect json response, don't retry
            e.printStackTrace();
            return new Submission("t3_null");
        }
        return null;
    }

    private void clearUnlistedSyncedPosts(List<RedditItem> posts, String filename) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(this), filename);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.endsWith(AppConstants.SYNCED_POST_LIST_SUFFIX);
            }
        };
        File[] files = dir.listFiles(filter);
        if(files!=null) {
            for (File file : files) {
                boolean isListed = false;
                for (RedditItem post : posts) {
                    if (post.getIdentifier().equals(file.getName())) {
                        isListed = true;
                        break;
                    }
                }
                if (!isListed) {
                    Log.d(TAG, "Clearing unlisted synced post " + file.getName());
                    CleaningUtils.clearSyncedPostFromCategory(this, filename, file.getName());
                }
            }
        }
    }

    private boolean isPostSynced(String postId, String filename) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(this), filename);
        File file = new File(dir, postId);
        return file.exists();
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

    private void pauseSync() {
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

        notifBuilder.setContentText(pauseReason).setSmallIcon(android.R.drawable.stat_notify_error);
        notificationManager.notify(FOREGROUND_ID, notifBuilder.build());

        //wait x amount of time inbetween retries
        SystemClock.sleep(waitTime);
    }

    public static void manualSyncPause(Context context) {
        manuallyPaused = true;
        try {
            notifBuilder.setContentText("Pausing..");
            notifBuilder.mActions.set(0, createResumeAction(context));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(FOREGROUND_ID, notifBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void manualSyncResume(Context context) {
        manuallyPaused = false;
        try {
            notifBuilder.setContentText("Resuming..");
            notifBuilder.mActions.set(0, createPauseAction(context));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(FOREGROUND_ID, notifBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void manualSyncCancel(Context context) {
        manuallyCancelled = true;
        try {
            notifBuilder.setContentText("Stopping sync..");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(FOREGROUND_ID, notifBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildForegroundNotification(String displayName, boolean indeterminateProgress) {
        notifBuilder.setOngoing(true);
        notifBuilder.setContentTitle("Alien Companion")
                .setContentText("Syncing " + displayName +"...")
                .setSmallIcon(android.R.drawable.stat_sys_download).setTicker("Syncing posts...")
                .setProgress(MAX_PROGRESS, progress, indeterminateProgress)
                .addAction(createPauseAction(this))
                .addAction(createCancelAction(this));
        return(notifBuilder.build());
    }

    private void increaseProgress(String displayName) {
        progress++;
        Log.d(TAG, progress + "/" + MAX_PROGRESS + " done");
        if (!manuallyPaused && !manuallyCancelled) {
            notifBuilder.setContentText("Syncing " + displayName + "...").setSmallIcon(android.R.drawable.stat_sys_download).setProgress(MAX_PROGRESS, progress, false);
            notificationManager.notify(FOREGROUND_ID, notifBuilder.build());
        }
    }

    private void showFailedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Alien Companion")
                .setContentText("Sync failed")
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
            File file = new File(dir, filename + AppConstants.SYNCED_POST_LIST_SUFFIX);
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
    }

    private void syncArticle(String url, String filename) {
        File subredditDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedArticlesDir(this), filename);
        if(subredditDir == null) {
            return;
        }

        try {
            HtmlFetcher fetcher = new HtmlFetcher();
            // set cache. e.g. take the map implementation from google collections:
            // fetcher.setCache(new MapMaker().concurrencyLevel(20).maximumSize(count).
            //    expireAfterWrite(minutes, TimeUnit.MINUTES).makeMap();

            JResult res = fetcher.fetchAndExtract(url, 5000, true);
            List<String> textList = res.getTextList();
            StringBuilder text = new StringBuilder();
            for (String item : textList) {
                text.append(item).append("\n\n");
            }
            String title = res.getTitle();
            String imageUrl = res.getImageUrl();
            if(!text.toString().trim().isEmpty()) {
                Log.d(TAG, "Syncing article from src: " + url);
                final String articleId = String.valueOf(url.hashCode());
                Article article = new Article(title, text.toString(), imageUrl);
                GeneralUtils.writeObjectToFile(article, new File(subredditDir, articleId + AppConstants.SYNCED_ARTICLE_DATA_SUFFIX));
                // catch all exceptions related to article image download, not as important
                try {
                    String imageSource = article.getImageSource();
                    //Log.d(TAG, "article image source: " + imageSource);
                    GeneralUtils.downloadToFileSync(imageSource, new File(subredditDir, articleId +
                    AppConstants.SYNCED_ARTICLE_IMAGE_SUFFIX));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncVideo(String url, String filename) {
        File file = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(this), filename);
        if(file == null) {
            return;
        }

        final String path = file.getAbsolutePath();
        // VIDEOS
        if(url.endsWith(".mp4")) {
            downloadMediaToPath(url, path);
        }
        // STREAMABLE
        else if(url.contains("streamable.com")) {
            try {
                url = StreamableTask.getStreamableDirectUrl(url);
                downloadMediaToPath(url, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void syncImage(String url, String filename, SyncProfileOptions syncOptions) {
        File file = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(this), filename);
        if(file == null) {
            return;
        }

        final String path = file.getAbsolutePath();
        // GFYCAT
        if (url.contains("gfycat.com")) {
            try {
                String mp4Url = GfycatTask.getGfycatDirectUrl(url);
                String saveName = LinkUtils.getGfycatId(url).concat(".mp4");
                downloadMediaToPath(mp4Url, path, saveName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // IMGUR
        else if (url.contains("imgur.com")) {
            ImgurItem item = null;
            try {
                item = GeneralUtils.getImgurDataFromUrl(new ImgurHttpClient(), url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(item instanceof ImgurImage) {
                ImgurImage image = (ImgurImage) item;
                String directUrl = (image.isAnimated()) ? image.getMp4() : image.getLink();
                String saveName = LinkUtils.getImgurImgId(url).concat(LinkUtils.getDirectMediaUrlExtension(directUrl));
                downloadMediaToPath(directUrl, path, saveName);
            }
            else if(item instanceof ImgurAlbum) {
                downloadAlbumImages(item, filename, path, syncOptions);
            }
            else if(item instanceof ImgurGallery) {
                ImgurGallery gallery = (ImgurGallery) item;
                if(gallery.isAlbum()) {
                    downloadAlbumImages(gallery, filename, path, syncOptions);
                }
                else {
                    String directUrl = (gallery.isAnimated()) ? gallery.getMp4() : gallery.getLink();
                    String saveName = LinkUtils.getImgurImgId(url).concat(LinkUtils.getDirectMediaUrlExtension(directUrl));
                    downloadMediaToPath(directUrl, path, saveName);
                }
            }
        }
        // GYAZO
        else if(url.contains("gyazo.com")) {
            try {
                String directUrl = GyazoTask.getGyazoDirectUrl(url);
                String saveName = LinkUtils.getGyazoId(url).concat(LinkUtils.getDirectMediaUrlExtension(directUrl));
                downloadMediaToPath(directUrl, path, saveName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // GIPHY
        else if(url.contains("giphy.com")) {
            try {
                String mp4Url = GiphyTask.getGiphyDirectUrl(url);
                String saveName = LinkUtils.getGiphyId(url).concat(".mp4");
                downloadMediaToPath(mp4Url, path, saveName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // REDDIT
        else if(url.contains("i.reddituploads.com") || url.contains("i.redditmedia.com")) {
            downloadMediaToPath(url, path, LinkUtils.urlToFilename(url).concat(".jpg"));
        }
        // REDDIT VIDEO
        else if(url.contains("v.redd.it")) {
            try {
                String mp4Url = getRedditVideoDirectUrl(url);
                String saveName = LinkUtils.urlToFilenameOld(url).concat(".mp4"); // use old method here to make sure we keep the id
                downloadMediaToPath(mp4Url, path, saveName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // IMAGES
        else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            downloadMediaToPath(url, path);
        }
        // GIFs
        else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
            url = url.replaceAll("\\?(\\d+)?", "");
            if (url.contains("imgur.com")) {
                url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                //url = url.replace(".gif", ".mp4");
            }
            downloadMediaToPath(url, path);
        }
    }

    // unsafe method
    private String getRedditVideoDirectUrl(String redditVideoUrl) {
        String postUrl = GeneralUtils.getFinalUrlRedirect(redditVideoUrl);
        Submission post = LinkUtils.getRedditPostFromUrl(postUrl);
        Comments comments = new Comments(httpClient, MyApplication.currentUser);
        comments.ofSubmission(post, null, -1, 1, 1, CommentSort.TOP);
        return post.getRedditVideo().getScrubberMediaUrl();
    }

    private void downloadAlbumImages(ImgurItem item, String filename, String path, SyncProfileOptions syncOptions) {
        final int albumSyncLimit = syncOptions.getAlbumSyncLimit();
        int i = 0;
        for(ImgurImage img : item.getImages()) {
            if(i >= albumSyncLimit) {
                break;
            }
            downloadMediaToPath((img.isAnimated()) ? img.getMp4() : img.getLink(), path);
            if(albumSyncLimit > 1) {
                // sync album thumbnails for grid view
                try {
                    String imgId = LinkUtils.getImgurImgId(img.getLink());
                    File thumbsDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedThumbnailsDir(this), filename);
                    GeneralUtils.downloadToFileSync("http://i.imgur.com/" + imgId + "s.jpg", new File(thumbsDir, imgId + "-thumb"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
        int indexExlusive = (albumSyncLimit > item.getImages().size()) ? item.getImages().size() : albumSyncLimit;
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
            File file = new File(dir, filename + "-" + item.getId() + AppConstants.IMGUR_INFO_FILE_NAME);
            GeneralUtils.writeObjectToFile(item, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadMediaToPath(String url, String path, String filename) {
        try {
            final File file = new File(path, filename);
            Log.d(TAG, "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadToFileSync(url, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadMediaToPath(String url, String path) {
        downloadMediaToPath(url, path, LinkUtils.urlToFilename(url));
    }

    private void downloadPostThumbnail(Submission post, String filename) {
        if(!post.isSelf()) {
            File dir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedThumbnailsDir(this), filename);
            if(dir!=null) {
                downloadMediaToPath(post.getThumbnail(), dir.getAbsolutePath(), post.getIdentifier() + AppConstants.SYNCED_THUMBNAIL_SUFFIX);
            }
        }
    }

}
