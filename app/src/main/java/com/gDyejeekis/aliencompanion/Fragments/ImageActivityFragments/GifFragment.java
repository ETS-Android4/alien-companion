package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GifDataDownloader;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment {

    public static final String TAG = "GifFragment";

    private ImageActivity activity;

    private String url;

    private VideoView videoView;

    private GifImageView gifView;

    private GifDrawable gifDrawable;

    private Button buttonRetry;

    private boolean isGif;

    private boolean gifSaved;

    public static GifFragment newInstance(String url) {
        GifFragment fragment = new GifFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url", "null");

        isGif = url.endsWith(".gif");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif, container, false);

        if(!isGif) {
            videoView = (VideoView) view.findViewById(R.id.videoView);
            videoView.setZOrderOnTop(true);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    activity.setMainProgressBarVisible(false);
                    mediaPlayer.setLooping(true);
                    if(MyApplication.dismissGifOnTap) {
                        videoView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {

                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                        activity.finish();
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                }
            });
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    activity.setMainProgressBarVisible(false);
                    videoView.setVisibility(View.GONE);
                    buttonRetry.setVisibility(View.VISIBLE);
                    return false;
                }
            });
        }
        else {
            gifView = (GifImageView) view.findViewById(R.id.gifView);
            gifView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (MyApplication.dismissGifOnTap) {
                        activity.finish();
                    } else {
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
        if(isGif) loadGif();
        else loadVideo();
    }

    private void loadVideo() {
        activity.setMainProgressBarVisible(true);
        buttonRetry.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        if(!MyApplication.dismissGifOnTap) {
            videoView.setMediaController(new MediaController(activity));
        }
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.start();
    }

    private void loadGif() {
        gifView.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);
        activity.setMainProgressBarVisible(true);

        new GifDataDownloader() {
            @Override protected void onPostExecute(final byte[] bytes) {
                activity.setMainProgressBarVisible(false);
                try {
                    if(bytes==null) throw new Exception();
                    gifDrawable = new GifDrawable(bytes);
                    gifView.setImageDrawable(gifDrawable);
                    gifView.setVisibility(View.VISIBLE);
                    buttonRetry.setVisibility(View.GONE);
                } catch (Exception e) {
                    buttonRetry.setVisibility(View.VISIBLE);
                    gifView.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }.execute(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveGif();
                return true;
            case R.id.action_share:
                shareGif();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

        String filename = url.replace("/", "(s)"); //url.replaceAll("https?://", "").replace("/", "-");
        final File file = new File(appFolder.getAbsolutePath(), filename);
        if(isGif) {
            new GifDataDownloader() {
                @Override protected void onPostExecute(final byte[] bytes) {
                    try {
                        FileOutputStream os = new FileOutputStream(file);
                        os.write(bytes);
                        os.flush();
                        os.close();
                        gifSaved = true;
                    } catch (Exception e) {
                        gifSaved = false;
                        e.printStackTrace();
                    }
                    onPostDownload(file);
                }
            }.execute(url);
        }
        else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "mp4 downloader starting");
                    try {
                        //Open a connection to that URL.
                        URLConnection ucon = new URL(url).openConnection();

                        //this timeout affects how long it takes for the app to realize there's a connection problem
                        ucon.setReadTimeout(5000);
                        ucon.setConnectTimeout(30000);


                        //Define InputStreams to read from the URLConnection.
                        // uses 3KB download buffer
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
                        FileOutputStream outStream = new FileOutputStream(file);
                        byte[] buff = new byte[5 * 1024];

                        //Read bytes (and store them) until there is nothing more to read(-1)
                        int len;
                        while ((len = inStream.read(buff)) != -1) {
                            outStream.write(buff, 0, len);
                            Log.d(TAG, "writing buffer to file..");
                        }

                        //clean up
                        outStream.flush();
                        outStream.close();
                        inStream.close();
                        gifSaved = true;
                    } catch (Exception e) {
                        gifSaved = false;
                        e.printStackTrace();
                    }

                    onPostDownload(file);
                }
            });
        }
    }

    private void onPostDownload(File file) {
        if(gifSaved) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            activity.sendBroadcast(mediaScanIntent);
        }
        showGifSavedNotification(gifSaved);
    }

    private void showGifSavedNotification(boolean success) {
        String title = (success) ? "Gif saved" : "Failed to save gif";
        int smallIcon = (success) ? R.mipmap.ic_photo_white_24dp : android.R.drawable.stat_notify_error;
        Notification notif = new Notification.Builder(activity)
                .setContentTitle(title)
                .setContentText(url)
                //.setSubText(url)
                .setSmallIcon(smallIcon)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(UUID.randomUUID().hashCode(), notif);
    }

    private void shareGif() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        activity.startActivity(Intent.createChooser(intent, "Share gif to.."));
    }

}
