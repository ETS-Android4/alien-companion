package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
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
import com.gDyejeekis.aliencompanion.Utils.SyncPausedException;
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

    public static final String LOCA_POST_LIST_SUFFIX = "-posts";

    public static final String LOCAL_THUMNAIL_SUFFIX = "thumb";

    public static final String LOCAL_ARTICLE_SUFFIX = "-article.html";

    private int MAX_PROGRESS;

    private int progress;

    //public static NotificationCompat.Builder notifBuilder;

    public static boolean manuallyPaused = false;

    public static boolean manuallyCancelled = false;

    private static final android.support.v4.app.NotificationCompat.Action pauseAction = new android.support.v4.app.NotificationCompat.Action(
            R.mipmap.ic_pause_white_48dp, "Pause", null); // TODO: 7/8/2016

    private static final android.support.v4.app.NotificationCompat.Action resumeAction = new android.support.v4.app.NotificationCompat.Action(
            R.mipmap.ic_resume_white_48dp, "Resume", null); // TODO: 7/8/2016

    private static final android.support.v4.app.NotificationCompat.Action cancelAction = new android.support.v4.app.NotificationCompat.Action(
            R.mipmap.ic_close_white_48dp, "Cancel", null); // TODO: 7/8/2016

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
    public void onHandleIntent(Intent i) {
        if(MyApplication.currentAccount == null) {
            MyApplication.currentAccount = MyApplication.getCurrentAccount(this);
        }
        //Log.d("SYNC_DEBUG", "DownloaderService onHandleIntent...");

        //List<String> subreddits = i.getStringArrayListExtra("subreddits");
        SyncProfile profile = (SyncProfile) i.getSerializableExtra("profile");
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
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    startForeground(FOREGROUND_ID, buildForegroundNotification(builder, filename));

                    syncSubreddit(filename, builder, subredditName, SubmissionSort.HOT, null, isMulti, syncOptions);
                }
            }

            if(i.getBooleanExtra("reschedule", false)) {
                profile.schedulePendingIntents(this);
            }
        }
        else {
            MAX_PROGRESS = MyApplication.syncPostCount + 1;
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

            syncSubreddit(filename, builder, subreddit, submissionSort, timeSpan, isMulti, new SyncProfileOptions());
        }
    }

    private void syncSubreddit(String filename, NotificationCompat.Builder builder, String subreddit, SubmissionSort submissionSort, TimeSpan timeSpan, boolean isMulti, SyncProfileOptions syncOptions) {
        try {
            checkPausedOrCancelled();
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
                    syncPost(builder, submission, filename, syncOptions);
                }
            }

        } catch (RetrievalFailedException | RedditError | SyncPausedException e) {
            //e.printStackTrace();
            pauseSync(builder);
            syncSubreddit(filename, builder, subreddit, submissionSort, timeSpan, isMulti, syncOptions);
        }
    }

    private void syncPost(NotificationCompat.Builder builder, Submission submission, String filename, SyncProfileOptions syncOptions) {
        try {
            checkPausedOrCancelled();
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
            increaseProgress(builder, filename);
        } catch (RetrievalFailedException | RedditError | SyncPausedException e) {
            //e.printStackTrace();
            pauseSync(builder);
            syncPost(builder, submission, filename, syncOptions);
        }
    }

    private void checkPausedOrCancelled() throws SyncPausedException {
        if(manuallyPaused) {
            throw new SyncPausedException();
        }
        else if(manuallyCancelled) {
            manuallyCancelled = false;
            stopSelf();
        }
    }

    private void pauseSync(NotificationCompat.Builder builder) {
        String pauseReason;
        long waitTime;
        int small_icon;
        if(manuallyPaused) {
            pauseReason = "Sync paused";
            waitTime = 0;
            small_icon = android.R.drawable.ic_media_pause;
            builder.mActions.set(0, resumeAction);
        }
        else if(!GeneralUtils.isNetworkAvailable(this)) {
            pauseReason = "Sync paused (network unavailable). Retrying..";
            waitTime = 0;
            small_icon = android.R.drawable.stat_notify_error;
        }
        else {
            pauseReason = "Sync paused (error connecting to reddit). Retrying..";
            waitTime = 2000;
            small_icon = android.R.drawable.stat_notify_error;
        }

        builder.setContentText(pauseReason).setSmallIcon(small_icon);
        notificationManager.notify(FOREGROUND_ID, builder.build());

        SystemClock.sleep(waitTime);
    }

    private void manualSyncPause(NotificationCompat.Builder builder) {
        manuallyPaused = true;
        builder.setContentText("Pausing..");
        builder.mActions.set(0, resumeAction);
        notificationManager.notify(FOREGROUND_ID, builder.build());
    }

    private void manualSyncResume(NotificationCompat.Builder builder) {
        manuallyPaused = false;
        builder.setContentText("Resuming..");
        builder.mActions.set(0, pauseAction);
        notificationManager.notify(FOREGROUND_ID, builder.build());
    }

    private Notification buildForegroundNotification(NotificationCompat.Builder b, String filename) {
        b.setOngoing(true);
        b.setContentTitle("Alien Companion")
                .setContentText("Syncing " + filename +"...")
                .setSmallIcon(android.R.drawable.stat_sys_download).setTicker("Syncing posts...")
                .setProgress(MAX_PROGRESS, progress, false)
                .addAction(pauseAction)
                .addAction(cancelAction);
        return(b.build());
    }

    private void increaseProgress(NotificationCompat.Builder b, String filename) {
        progress++;
        Log.d(TAG, progress + "/" + MAX_PROGRESS + " done");
        b.setContentText("Syncing " + filename + "...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(MAX_PROGRESS, progress, false);
        notificationManager.notify(FOREGROUND_ID, b.build());
    }

    private void writePostsToFile(List<RedditItem> posts, String filename) {
        try {
            Log.d(TAG, "writing posts to " + filename);
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
            Log.d(TAG, "writing comments to " + filename);
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

            String title = "<h1 style=\"font-size:" + headerSize + "px;\"> " + StringEscapeUtils.escapeHtml(res.getTitle()) + "</h1>";

            String image = "";
            if(res.getImageUrl().length()>0) {
                String imageFilename = filename + "-" + post.getIdentifier() + "-article_image";
                GeneralUtils.downloadMediaToFile(res.getImageUrl(), new File(getFilesDir(), imageFilename));
                image = "<img src=\"" + imageFilename + "\" width=\"" + screenWidth + "\"/>";
            }

            List<String> textList = res.getTextList();
            String text = "";
            for(String paragraph : textList) {
                paragraph = StringEscapeUtils.escapeHtml(paragraph);
                text = text.concat("<p style=\"font-size:" + textSize + "px;\">" + paragraph + "</p>");
            }

            String result = "<html><head></head><body><div style=\"padding-left: 10px; padding-right: 10px;\">"
                    + title + "\n" + image + "\n" + text + "</div></body></html>";

            GeneralUtils.writeObjectToFile(result, new File(getFilesDir(), filename + post.getIdentifier() + LOCAL_ARTICLE_SUFFIX));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPostImage(Submission post, String filename) {
        String url = post.getURL();
        String domain = post.getDomain();
        if(domain.contains("imgur.com") || domain.contains("gfycat.com") || domain.equals("i.reddituploads.com") ||
                url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif")) { // TODO: 6/26/2016 probably remove this check
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            final File appFolder = new File(dir + "/AlienCompanion");
            if(!appFolder.exists()) {
                appFolder.mkdir();
            }

            final File folder = new File(dir + "/AlienCompanion/" + filename);
            if(!folder.exists()) {
                folder.mkdir();
            }

            final String folderPath = folder.getAbsolutePath();

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

    private void deletePreviousImages(final String filename) {
        GeneralUtils.clearSyncedImages(this, filename);
    }

    private void deletePreviousComments(final String subreddit) {
        GeneralUtils.clearSyncedPostsAndComments(this, subreddit);
    }

}
