package com.gDyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.Utils.ImageLoader;
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
    //private Activity activity;
    private Context context;
    private SearchFragment sf;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private LoadType loadType;
    private RedditItemListAdapter adapter;
    private SearchSort sort;
    private TimeSpan time;
    private boolean changedSort;

    public LoadSearchTask(Context context, SearchFragment searchFragment, LoadType loadType) {
        this.context = context;
        this.sf = searchFragment;
        this.loadType = loadType;
        //httpClient = new PoliteRedditHttpClient();
        sort = sf.searchSort;
        time = sf.timeSpan;
        changedSort = false;
    }

    public LoadSearchTask(Context context, SearchFragment searchFragment, LoadType loadType, SearchSort sort, TimeSpan time) {
        this.context = context;
        this.sf = searchFragment;
        this.loadType = loadType;
        //httpClient = new PoliteRedditHttpClient();
        this.sort = sort;
        this.time = time;
        changedSort = true;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        //SystemClock.sleep(5000);
        try {
            Submissions subms = new Submissions(httpClient, MainActivity.currentUser);
            List<RedditItem> submissions;

            if(loadType == LoadType.extend) {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) sf.postListAdapter.getLastItem(), null, true);
                adapter = sf.postListAdapter;
            }
            else {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                adapter = new RedditItemListAdapter(context, submissions);
            }
            //ImageLoader.preloadThumbnails(submissions, context);
            //ConvertUtils.preparePostsText(context, submissions);
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
            SearchFragment fragment = (SearchFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            fragment.loadMore = MainActivity.endlessPosts;
        } catch (NullPointerException e) {}
    }

    @Override
    protected void onPostExecute(List<RedditItem> submissions) {
        try {
            SearchFragment fragment = (SearchFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            sf = fragment;
            sf.currentLoadType = null;
            sf.mainProgressBar.setVisibility(View.GONE);
            sf.swipeRefreshLayout.setRefreshing(false);
            sf.contentView.setVisibility(View.VISIBLE);

            if (exception != null) {
                ToastUtils.postsLoadError(context);
                if (loadType == LoadType.extend) {
                    sf.postListAdapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init){
                    sf.postListAdapter = new RedditItemListAdapter(context);
                    sf.contentView.setAdapter(sf.postListAdapter);
                }
            } else {
                if(submissions.size()>0) {
                    ImageLoader.preloadThumbnails(submissions, context);
                    sf.postListAdapter = adapter;
                }
                sf.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;
                switch (loadType) {
                    case init:
                        if(submissions.size()==0) {
                            sf.contentView.setAdapter(new RedditItemListAdapter(context));
                            ToastUtils.noResults(context, sf.searchQuery);
                        }
                        else sf.contentView.setAdapter(sf.postListAdapter);
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            if(changedSort) {
                                sf.searchSort = sort;
                                sf.timeSpan = time;
                                sf.setActionBarSubtitle();
                            }
                            sf.contentView.setAdapter(sf.postListAdapter);
                        }
                        else ToastUtils.displayShortToast(context, "No posts found");
                        break;
                    case extend:
                        sf.postListAdapter.setLoadingMoreItems(false);
                        sf.postListAdapter.addAll(submissions);
                        sf.loadMore = MainActivity.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
