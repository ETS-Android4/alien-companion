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
import com.george.redditreader.LoadTasks.LoadUserTask;
import com.george.redditreader.enums.UserContent;
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
    public UserContent userContent;
    public Button showMore;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        //restClient = new HttpRestClient();
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
            setUserContent(UserContent.overview);
            setUserOverviewSort(UserOverviewSort.NEW);
            LoadUserTask task = new LoadUserTask(activity, this, LoadType.init, userContent);
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
        LoadUserTask task = new LoadUserTask(activity, this, LoadType.refresh, userContent);
        task.execute();
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(username);
    }

    public void setUserContent(UserContent userContent) {
        this.userContent = userContent;
    }

    public void setUserOverviewSort (UserOverviewSort sort) {
        this.userOverviewSort = sort;
    }

    public void setActionBarSubtitle() {
        String subtitle = userContent.value() + ": " + userOverviewSort.value();
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_user_overview:
                        setUserContent(UserContent.overview);
                        showSortPopup();
                        return true;
                    case R.id.action_user_comments:
                        setUserContent(UserContent.comments);
                        showSortPopup();
                        return true;
                    case R.id.action_user_submitted:
                        setUserContent(UserContent.submitted);
                        showSortPopup();
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
                        setUserOverviewSort(UserOverviewSort.NEW);
                        refreshUser();
                        return true;
                    case R.id.action_sort_hot:
                        setUserOverviewSort(UserOverviewSort.HOT);
                        refreshUser();
                        return true;
                    case R.id.action_sort_top:
                        setUserOverviewSort(UserOverviewSort.TOP);
                        refreshUser();
                        return true;
                    case R.id.action_sort_controversial:
                        setUserOverviewSort(UserOverviewSort.COMMENTS);
                        refreshUser();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    //class LoadUserTask extends AsyncTask<Void, Void, List<Object>> {
//
    //    private Exception mException;
    //    private LoadType mLoadType;
    //    private LoadContent mLoadContent;
//
    //    public LoadUserTask(LoadType loadType, LoadContent userContent) {
    //        mLoadContent = userContent;
    //        mLoadType = loadType;
    //    }
//
    //    @Override
    //    protected List<Object> doInBackground(Void... unused) {
    //        try {
    //            List<Object> userContent = null;
    //            switch (mLoadContent) {
    //                case overview:
    //                    UserOverview userOverview = new UserOverview(restClient);
    //                    if(mLoadType == LoadType.extend) {
    //                        Object lastObject = userAdapter.getLastObject();
    //                        userContent = userOverview.ofUser(username, userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, (Thing) lastObject, null, true);
//
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    else {
    //                        UserDetails userDetails = new UserDetails(restClient);
    //                        UserInfo userInfo = userDetails.ofUser(username);
    //                        userInfo.retrieveTrophies(activity, restClient);
//
    //                        userContent = userOverview.ofUser(username, userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
//
    //                        userAdapter = new UserAdapter(activity);
    //                        userAdapter.add(userInfo);
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    ImageLoader.preloadUserImages(userContent, activity);
    //                    break;
    //                case comments:
    //                    Comments comments = new Comments(restClient);
    //                    if(mLoadType == LoadType.extend) {
    //                        Comment lastComment = (Comment) userAdapter.getLastObject();
    //                        userContent = (List<Object>) (List<?>) comments.ofUser(username, userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, lastComment, null, true);
//
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    else {
    //                        userContent = (List<Object>) (List<?>) comments.ofUser(username, userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
//
    //                        userAdapter = new UserAdapter(activity);
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    break;
    //                case submitted:
    //                    Submissions submissions = new Submissions(restClient);
    //                    if(mLoadType == LoadType.extend) {
    //                        Submission lastPost = (Submission) userAdapter.getLastObject();
    //                        userContent = (List<Object>) (List<?>) submissions.ofUser(username, UserSubmissionsCategory.SUBMITTED, userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, lastPost, null, true);
//
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    else {
    //                        userContent = (List<Object>) (List<?>) submissions.ofUser(username, UserSubmissionsCategory.SUBMITTED, userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
//
    //                        userAdapter = new UserAdapter(activity);
    //                        userAdapter.addAll(userContent);
    //                    }
    //                    ImageLoader.preloadUserImages(userContent, activity);
    //                    break;
    //            }
    //            return userContent;
    //        } catch (RetrievalFailedException e) {
    //            mException = e;
    //        } catch (RedditError e) {
    //            mException = e;
    //        } catch (NullPointerException e) {
    //            mException = e;
    //        }
    //        return null;
    //    }
//
    //    @Override
    //    protected void onPostExecute(List<Object> things) {
    //        if(mException != null) {
    //            ToastUtils.userLoadError(activity);
    //            if(mLoadType == LoadType.extend) {
    //                footerProgressBar.setVisibility(View.GONE);
    //                showMore.setVisibility(View.VISIBLE);
    //            }
    //        }
    //        else {
    //            switch (mLoadType) {
    //                case init:
    //                    progressBar.setVisibility(View.GONE);
    //                    contentView.setAdapter(userAdapter);
    //                    break;
    //                case refresh:
    //                    if(things.size() != 0) {
    //                        progressBar.setVisibility(View.GONE);
    //                        contentView.setAdapter(userAdapter);
    //                        contentView.setVisibility(View.VISIBLE);
    //                    }
    //                    break;
    //                case extend:
    //                    footerProgressBar.setVisibility(View.GONE);
    //                    showMore.setVisibility(View.VISIBLE);
    //                    break;
    //            }
    //        }
    //    }
    //}

}
