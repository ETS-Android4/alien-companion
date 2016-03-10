package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 3/8/2016.
 */
public class AlbumFragment extends Fragment {

    private ImageActivity activity;

    private String[] urls;

    public static ImageFragment newInstance(String[] urls) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArray("url", urls);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ImageActivity) getActivity();
        urls = getArguments().getStringArray("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        return view;
    }

}
