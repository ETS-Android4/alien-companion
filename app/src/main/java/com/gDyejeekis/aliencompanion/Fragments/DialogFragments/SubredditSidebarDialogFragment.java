package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.SubredditInfo;

/**
 * Created by sound on 2/29/2016.
 */
public class SubredditSidebarDialogFragment extends DialogFragment {

    private String subreddit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subreddit = getArguments().getString("subreddit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit_sidebar, container, false);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    public void bindData(SubredditInfo info) {
        // TODO: 2/29/2016
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogDimens();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogDimens();
    }

    private void setDialogDimens() {
        Window window = getDialog().getWindow();
        int width = 95 * getResources().getDisplayMetrics().widthPixels / 100;
        //int height = 95 * getResources().getDisplayMetrics().heightPixels / 100;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

}
