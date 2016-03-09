package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.BitmapTransform;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by sound on 3/8/2016.
 */
public class ImageFragment extends Fragment {

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;

    private static final int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

    private ImageActivity activity;

    private String url;

    private ImageView photoView;

    private PhotoViewAttacher attacher;

    private Button buttonRetry;

    public static ImageFragment newInstance(String url) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        photoView = (ImageView) view.findViewById(R.id.photoview);
        buttonRetry = (Button) view.findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImage();
                attacher.update();
            }
        });

        loadImage();

        attacher = new PhotoViewAttacher(photoView);
        if(true) {
            attacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    activity.finish();
                }
            });
        }

        return view;
    }

    private void loadImage() {
        photoView.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);
        activity.setMainProgressBarVisible(true);

        Picasso.with(getActivity()).load(url).transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT)).skipMemoryCache().resize(size, size).centerInside().into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                activity.setMainProgressBarVisible(false);
                photoView.setVisibility(View.VISIBLE);
                buttonRetry.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                activity.setMainProgressBarVisible(false);
                photoView.setVisibility(View.GONE);
                buttonRetry.setVisibility(View.VISIBLE);
            }
        });
    }

}
