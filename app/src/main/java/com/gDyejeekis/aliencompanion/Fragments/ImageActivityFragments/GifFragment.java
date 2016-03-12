package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.felipecsl.gifimageview.library.GifImageView;
import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GifDataDownloader;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment {

    private ImageActivity activity;

    private String url;

    private VideoView videoView;

    private GifImageView gifView;

    private Button buttonRetry;

    private boolean isGif;

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

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url", "null");

        isGif = url.endsWith(".gif");
    }

    //@Override
    //public void onPause() {
    //    super.onPause();
    //    if(isGif) gifView.stopAnimation();
    //    else videoView.suspend();
    //}
//
    //@Override
    //public void onResume() {
    //    super.onResume();
    //    if(isGif) gifView.startAnimation();
    //    else videoView.resume();
    //}

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
                    if (gifView.isAnimating()) {
                        gifView.stopAnimation();
                    } else {
                        gifView.startAnimation();
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
        videoView.setMediaController(new MediaController(activity));
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
                    gifView.setBytes(bytes);
                    gifView.setVisibility(View.VISIBLE);
                    buttonRetry.setVisibility(View.GONE);
                    gifView.startAnimation();
                    //Log.d("GifFragment", "GIF width is " + gifView.getGifWidth());
                    //Log.d("GifFragment", "GIF height is " + gifView.getGifHeight());
                } catch (Exception e) {
                    buttonRetry.setVisibility(View.VISIBLE);
                    gifView.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }.execute(url);
    }

}
