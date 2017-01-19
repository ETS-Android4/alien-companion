package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

/**
 * Created by George on 1/18/2017.
 */

public class VideoFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String TAG = "VideoFragment";

    public static VideoFragment newInstance(String url) {
        VideoFragment fragment = new VideoFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        return fragment;
    }

    private ImageActivity activity;

    private String url;

    private VideoView videoView;

    private MediaController mediaController;

    private Button buttonRetry;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        videoView = (VideoView) view.findViewById(R.id.videoView);
        videoView.setZOrderOnTop(true);
        videoView.setOnPreparedListener(this);
        videoView.setOnErrorListener(this);

        buttonRetry = (Button) view.findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setVisibility(View.VISIBLE);
                activity.setMainProgressBarVisible(true);
                buttonRetry.setVisibility(View.GONE);
                loadVideo();
            }
        });

        loadVideo();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_save) {
            // TODO: 1/18/2017
            return true;
        }
        else if(item.getItemId() == R.id.action_share) {
            shareVideo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        activity.setMainProgressBarVisible(false);
        videoView.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                videoView.start();
            }
        }, 10);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        loadError();
        return false;
    }

    private void loadVideo() {
        Log.d(TAG, "Loading video from " + url);
        try {
            mediaController = new MediaController(activity);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse(url);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
        } catch (Exception e) {
            loadError();
            e.printStackTrace();
        }
    }

    private void loadError() {
        ToastUtils.displayShortToast(activity, "Error loading video");
        activity.setMainProgressBarVisible(false);
        buttonRetry.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
    }

    private void shareVideo() {
        String label = "Share video to..";

        GeneralUtils.shareUrl(activity, label, url);
    }

}
