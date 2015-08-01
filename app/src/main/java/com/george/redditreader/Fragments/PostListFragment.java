package com.george.redditreader.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.PostListAdapter;
import com.george.redditreader.ClickListeners.FooterListeners.SubredditFooterListener;
import com.george.redditreader.LoadTasks.LoadPostsTask;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.R;
import com.george.redditreader.api.retrieval.params.SubmissionSort;
import com.george.redditreader.api.retrieval.params.TimeSpan;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostListFragment extends Fragment {

    public PostListAdapter postListAdapter;
   // private RestClient restClient;
    public Button showMore;
    public ProgressBar footerProgressBar;
    public ProgressBar mainProgressBar;
    public ListView contentView;
    public String subreddit;
    private MainActivity activity;
    //private enum LoadType {
    //    init, refresh, extend
    //}
    public SubmissionSort submissionSort;
    private SubmissionSort tempSort;
    public TimeSpan timeSpan;
    public boolean hasPosts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        //restClient = new HttpRestClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (ListView) view.findViewById(R.id.listView);

        if(postListAdapter == null) {
            Log.d("PostListFragment", "Loading posts...");
            setSubmissionSort(SubmissionSort.HOT);
            LoadPostsTask task = new LoadPostsTask(activity, this, LoadType.init);
            task.execute();
        }
        else {
            mainProgressBar.setVisibility(View.GONE);
            contentView.setAdapter(postListAdapter);
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_sort:
                showSortPopup(activity.findViewById(R.id.action_sort));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_posts_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hot:
                        setSubmissionSort(SubmissionSort.HOT);
                        refreshList();
                        return true;
                    case R.id.action_sort_new:
                        setSubmissionSort(SubmissionSort.NEW);
                        refreshList();
                        return true;
                    case R.id.action_sort_rising:
                        setSubmissionSort(SubmissionSort.RISING);
                        refreshList();
                        return true;
                    case R.id.action_sort_top:
                        //setSubmissionSort(SubmissionSort.TOP);
                        //refreshList();
                        tempSort = SubmissionSort.TOP;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_controversial:
                        //setSubmissionSort(SubmissionSort.CONTROVERSIAL);
                        //refreshList();
                        tempSort = SubmissionSort.CONTROVERSIAL;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortTimePopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_posts_sort_time);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hour:
                        setSubmissionSort(TimeSpan.HOUR);
                        refreshList();
                        return true;
                    case R.id.action_sort_day:
                        setSubmissionSort(TimeSpan.DAY);
                        refreshList();
                        return true;
                    case R.id.action_sort_week:
                        setSubmissionSort(TimeSpan.WEEK);
                        refreshList();
                        return true;
                    case R.id.action_sort_month:
                        setSubmissionSort(TimeSpan.MONTH);
                        refreshList();
                        return true;
                    case R.id.action_sort_year:
                        setSubmissionSort(TimeSpan.YEAR);
                        refreshList();
                        return true;
                    case R.id.action_sort_all:
                        setSubmissionSort(TimeSpan.ALL);
                        refreshList();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        setActionBarSubtitle();
        createFooter();
        if(!hasPosts)
            showMore.setVisibility(View.GONE);
        else
            showMore.setVisibility(View.VISIBLE);
    }

    private void createFooter() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.footer_layout, null);
        contentView.addFooterView(view);
        footerProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        footerProgressBar.setVisibility(View.GONE);
        showMore = (Button) view.findViewById(R.id.showMore);
        showMore.setOnClickListener(new SubredditFooterListener(activity, this));
        //showMore.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        showMore.setVisibility(View.GONE);
        //        footerProgressBar.setVisibility(View.VISIBLE);
        //        PostListFragment plf = (PostListFragment)
        //                activity.getFragmentManager().findFragmentById(R.id.fragmentHolder);
        //        LoadPostsTask task = new LoadPostsTask(activity, plf, LoadType.extend);
        //        task.execute();
        //    }
        //});
    }

    //Reload Posts List
    public void refreshList() {
        Log.d("PostListFragment", "Refreshing posts...");
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        LoadPostsTask task = new LoadPostsTask(activity, this, LoadType.refresh);
        task.execute();
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
        setActionBarTitle();
    }

    public void setSubmissionSort(SubmissionSort sort) {
        this.timeSpan = null;
        this.submissionSort = sort;
        setActionBarSubtitle();
    }

    public void setSubmissionSort(TimeSpan time) {
        this.submissionSort = tempSort;
        this.timeSpan = time;
        setActionBarSubtitle();
    }

    //Set Action Bar Title
    public void setActionBarTitle() {
        String title = (subreddit == null) ? "Frontpage" : subreddit;
        activity.getSupportActionBar().setTitle(title);
    }

    //Set Action Bar Subtitle
    public void setActionBarSubtitle() {
        if(timeSpan == null) {
            activity.getSupportActionBar().setSubtitle(submissionSort.value());
        }
        else {
            activity.getSupportActionBar().setSubtitle(submissionSort.value()+": "+timeSpan.value());
        }
    }

    //private void postsError() {
    //    Toast toast = Toast.makeText(activity, "Error loading posts", Toast.LENGTH_SHORT);
    //    toast.show();
    //}

    //public static List<Thumbnail> preloadImages(List<Submission> posts, Context context) {
    //    //if (BuildConfig.DEBUG) {
    //    //    Picasso.with(activity).setIndicatorsEnabled(true);
    //    //    Picasso.with(activity).setLoggingEnabled(true);
    //    //}
    //    List<Thumbnail> thumbnails = new ArrayList<>();
    //    for(Submission post : posts) {
    //        Thumbnail thumbnail = new Thumbnail(post.getThumbnail());
    //        try {
    //            Picasso.with(context).load(post.getThumbnail()).fetch();
    //            thumbnail.setHasThumbnail(true);
    //        } catch (IllegalArgumentException e) {
    //            thumbnail.setHasThumbnail(false);
    //        }
    //        thumbnails.add(thumbnail);
    //    }
    //    return  thumbnails;
    //}

    //Main Load Task
    //class LoadTask extends AsyncTask<Void, Void, List<Submission>> {
//
    //    private Exception exception;
    //    private LoadType loadType;
//
    //    public LoadTask(LoadType loadType) {
    //        this.loadType = loadType;
    //    }
//
    //    @Override
    //    protected List<Submission> doInBackground(Void... unused) {
    //        try {
    //            Submissions subms = new Submissions(restClient);
    //            List<Submission> submissions = null;
//
    //            if(loadType == LoadType.extend) {
    //                if(subreddit == null) {
    //                    submissions = subms.frontpage(submissionSort, timeSpan, -1, RedditConstants.DEFAULT_LIMIT, postListAdapter.getLastPost(), null, true);
    //                }
    //                else {
    //                    submissions = subms.ofSubreddit(subreddit, submissionSort, timeSpan, -1, RedditConstants.DEFAULT_LIMIT, postListAdapter.getLastPost(), null, true);
    //                }
    //            }
    //            else {
    //                if(subreddit == null) {
    //                    submissions = subms.frontpage(submissionSort, timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
    //                }
    //                else {
    //                    submissions = subms.ofSubreddit(subreddit, submissionSort, timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
    //                }
    //                postListAdapter = new PostListAdapter(activity, submissions);
    //            }
    //            ImageLoader.preloadImages(submissions, activity);
    //            return submissions;
    //        } catch (RetrievalFailedException e) {
    //            exception = e;
    //        } catch (RedditError e) {
    //            exception = e;
    //        }
    //        return null;
    //    }
//
    //    @Override
    //    protected void onPostExecute(List<Submission> submissions) {
    //        if(exception != null) {
    //            DisplayToast.postsLoadError(activity);
    //            if(loadType == LoadType.extend) {
    //                footerProgressBar.setVisibility(View.GONE);
    //                showMore.setVisibility(View.VISIBLE);
    //            }
    //        }
    //        else {
    //            switch (loadType) {
    //                case init:
    //                    mainProgressBar.setVisibility(View.GONE);
    //                    contentView.setAdapter(postListAdapter);
    //                    showMore.setVisibility(View.VISIBLE);
    //                    hasPosts = true;
    //                    break;
    //                case refresh:
    //                    mainProgressBar.setVisibility(View.GONE);
    //                    if(submissions.size() != 0) {
    //                        contentView.setAdapter(postListAdapter);
    //                        contentView.setVisibility(View.VISIBLE);
    //                        hasPosts = true;
    //                        showMore.setVisibility(View.VISIBLE);
    //                    }
    //                    else {
    //                        hasPosts = false;
    //                        DisplayToast.subredditNotFound(activity);
    //                    }
    //                    break;
    //                case extend:
    //                    footerProgressBar.setVisibility(View.GONE);
    //                    postListAdapter.addAll(submissions);
    //                    showMore.setVisibility(View.VISIBLE);
    //                    break;
    //            }
    //        }
    //    }
//
    //}

}
