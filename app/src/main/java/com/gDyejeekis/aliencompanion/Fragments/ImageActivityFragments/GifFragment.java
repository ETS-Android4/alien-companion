package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GifDataDownloader;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment {

    private ImageActivity activity;

    private String url;

    private VideoView videoView;

    private GifImageView gifView;

    private GifDrawable gifDrawable;

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
                    if(true) { // TODO: 3/13/2016 flag for dismiss gif on single tap here
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
                    if (true) { // TODO: 3/13/2016 flag for dismiss gif on single tap here
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
        if(!true) { // TODO: 3/13/2016 flag for dismiss gif on single tap here
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

    }

    private void shareGif() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        activity.startActivity(Intent.createChooser(intent, "Share gif to.."));
    }

}
