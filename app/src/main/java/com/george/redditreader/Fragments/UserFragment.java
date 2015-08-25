package com.george.redditreader.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.george.redditreader.Adapters.UserAdapter;
import com.george.redditreader.ClickListeners.FooterListeners.UserFooterListener;
import com.george.redditreader.LoadTasks.LoadUserInfoTask;
import com.george.redditreader.api.retrieval.params.UserSubmissionsCategory;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.R;
import com.george.redditreader.api.retrieval.params.UserOverviewSort;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    public ProgressBar progressBar;
    public ProgressBar footerProgressBar;
    public ListView contentView;
    public UserAdapter userAdapter;
    private AppCompatActivity activity;
    public String username;
    public UserOverviewSort userOverviewSort;
    public UserSubmissionsCategory userContent;
    public Button showMore;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        username = activity.getIntent().getStringExtra("username");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        setActionBarSubtitle();
        createFooter();
    }

    private void createFooter() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.footer_layout, null);
        contentView.addFooterView(view);
        footerProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        footerProgressBar.setVisibility(View.GONE);
        showMore = (Button) view.findViewById(R.id.showMore);
        showMore.setOnClickListener(new UserFooterListener(activity, this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar5);
        contentView = (ListView) view.findViewById(R.id.listView2);

        if(userAdapter == null) {
            //setUserContent(UserContent.overview);
            userContent = UserSubmissionsCategory.OVERVIEW;
            //setUserOverviewSort(UserOverviewSort.NEW);
            userOverviewSort = UserOverviewSort.NEW;
            LoadUserInfoTask task = new LoadUserInfoTask(activity, this, LoadType.init, userContent);
            task.execute();
        }
        else {
            progressBar.setVisibility(View.GONE);
            contentView.setAdapter(userAdapter);
        }

        return view;
    }

    //Refresh User Posts
    private void refreshUser() {
        contentView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        LoadUserInfoTask task = new LoadUserInfoTask(activity, this, LoadType.refresh, userContent);
        task.execute();
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(username);
    }

    //public void setUserContent(UserContent userContent) {
    //    this.userContent = userContent;
    //}

    //public void setUserOverviewSort (UserOverviewSort sort) {
    //    this.userOverviewSort = sort;
    //}

    public void setActionBarSubtitle() {
        String subtitle = userContent.value();
        if(userOverviewSort != null) subtitle = subtitle.concat(": " + userOverviewSort.value());
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                refreshUser();
                return true;
            case R.id.action_sort:
                showContentPopup(activity.findViewById(R.id.action_sort));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showContentPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.inflate(R.menu.menu_user_content);
        //popupMenu.inflate(R.menu.menu_user_content_public);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_user_overview:
                        userContent = UserSubmissionsCategory.OVERVIEW;
                        showSortPopup();
                        return true;
                    case R.id.action_user_comments:
                        userContent = UserSubmissionsCategory.COMMENTS;
                        showSortPopup();
                        return true;
                    case R.id.action_user_submitted:
                        userContent = UserSubmissionsCategory.SUBMITTED;
                        showSortPopup();
                        return true;
                    case R.id.action_user_gilded:
                        userOverviewSort = null;
                        userContent = UserSubmissionsCategory.GILDED;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_user_upvoted:
                        userOverviewSort = null;
                        userContent = UserSubmissionsCategory.LIKED;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_user_downvoted:
                        userOverviewSort = null;
                        userContent = UserSubmissionsCategory.DISLIKED;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_user_hidden:
                        userOverviewSort = null;
                        userContent = UserSubmissionsCategory.HIDDEN;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_user_saved:
                        userOverviewSort = null;
                        userContent = UserSubmissionsCategory.SAVED;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortPopup() {
        showSortPopup(activity.findViewById(R.id.action_sort));
    }

    private void showSortPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.inflate(R.menu.menu_user_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_new:
                        userOverviewSort = UserOverviewSort.NEW;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_sort_hot:
                        userOverviewSort = UserOverviewSort.HOT;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_sort_top:
                        userOverviewSort = UserOverviewSort.TOP;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    case R.id.action_sort_controversial:
                        userOverviewSort = UserOverviewSort.COMMENTS;
                        setActionBarSubtitle();
                        refreshUser();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

}
