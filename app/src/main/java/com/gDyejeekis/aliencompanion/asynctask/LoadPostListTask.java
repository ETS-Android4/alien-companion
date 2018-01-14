package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.FilterUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.utils.ThumbnailLoader;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

import java.io.File;
import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadPostListTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception exception;
    private LoadType loadType;
    private Context context;
    private PostListFragment fragment;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private SubmissionSort sort;
    private TimeSpan time;
    private boolean changedSort;
    private String offlineSubtitle;
    private boolean randomSubreddit = false;

    public LoadPostListTask(Context context, PostListFragment fragment, LoadType loadType) {
        this.context = context;
        this.fragment = fragment;
        this.loadType = loadType;
        sort = fragment.submissionSort;
        time = fragment.timeSpan;
        changedSort = false;
    }

    public LoadPostListTask(Context context, PostListFragment fragment, LoadType loadType, SubmissionSort sort, TimeSpan time) {
        this.context = context;
        this.fragment = fragment;
        this.loadType = loadType;
        this.sort = sort;
        this.time = time;
        changedSort = true;
    }

    private List<RedditItem> readPostListFromFile(String name) {
        List<RedditItem> posts = null;
        try {
            File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(context), name);
            File file = new File(dir, name + MyApplication.SYNCED_POST_LIST_SUFFIX);
            posts = (List<RedditItem>) GeneralUtils.readObjectFromFile(file);
            if(posts == null || posts.isEmpty()) {
                throw new RuntimeException("Nonexistant/empty post list");
            }
            else {
                offlineSubtitle = fragment.isOther ? "updated " : "synced ";
                offlineSubtitle += ConvertUtils.getSubmissionAge((double) file.lastModified() / 1000);
            }
        } catch (Exception e) {
            offlineSubtitle = fragment.isOther ? "no posts" : "not synced";
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> submissions;
            if(MyApplication.offlineModeEnabled) {
                // wait until nav drawer is closed to start
                SystemClock.sleep(MyApplication.NAV_DRAWER_CLOSE_TIME - 50);

                String name = "";
                if(fragment.subreddit == null)
                    name = "frontpage";
                else {
                    if(fragment.isMulti)
                        name = MyApplication.MULTIREDDIT_FILE_PREFIX;
                    name = name + fragment.subreddit.toLowerCase();
                }
                submissions = readPostListFromFile(name);
            }
            else {
                Submissions subms = new Submissions(httpClient, MyApplication.currentUser);

                if (loadType == LoadType.extend) { // extend case
                    if (fragment.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) fragment.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    } else {
                        if(fragment.isMulti) submissions = subms.ofMultireddit(fragment.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) fragment.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                        else submissions = subms.ofSubreddit(fragment.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) fragment.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    }
                } else { // init or refresh case
                    if (fragment.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    } else {
                        if(fragment.isMulti) submissions = subms.ofMultireddit(fragment.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                        else submissions = subms.ofSubreddit(fragment.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    }
                }
            }
            checkIfRandomSubreddit(submissions);
            submissions = FilterUtils.checkProfiles(context, submissions, fragment.subreddit == null ? "frontpage" : fragment.subreddit, fragment.isMulti);
            return submissions;
        } catch (RetrievalFailedException | RedditError | NullPointerException e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    private void checkIfRandomSubreddit(List<RedditItem> items) {
        if(fragment.subreddit!=null && fragment.subreddit.equalsIgnoreCase("random")) {
            randomSubreddit = true;
            Submission post = (Submission) items.get(0);
            fragment.subreddit = post.getSubreddit().toLowerCase();
        }
    }

    @Override
    protected void onCancelled(List<RedditItem> submissions) {
        try {
            PostListFragment fragment = (PostListFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
            fragment.loadMore = MyApplication.endlessPosts;
        } catch (NullPointerException e) {}
    }

    @Override
    protected void onPostExecute(List<RedditItem> submissions) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(context);
        }

        try {
            PostListFragment fragment = (PostListFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
            this.fragment = fragment;
            this.fragment.currentLoadType = null;
            this.fragment.mainProgressBar.setVisibility(View.GONE);
            this.fragment.swipeRefreshLayout.setRefreshing(false);
            this.fragment.contentView.setVisibility(View.VISIBLE);
            if(offlineSubtitle!=null) {
                this.fragment.setActionBarSubtitle(offlineSubtitle);
            }

            if (exception != null || submissions == null) {
                if(loadType == LoadType.extend) {
                    this.fragment.adapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init) {
                    this.fragment.updateContentView(new RedditItemListAdapter(context, this.fragment.currentViewTypeValue));
                }

                View.OnClickListener listener;
                if(MyApplication.offlineModeEnabled) {
                    if(this.fragment.isOther && this.fragment.subreddit!=null && this.fragment.subreddit.equals("synced")) {
                        showNoPostsSnackbar();
                    }
                    else {
                        listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LoadPostListTask.this.fragment.addToSyncQueue();
                            }
                        };
                        this.fragment.setSnackbar(ToastUtils.showSnackbar(this.fragment.getSnackbarParentView(), "No synced posts found", "Sync", listener, Snackbar.LENGTH_INDEFINITE));
                    }
                }
                else {
                    String message = "Error loading posts";
                    if(loadType!=LoadType.extend) {
                        listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LoadPostListTask.this.fragment.refreshList();
                            }
                        };
                        this.fragment.setSnackbar(ToastUtils.showSnackbar(this.fragment.getSnackbarParentView(), message, "Retry", listener, Snackbar.LENGTH_INDEFINITE));
                    }
                    else {
                        ToastUtils.showSnackbar(this.fragment.getSnackbarParentView(), message);
                    }
                }

            } else {
                if(submissions.size()>0) {
                    if(!MyApplication.noThumbnails) ThumbnailLoader.preloadThumbnails(submissions, context); //TODO: used to throw indexoutofboundsexception in offline mode
                }
                this.fragment.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;

                switch (loadType) {
                    case init:
                        if(submissions.size()==0) {
                            this.fragment.updateContentView(new RedditItemListAdapter(context, this.fragment.currentViewTypeValue));
                            showNoPostsSnackbar();
                        }
                        else {
                            this.fragment.updateContentView(new RedditItemListAdapter(context, this.fragment.currentViewTypeValue, submissions));
                        }

                        if(randomSubreddit) {
                            fragment.setActionBarTitle();
                        }
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            if(changedSort) {
                                this.fragment.submissionSort = sort;
                                this.fragment.timeSpan = time;
                            }
                            if(!MyApplication.offlineModeEnabled)
                                this.fragment.setActionBarSubtitle();
                            this.fragment.updateContentView(new RedditItemListAdapter(context, this.fragment.currentViewTypeValue, submissions));
                        }
                        else {
                            showNoPostsSnackbar();
                        }
                        break;
                    case extend:
                        this.fragment.adapter.setLoadingMoreItems(false);
                        this.fragment.adapter.addAll(submissions);
                        this.fragment.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showNoPostsSnackbar() {
        String message = "No posts found";
        if (MyApplication.hideNSFW && !MyApplication.offlineModeEnabled) {
            message = message.concat(" (NSFW filter is enabled)");
        }
        fragment.setSnackbar(ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message));
    }

}
