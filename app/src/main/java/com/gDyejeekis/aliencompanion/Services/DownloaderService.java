package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
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
        if(MyApplication.currentAccount == null) {
            MyApplication.currentAccount = MyApplication.getCurrentAccount(this);
        }
        //Log.d("SYNC_DEBUG", "DownloaderService onHandleIntent...");

        //List<String> subreddits = i.getStringArrayListExtra("subreddits");
        SyncProfile profile = (SyncProfile) i.getSerializableExtra("profile");
        if(profile != null) {
            for(String subreddit : profile.getSubreddits()) {
                progress = 0;
                String filename;
                boolean isMulti = false;
                if(subreddit.endsWith(" (multi)")) {
                    filename = MyApplication.MULTIREDDIT_FILE_PREFIX + subreddit.toLowerCase();
                    isMulti = true;
                }
                else {
                    filename = subreddit.toLowerCase();
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                startForeground(FOREGROUND_ID, buildForegroundNotification(builder, filename));

                syncSubreddit(filename, builder, subreddit, SubmissionSort.HOT, null, isMulti);
            }

            if(i.getBooleanExtra("reschedule", false)) {
                profile.schedulePendingIntents(this);
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
                deletePreviousImages(filename);
                writePostsToFile(posts, filename);
                for (RedditItem post : posts) {
                    increaseProgress(builder);
                    Submission submission = (Submission) post;
                    if(MyApplication.syncThumbnails) {
                        downloadPostThumbnail(submission, filename + submission.getIdentifier() + LOCAL_THUMNAIL_SUFFIX);
                    }
                    List<Comment> comments = cmntsRetrieval.ofSubmission(submission, null, -1, MyApplication.syncCommentDepth, MyApplication.syncCommentCount, MyApplication.syncCommentSort);
                    submission.setSyncedComments(comments);
                    if(MyApplication.syncImages) {
                        downloadPostImage(submission, filename);
                    }
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

    private void downloadPostImage(Submission post, String filename) {
        String url = post.getURL();
        String domain = post.getDomain();
        if(domain.contains("imgur.com") || domain.contains("gfycat.com") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif")) {
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
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
            else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                downloadPostImageToFile(url, folderPath);
            }
            else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                if (domain.contains("imgur.com")) {
                    url = url.replace(".gifv", ".mp4");
                    url = url.replace(".gif", ".mp4");
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
                    downloadPostImageToFile(image.getLink(), folderPath);
                }
                else if(item instanceof ImgurAlbum) {
                    ImgurAlbum album = (ImgurAlbum) item;
                }
                else if(item instanceof ImgurGallery) {
                    ImgurGallery gallery = (ImgurGallery) item;
                    if(gallery.isAlbum()) {

                    }
                    else {
                        downloadPostImageToFile(gallery.getLink(), folderPath);
                    }
                }
            }
        }
    }

    private void downloadPostImageToFile(String url, String dir) {
        try {
            final String imgFilename = url.replace("/", "(s)").replaceAll("https?:", "");
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
