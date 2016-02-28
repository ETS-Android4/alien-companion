package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 2/29/2016.
 */
public class SubredditSidebarDialogFragment extends ScalableDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit_sidebar, container, false);

        return view;
    }

}
