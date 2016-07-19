package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.Models.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.Utils.StorageUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
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
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
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

    public static final String INDIVIDUALLY_SYNCED_FILENAME = "synced";

    public static final String LOCA_POST_LIST_SUFFIX = "-posts";

    public static final String LOCAL_THUMNAIL_SUFFIX = "thumb";

    public static final String LOCAL_ARTICLE_SUFFIX = "-article.html";

    private int MAX_PROGRESS;

    private int progress;

    public static NotificationCompat.Builder notifBuilder;

    public static boolean manuallyPaused = false;

    public static boolean manuallyCancelled = false;

    private HttpClient httpClient = new PoliteRedditHttpClient();

    private NotificationManager notificationManager;

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //MAX_PROGRESS = MyApplication.syncPostCount + 1;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        manuallyCancelled = false;
        manuallyPaused = false;
        super.onDestroy();
    }

    @Override
    public void onHandleIntent(Intent i) {
        if(MyApplication.currentAccount == null) {
            MyApplication.currentAccount = MyApplication.getCurrentAccount(this);
        }
        //Log.d("SYNC_DEBUG", "DownloaderService onHandleIntent...");

        //List<String> subreddits = i.getStringArrayListExtra("subreddits");
        SyncProfile profile = (SyncProfile) i.getSerializableExtra("profile");
        Submission submission = (Submission) i.getSerializableExtra("post");
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
                for (String subreddit : profile.getSubreddits()) {
                    progress = 0;
                    String filename;
                    String subredditName;
                    boolean isMulti = false;
                    if (subreddit.endsWith(" (multi)")) {
                        subredditName = subreddit.replace(" (multi)", "");
                        filename = MyApplication.MULTIREDDIT_FILE_PREFIX + subredditName.toLowerCase();
                        isMulti = true;
                    } else {
                        subredditName = subreddit;
                        filename = subreddit.toLowerCase();
                    }
                    notifBuilder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, filename, false));

                    syncSubreddit(filename, notifBuilder, subredditName, SubmissionSort.HOT, null, isMulti, syncOptions);
                }
            }

            if(i.getBooleanExtra("reschedule", false)) {
                profile.schedulePendingIntents(this);
            }
        }
        else if(submission != null) {
            MAX_PROGRESS = 1;
            progress = 0;
            notifBuilder = new NotificationCompat.Builder(this);
            String title = (submission.getTitle().length() > 20) ? submission.getTitle().substring(0, 20) : submission.getTitle();
            startForeground(FOREGROUND_ID, buildForegroundNotification(notifBuilder, title, true));

            syncPost(notifBuilder, submission, INDIVIDUALLY_SYNCED_FILENAME, title, new SyncProfileOptions());
            addToIndividuallySyncedPosts(submission);
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

            SubmissionSort submissionSort = (SubmissionSort) i.getSerializableExtra("sort");
            TimeSpan timeSpan = (TimeSpan) i.getSerializableExtra("time");

            syncSubreddit(filename, notifBuilder, subreddit, submissionSort, timeSpan, isMulti, new SyncProfileOptions());
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

    private void syncSubreddit(String filename, NotificationCompat.Builder builder, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, SyncProfileOptions syncOptions) {
        try {
            checkManuallyPaused();
            if(manuallyCancelled) {
                return;
            }
            Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
            List<RedditItem> posts;

            if (subreddit == null || subreddit.equals("frontpage"))
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
            if (syncOptions.isSyncThumbs()) {
                downloadPostThumbnail(submission, filename + submission.getIdentifier() + LOCAL_THUMNAIL_SUFFIX);
            }
            List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
            submission.setSyncedComments(comments);

            String url = submission.getURL();
            String domain = submission.getDomain();
            if (domain.contains("reddit.com") || domain.equals("redd.it")) {
                syncLinkedRedditPost(url, domain, filename, cmntsRetrieval, syncOptions);
            } else if (syncOptions.isSyncImages() && GeneralUtils.isImageLink(url, domain)) {
                if (GeneralUtils.canAccessExternalStorage(this)) {
                    downloadPostImage(submission, filename);
                }
            } else if (syncOptions.isSyncWebpages() && GeneralUtils.isArticleLink(url, domain)) {
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

    private Notification buildForegroundNotification(NotificationCompat.Builder b, String filename, boolean indeterminateProgress) {
        b.setOngoing(true);
        b.setContentTitle("Alien Companion")
                .setContentText("Syncing " + filename +"...")
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
                new Intent("com.gDyejeekis.aliencompanion.SYNC_PAUSE"), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.mipmap.ic_pause_white_48dp, "Pause", pIntent);
    }

    public static android.support.v4.app.NotificationCompat.Action createResumeAction(Context context) {
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CHANGE_STATE_REQUEST_CODE,
                new Intent("com.gDyejeekis.aliencompanion.SYNC_RESUME"), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.mipmap.ic_resume_white_48dp, "Resume", pIntent);
    }

    public static android.support.v4.app.NotificationCompat.Action createCancelAction(Context context) {
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CHANGE_STATE_REQUEST_CODE,
                new Intent("com.gDyejeekis.aliencompanion.SYNC_CANCEL"), PendingIntent.FLAG_CANCEL_CURRENT);

        return new android.support.v4.app.NotificationCompat.Action(R.mipmap.ic_close_white_48dp, "Cancel", pIntent);
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
        }
    }

    private File getPreferredStorageFile(String filename) {
        File file;
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable()) {
            File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
            file = (dirs.length > 1) ? new File(dirs[1], filename) : new File(dirs[0], filename);
        }
        else {
            file = new File(getFilesDir(), filename);
        }
        return file;
    }

    private void syncLinkedRedditPost(String url, String domain, String filename, Comments commentsRetrieval, SyncProfileOptions syncOptions) {
        Submission linkedpost = null;
        if(domain.equals("redd.it")) {
            linkedpost = new Submission(LinkHandler.getShortRedditId(url));
            List<Comment> comments = commentsRetrieval.ofSubmission(linkedpost, null, -1, syncOptions.getSyncCommentDepth(), syncOptions.getSyncCommentCount(),
                    syncOptions.getSyncCommentSort());
            linkedpost.setSyncedComments(comments);
        }
        else {
            String[] postInfo = LinkHandler.getRedditPostInfo(url);
            if(postInfo!=null) {
                linkedpost = new Submission(postInfo[1]);
                linkedpost.setSubreddit(postInfo[0]);
                int parentsShown = (postInfo[3]==null) ? -1 : Integer.valueOf(postInfo[3]);
                List<Comment> comments = commentsRetrieval.ofSubmission(linkedpost, postInfo[2], parentsShown, syncOptions.getSyncCommentDepth(),
                        syncOptions.getSyncCommentCount(), syncOptions.getSyncCommentSort());
                linkedpost.setSyncedComments(comments);
            }
        }

        if(linkedpost!=null) {
            writePostToFile(linkedpost, filename + "-" + linkedpost.getIdentifier());
        }
    }

    private void downloadPostArticle(Submission post, String filename) {
        try {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float headerSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, metrics);
            float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, metrics);

            //WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            //Display display = wm.getDefaultDisplay();
            //int screenWidth = display.getWidth();
            int screenWidth = 720;

            HtmlFetcher fetcher = new HtmlFetcher();
            // set cache. e.g. take the map implementation from google collections:
            // fetcher.setCache(new MapMaker().concurrencyLevel(20).maximumSize(count).
            //    expireAfterWrite(minutes, TimeUnit.MINUTES).makeMap();
            JResult res = fetcher.fetchAndExtract(post.getURL(), 10000, true);

            List<String> textList = res.getTextList();
            if(textList.size() == 0) {
                Log.d(TAG, "No paragraph text found for " + post.getIdentifier());
                return;
            }

            String title = "<h1 style=\"font-size:" + headerSize + "px;\"> " + StringEscapeUtils.escapeHtml(res.getTitle()) + "</h1>";

            String image = "";
            if(res.getImageUrl().length()>0) {
                String imageFilename = filename + "-" + post.getIdentifier() + "-article_image";
                GeneralUtils.downloadMediaToFile(res.getImageUrl(), new File(getFilesDir(), imageFilename));
                image = "<img src=\"" + imageFilename + "\" width=\"" + screenWidth + "\"/>";
            }

            String text = "";
            for(String paragraph : textList) {
                paragraph = StringEscapeUtils.escapeHtml(paragraph);
                text = text.concat("<p style=\"font-size:" + textSize + "px;\">" + paragraph + "</p>");
            }

            String result = "<html><head></head><body><div style=\"padding-left: 10px; padding-right: 10px;\">"
                    + title + "\n" + image + "\n" + text + "</div></body></html>";

            File file = getPreferredStorageFile(filename + post.getIdentifier() + LOCAL_ARTICLE_SUFFIX);
            //GeneralUtils.writeObjectToFile(result, new File(getFilesDir(), filename + post.getIdentifier() + LOCAL_ARTICLE_SUFFIX));
            GeneralUtils.writeObjectToFile(result, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImage(Submission post, String filename) {
        String url = post.getURL();
        String domain = post.getDomain();
        if(domain.contains("imgur.com") || domain.contains("gfycat.com") || domain.equals("i.reddituploads.com") ||
                url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif")) { // TODO: 6/26/2016 probably remove this check
            File parentFolder;
            if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable()) {
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

            if (domain.contains("gfycat.com")) {
                try {
                    url = GeneralUtils.getGfycatMobileUrl(url);
                    downloadPostImageToFile(url, folderPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(domain.equals("i.reddituploads.com")) {
                downloadPostImageToFile(url, folderPath, LinkHandler.getReddituploadsFilename(url));
            }
            else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                downloadPostImageToFile(url, folderPath);
            }
            else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                if (domain.contains("imgur.com")) {
                    url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                    //url = url.replace(".gif", ".mp4");
                }
                downloadPostImageToFile(url, folderPath);
            }
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
                    downloadPostImageToFile(link, folderPath);
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
                        downloadPostImageToFile(link, folderPath);
                    }
                }
            }
        }
    }

    private void downloadAlbumImages(ImgurItem item, String filename, String folderPath) {
        int i = 0;
        for(ImgurImage img : item.getImages()) {
            if(i >= MyApplication.syncAlbumImgCount) break;
            downloadPostImageToFile(img.getLink(), folderPath);
            String imgId = LinkHandler.getImgurImgId(img.getLink());
            if(MyApplication.syncAlbumImgCount > 1) {
                try {
                    GeneralUtils.downloadMediaToFile("http://i.imgur.com/" + imgId + "s.jpg", new File(getFilesDir().getAbsolutePath(), filename + "-" + imgId + "-thumb.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
        int indexExlusive = (MyApplication.syncAlbumImgCount > item.getImages().size()) ? item.getImages().size() : MyApplication.syncAlbumImgCount;
        item.setImages(new ArrayList<ImgurImage>(item.getImages().subList(0, indexExlusive)));
        for(ImgurImage img : item.getImages()) {
            img.setLink("file:" + folderPath + "/" + img.getLink().replaceAll("https?://", "").replace("/", "(s)"));
        }
        saveAlbumInfoToFile(item, filename + "-" + item.getId() + "-albumInfo");
    }

    private void saveAlbumInfoToFile(ImgurItem item, final String filename) {
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(item);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImageToFile(String url, String dir) {
        try {
            final String imgFilename = url.replaceAll("https?://", "").replace("/", "(s)");
            final File file = new File(dir, imgFilename);
            Log.d("DownloaderService", "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadMediaToFile(url, file);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImageToFile(String url, String dir, String imgFilename) {
        try {
            imgFilename = imgFilename.replaceAll("https?://", "").replace("/", "(s)");
            final File file = new File(dir, imgFilename);
            Log.d("DownloaderService", "Downloading " + url + " to " + file.getAbsolutePath());
            GeneralUtils.downloadMediaToFile(url, file);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
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

    private void deletePreviousImages(final String filename) {
        GeneralUtils.clearSyncedImages(this, filename);
    }

    private void deletePreviousComments(final String subreddit) {
        GeneralUtils.clearSyncedPostsAndComments(this, subreddit);
    }

}
