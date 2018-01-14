package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.utils.FilterUtils;
import com.gDyejeekis.aliencompanion.utils.ThumbnailLoader;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.retrieval.params.QuerySyntax;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadSearchTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception exception;
    private Context context;
    private SearchFragment sf;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private LoadType loadType;
    private SearchSort sort;
    private TimeSpan time;
    private boolean changedSort;

    public LoadSearchTask(Context context, SearchFragment searchFragment, LoadType loadType) {
        this.context = context;
        this.sf = searchFragment;
        this.loadType = loadType;
        sort = sf.searchSort;
        time = sf.timeSpan;
        changedSort = false;
    }

    public LoadSearchTask(Context context, SearchFragment searchFragment, LoadType loadType, SearchSort sort, TimeSpan time) {
        this.context = context;
        this.sf = searchFragment;
        this.loadType = loadType;
        this.sort = sort;
        this.time = time;
        changedSort = true;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            Submissions subms = new Submissions(httpClient, MyApplication.currentUser);
            List<RedditItem> submissions;

            if(loadType == LoadType.extend) {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) sf.adapter.getLastItem(), null, true);
            }
            else {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
            }
            submissions = FilterUtils.checkProfiles(context, submissions, sf.subreddit, false);
            return submissions;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled(List<RedditItem> submissions) {
        try {
            SearchFragment fragment = (SearchFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
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
            SearchFragment fragment = (SearchFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
            sf = fragment;
            sf.currentLoadType = null;
            sf.mainProgressBar.setVisibility(View.GONE);
            sf.swipeRefreshLayout.setRefreshing(false);
            sf.contentView.setVisibility(View.VISIBLE);

            if (exception != null) {
                String message = "Search failed";
                if(GeneralUtils.isNetworkAvailable(context)) {
                    message += " - Reddit's servers are under heavy load. Please try again in a bit.";
                    ToastUtils.showSnackbar(sf.getSnackbarParentView(), message, Snackbar.LENGTH_LONG);
                }
                else {
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sf.refreshList();
                        }
                    };
                    sf.setSnackbar(ToastUtils.showSnackbar(sf.getSnackbarParentView(), message, "Retry", listener, Snackbar.LENGTH_INDEFINITE));
                }

                if (loadType == LoadType.extend) {
                    sf.adapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init){
                    sf.updateContentViewAdapter(new RedditItemListAdapter(context));
                }
            } else {
                if(submissions.size()>0) {
                    if(!MyApplication.noThumbnails) ThumbnailLoader.preloadThumbnails(submissions, context);
                }
                sf.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;
                switch (loadType) {
                    case init:
                        if(submissions.size()==0) {
                            sf.updateContentViewAdapter(new RedditItemListAdapter(context));
                            ToastUtils.showSnackbar(sf.getSnackbarParentView(), "No results for \'" + sf.searchQuery + "\'");
                        }
                        else {
                            sf.updateContentViewAdapter(new RedditItemListAdapter(context, submissions));
                        }
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            if(changedSort) {
                                sf.searchSort = sort;
                                sf.timeSpan = time;
                                sf.setActionBarSubtitle();
                            }
                            sf.updateContentViewAdapter(new RedditItemListAdapter(context, submissions));
                        }
                        else ToastUtils.showToast(context, "No posts found");
                        break;
                    case extend:
                        sf.adapter.setLoadingMoreItems(false);
                        sf.adapter.addAll(submissions);
                        sf.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
