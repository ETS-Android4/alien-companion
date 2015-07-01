package com.george.redditreader.Fragments;


import android.app.ListFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.view.View;

import com.george.redditreader.Adapters.MainOptionsAdapter;
import com.george.redditreader.Models.OptionItem;
import com.george.redditreader.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainOptionsFragment extends ListFragment {

    private List<OptionItem> mainOptions;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);

        String[] titles = getResources().getStringArray(R.array.main_options_titles);
        String[] subtitles = getResources().getStringArray(R.array.main_options_subtitles);
        mainOptions = new ArrayList<>();
        for(int i=0;i<titles.length;i++) {
            mainOptions.add(new OptionItem(titles[i], subtitles[i]));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new MainOptionsAdapter(getActivity(), mainOptions));
    }


}
