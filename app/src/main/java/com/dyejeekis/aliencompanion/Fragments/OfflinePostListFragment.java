package com.dyejeekis.aliencompanion.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class OfflinePostListFragment extends Fragment {

    private RecyclerView contentView;
    private RedditItemListAdapter postListAdapter;
    private String subreddit;
    private SubmissionSort submissionSort;
    private TimeSpan timeSpan;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_offline_post_list, container, false);
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        contentView = (RecyclerView) view.findViewById(R.id.recyclerView_postList);

        contentView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contentView.setHasFixedSize(true);
        contentView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                //refreshList();
                return true;
            case R.id.action_sort:
                //showSortPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_hide_read:
                postListAdapter.hideReadPosts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public void setSubmissionSort(SubmissionSort sort) {
        this.submissionSort = sort;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

}
