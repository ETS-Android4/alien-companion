package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.AsyncTasks.MediaDownloadTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.MediaLoadTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = "GifFragment";

    private ImageActivity activity;

    private String url;

    private SurfaceView videoView;

    private SurfaceHolder sHolder;

    private MediaPlayer mPlayer;

    private GifImageView gifView;

    private GifDrawable gifDrawable;

    private Button buttonRetry;

    private boolean isGif;

    private boolean autoplay;

    private boolean gifSaved;

    private MediaLoadTask loadGifTask;

    private RelativeLayout gifParent;

    public static GifFragment newInstance(String url, boolean autoplay) {
        GifFragment fragment = new GifFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putBoolean("autoplay", autoplay);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url", "null");
        autoplay = getArguments().getBoolean("autoplay");

        if(url.endsWith(".gif")) {
            if(url.contains("imgur.com")) {
                url = url.replace(".gif", ".mp4");
                isGif = false;
            }
            else {
                isGif = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif, container, false);

        if(!isGif) {
            gifParent = (RelativeLayout) view.findViewById(R.id.layout_gif_parent);
            videoView = (SurfaceView) view.findViewById(R.id.videoView);
            videoView.setZOrderOnTop(true);
        }
        else {
            gifView = (GifImageView) view.findViewById(R.id.gifView);
            gifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (MyApplication.dismissGifOnTap) {
                        activity.finish();
                    }
                    else {
                        if (gifDrawable.isPlaying()) {
                            gifDrawable.stop();
                        } else {
                            gifDrawable.start();
                        }
                    }
                }
            });
        }
        buttonRetry = (Button) view.findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUrl();
            }
        });

        loadUrl();

        return view;
    }

    private void loadUrl() {
        if(isGif) {
            loadGif();
        }
        else {
            if(autoplay) {
                loadVideo();
            }
        }
    }

    private void loadVideo() {
        activity.setMainProgressBarVisible(true);
        buttonRetry.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        sHolder = videoView.getHolder();
        sHolder.addCallback(this);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        activity.setMainProgressBarVisible(false);
        handleAspectRatio();
        mPlayer.setLooping(true);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.dismissGifOnTap) {
                    activity.finish();
                }
                else {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                    } else {
                        mPlayer.start();
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPlayer.start();
            }
        }, 10);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        activity.setMainProgressBarVisible(false);
        buttonRetry.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        loadVideoSource();
    }

    private void loadVideoSource() {
        Log.d(TAG, "Loading video from " + url);
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(url);
            mPlayer.setDisplay(sHolder);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
        } catch (IOException e) {
            e.printStackTrace();
            buttonRetry.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
        }
    }

    private void handleAspectRatio() {
        int surfaceView_Width = gifParent.getWidth();
        int surfaceView_Height = gifParent.getHeight();

        float video_Width = mPlayer.getVideoWidth();
        float video_Height = mPlayer.getVideoHeight();

        float ratio_width = surfaceView_Width/video_Width;
        float ratio_height = surfaceView_Height/video_Height;
        float aspectratio = video_Width/video_Height;

        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();

        if (ratio_width > ratio_height){
            layoutParams.width = (int) (surfaceView_Height * aspectratio);
            layoutParams.height = surfaceView_Height;
        }
        else {
            layoutParams.width = surfaceView_Width;
            layoutParams.height = (int) (surfaceView_Width / aspectratio);
        }

        videoView.setLayoutParams(layoutParams);

        //Log.d(TAG, "-----------------------------------------------");
        //Log.d(TAG, "SurfaceView width: " + surfaceView_Width);
        //Log.d(TAG, "SurfaceView height: " + surfaceView_Height);
        //Log.d(TAG, "MediaPlayer width: " + video_Width);
        //Log.d(TAG, "MediaPlayer height: " + video_Height);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!isGif) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleAspectRatio();
                }
            }, 600);
        }
    }

    @Override
    public void onDestroy() {
        //Log.d(TAG, "gifFragment onDestroy");
        if(loadGifTask!=null) {
            loadGifTask.cancelOperation();
        }
        super.onDestroy();
    }

    private void loadGif() {
        Log.d(TAG, "Loading gif from " + url);
        gifView.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);
        activity.setMainProgressBarVisible(true);

        if(activity.loadedFromLocal()) {
            activity.setMainProgressBarVisible(false);
            try {
                gifDrawable = new GifDrawable(url);
                gifView.setImageDrawable(gifDrawable);
                gifView.setVisibility(View.VISIBLE);
                buttonRetry.setVisibility(View.GONE);
            } catch (IOException e) {
                gifView.setVisibility(View.GONE);
                buttonRetry.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }
        }
        else {
            loadGifTask = new MediaLoadTask(activity.getCacheDir()) {

                @Override
                protected void onPostExecute(String gifPath) {
                    activity.setMainProgressBarVisible(false);
                    if(gifPath!=null) {
                        try {
                            gifDrawable = new GifDrawable(gifPath);
                            gifView.setImageDrawable(gifDrawable);
                            gifView.setVisibility(View.VISIBLE);
                            buttonRetry.setVisibility(View.GONE);
                        } catch (IOException e) {
                            gifView.setVisibility(View.GONE);
                            buttonRetry.setVisibility(View.VISIBLE);
                            e.printStackTrace();
                        }
                    }
                    else {
                        gifView.setVisibility(View.GONE);
                        buttonRetry.setVisibility(View.VISIBLE);
                        ToastUtils.displayShortToast(activity, "Error loading gif");
                    }
                }
            };
            loadGifTask.execute(url);
            //new GifDataDownloader() {
            //    @Override
            //    protected void onPostExecute(final byte[] bytes) {
            //        activity.setMainProgressBarVisible(false);
            //        try {
            //            if (bytes == null) throw new Exception();
            //            gifDrawable = new GifDrawable(bytes);
            //            gifView.setImageDrawable(gifDrawable);
            //            gifView.setVisibility(View.VISIBLE);
            //            buttonRetry.setVisibility(View.GONE);
            //        } catch (Exception e) {
            //            buttonRetry.setVisibility(View.VISIBLE);
            //            gifView.setVisibility(View.GONE);
            //            e.printStackTrace();
            //        }
            //    }
            //}.execute(url);
        }
    }

    public void resumePlayback() {
        if(isGif) {
            if(gifDrawable != null) {
                gifDrawable.start();
            }
        }
        else {
            if(sHolder == null) {
                loadVideo();
            }
            else if(mPlayer != null) {
                mPlayer.start();
            }
        }
    }

    public void pausePlayback() {
        if(isGif) {
            if(gifDrawable != null && gifDrawable.isPlaying()) {
                gifDrawable.pause();
            }
        }
        else {
            if(mPlayer !=null && mPlayer.isPlaying()) {
                mPlayer.pause();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        saveGif();
                    }
                    else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 12);
                    }
                }
                else {
                    saveGif();
                }
                return true;
            case R.id.action_share:
                shareGif();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 12) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                saveGif();
            }
            else {
                ToastUtils.displayShortToast(activity, "Failed to save GIF to photos (permission denied)");
            }
        }
    }

    private void saveGif() {
        ToastUtils.displayShortToast(activity, "Saving to photos..");
        Log.d(TAG, "Saving " + url + " to pictures directory");
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        final File appFolder = new File(dir + "/AlienCompanion");

        if(!appFolder.exists()) {
            appFolder.mkdir();
        }

        final int id = UUID.randomUUID().hashCode();
        final String filename = url.replaceAll("https?://", "").replace("/", "(s)");
        final File file = new File(appFolder.getAbsolutePath(), filename);

        showSavingGifNotif(id);

        new MediaDownloadTask(url, file, activity.getCacheDir()) {
            @Override protected void onPostExecute(Boolean success) {
                Log.d(TAG, "Gif downloaded to file");
                gifSaved = success;
                Uri contentUri = null;
                if(gifSaved) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    activity.sendBroadcast(mediaScanIntent);
                }
                showGifSavedNotification(id, gifSaved, contentUri);
            }
        }.execute();

    }

    private void showSavingGifNotif(int id) {
        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Saving gif..")
                .setContentText(url)
                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                .setProgress(1, 0, true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void showGifSavedNotification(int id, boolean success, Uri uri) {
        PendingIntent pIntent = null;
        if(success) {
            String type = (isGif) ? "image/*" : "video/*";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, type);
            pIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        }

        String title = (success) ? "Gif saved" : "Failed to save gif";
        int smallIcon = (success) ? R.mipmap.ic_photo_white_24dp : android.R.drawable.stat_notify_error;
        Notification notif = new Notification.Builder(activity)
                .setContentTitle(title)
                .setContentText(url)
                //.setSubText(url)
                .setSmallIcon(smallIcon)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void shareGif() {
        String label = "Share gif to..";
        String link;
        if(activity.loadedFromLocal()) {
            link = "http://" + url.substring(url.lastIndexOf("/")+1).replace("(s)", "/");
        }
        else {
            link = url;
        }
        GeneralUtils.shareUrl(activity, label, link);
    }

}
