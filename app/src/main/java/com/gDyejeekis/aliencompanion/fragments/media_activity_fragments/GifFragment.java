package com.gDyejeekis.aliencompanion.fragments.media_activity_fragments;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import com.gDyejeekis.aliencompanion.activities.MediaActivity;
import com.gDyejeekis.aliencompanion.asynctask.MediaLoadTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = "GifFragment";

    //private static final boolean PRE_CACHE_VIDEO_GIFS = false;

    private MediaActivity activity;

    public String getUrl() {
        return url;
    }

    private String url;

    private SurfaceView videoView;

    private SurfaceHolder sHolder;

    private MediaPlayer mPlayer;

    private GifImageView gifView;

    private GifDrawable gifDrawable;

    private Button buttonRetry;

    private boolean isGif;

    private boolean autoplay;

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

        activity = (MediaActivity) getActivity();
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
            gifParent = view.findViewById(R.id.layout_gif_parent);
            videoView = view.findViewById(R.id.videoView);
            videoView.setZOrderOnTop(true);
        }
        else {
            gifView = view.findViewById(R.id.gifView);
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
        buttonRetry = view.findViewById(R.id.button_retry);
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
        if (isGif) {
            loadGif();
        } else {
            if (autoplay) {
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

        //if (!activity.loadedFromSynced() && PRE_CACHE_VIDEO_GIFS) {
        //    loadGifTask = new MediaLoadTask(activity.getCacheDir()) {
        //        @Override
        //        protected void onPostExecute(String videoPath) {
        //            activity.setMainProgressBarVisible(false);
        //            if(videoPath!=null) {
        //                url = videoPath;
        //                loadVideoSource();
        //            }
        //            else {
        //                videoView.setVisibility(View.GONE);
        //                buttonRetry.setVisibility(View.VISIBLE);
        //                ToastUtils.showToast(activity, "Error loading gif");
        //                CleaningUtils.clearMediaFromCache(activity.getCacheDir(), url); // this shouldn't throw any exceptions
        //            }
        //        }
        //    };
        //    loadGifTask.execute(url);
        //} else {
        //    sHolder.addCallback(this);
        //}
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        loadVideoSource();
    }

    private void loadVideoSource() {
        Log.d(TAG, "Loading video from " + url);
        try {
            mPlayer = new MediaPlayer();
            String source = activity.loadedFromSynced() ? url :
                    MyApplication.proxyCacheServer.getProxyUrl(url);
            mPlayer.setDataSource(source);
            mPlayer.setDisplay(sHolder);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            buttonRetry.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        activity.setMainProgressBarVisible(false);
        try {
            handleAspectRatio();
            mPlayer.setLooping(true);
        } catch (Exception e) {}
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.dismissGifOnTap) {
                    activity.finish();
                } else {
                    try {
                        if (mPlayer.isPlaying()) {
                            mPlayer.pause();
                        } else {
                            safeMediaPlayerStart();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                safeMediaPlayerStart();
            }
        }, 10);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        activity.setMainProgressBarVisible(false);
        buttonRetry.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        return true;
    }

    private void handleAspectRatio() {
        int surfaceView_Width = gifParent.getWidth();
        int surfaceView_Height = gifParent.getHeight();

        float video_Width = mPlayer.getVideoWidth();
        float video_Height = mPlayer.getVideoHeight();

        float ratio_width = surfaceView_Width / video_Width;
        float ratio_height = surfaceView_Height / video_Height;
        float aspectratio = video_Width / video_Height;

        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();

        if (ratio_width > ratio_height) {
            layoutParams.width = (int) (surfaceView_Height * aspectratio);
            layoutParams.height = surfaceView_Height;
        } else {
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
                    try {
                        handleAspectRatio();
                    } catch (Exception e) {}
                }
            }, 600);
        }
    }

    //@Override
    //public void onPause() {
    //    super.onPause();
    //}

    @Override
    public void onStop() {
        super.onStop();
        if(mPlayer != null) {
            mPlayer.release(); // should cause to do less work in mPlayer.finalize()
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

        if(activity.loadedFromSynced()) {
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
                        ToastUtils.showToast(activity, "Error loading gif");
                        CleaningUtils.clearMediaFromCache(activity.getCacheDir(), url); // this shouldn't throw any exceptions
                    }
                }
            };
            loadGifTask.execute(url);
        }
    }

    private void safeMediaPlayerStart() {
        try {
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            activity.setMainProgressBarVisible(false);
            videoView.setVisibility(View.GONE);
            buttonRetry.setVisibility(View.VISIBLE);
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
                safeMediaPlayerStart();
            }
        }
    }

    public void resumePlaybackSafe() {
        try {
            resumePlayback();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void pausePlaybackSafe() {
        try {
            pausePlayback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                activity.saveMedia(url);
                return true;
            case R.id.action_share:
                activity.shareMedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
