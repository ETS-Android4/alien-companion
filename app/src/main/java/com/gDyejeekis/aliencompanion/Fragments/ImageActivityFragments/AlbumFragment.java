package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;

/**
 * Created by sound on 3/8/2016.
 */
public class AlbumFragment extends Fragment {

    private ImageActivity activity;

    private ArrayList<String> urls;

    public static ImageFragment newInstance(ArrayList<String> urls) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("urls", urls);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ImageActivity) getActivity();
        urls = getArguments().getStringArrayList("urls");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        return view;
    }

}
