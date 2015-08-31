package com.george.redditreader.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.RedditItemListAdapter;
import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.Models.RedditItem;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Submissions;
import com.george.redditreader.api.retrieval.params.QuerySyntax;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadSearchTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception exception;
    //private Activity activity;
    private Context context;
    private SearchFragment sf;
    private HttpClient httpClient;
    private LoadType loadType;

    public LoadSearchTask(Context context, SearchFragment searchFragment, LoadType loadType) {
        this.context = context;
        this.sf = searchFragment;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            Submissions subms = new Submissions(httpClient, MainActivity.currentUser);
            List<RedditItem> submissions;

            if(loadType == LoadType.extend) {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sf.searchSort, sf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, (Submission) sf.postListAdapter.getLastItem(), null, true);
            }
            else {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sf.searchSort, sf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                sf.postListAdapter = new RedditItemListAdapter(context, submissions);
            }
            ImageLoader.preloadThumbnails(submissions, context);
            return submissions;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<RedditItem> submissions) {
        if(exception != null) {
            ToastUtils.postsLoadError(context);
            if(loadType == LoadType.extend) {
                //sf.footerProgressBar.setVisibility(View.GONE);
                //sf.showMore.setVisibility(View.VISIBLE);
                sf.postListAdapter.setLoadingMoreItems(false);
            }
        }
        else {
            switch (loadType) {
                case init:
                    sf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        sf.contentView.setAdapter(sf.postListAdapter);
                        //sf.showMore.setVisibility(View.VISIBLE);
                        sf.hasPosts = true;
                    }
                    else {
                        sf.hasPosts = false;
                        ToastUtils.noResults(context, sf.searchQuery);
                    }
                    break;
                case refresh:
                    sf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        sf.contentView.setAdapter(sf.postListAdapter);
                        sf.contentView.setVisibility(View.VISIBLE);
                        //sf.showMore.setVisibility(View.VISIBLE);
                        sf.hasPosts = true;
                    }
                    else {
                        sf.hasPosts = false;
                        ToastUtils.noResults(context, sf.searchQuery);
                    }
                    break;
                case extend:
                    //sf.footerProgressBar.setVisibility(View.GONE);
                    //sf.postListAdapterOld.addAll(submissions);
                    //sf.showMore.setVisibility(View.VISIBLE);
                    sf.postListAdapter.setLoadingMoreItems(false);
                    sf.postListAdapter.addAll(submissions);
                    break;
            }
        }
    }
}
