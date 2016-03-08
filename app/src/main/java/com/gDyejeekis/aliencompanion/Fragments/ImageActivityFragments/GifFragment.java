package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 3/8/2016.
 */
public class GifFragment extends Fragment {

    private String url;

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

        url = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif, container, false);

        return view;
    }

}
