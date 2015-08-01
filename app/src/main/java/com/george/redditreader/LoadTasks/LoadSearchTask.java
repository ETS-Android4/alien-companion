package com.george.redditreader.LoadTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Adapters.PostListAdapter;
import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.Utils.DisplayToast;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Submissions;
import com.george.redditreader.api.retrieval.params.QuerySyntax;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.restClient.HttpRestClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadSearchTask extends AsyncTask<Void, Void, List<Submission>> {

    private Exception exception;
    private Activity activity;
    private SearchFragment sf;
    private HttpRestClient restClient;
    private LoadType loadType;

    public LoadSearchTask(Activity activity, SearchFragment searchFragment, LoadType loadType) {
        this.activity = activity;
        this.sf = searchFragment;
        this.loadType = loadType;
        restClient = new HttpRestClient();
    }

    @Override
    protected List<Submission> doInBackground(Void... unused) {
        try {
            Submissions subms = new Submissions(restClient);
            List<Submission> submissions = null;

            if(loadType == LoadType.extend) {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sf.searchSort, sf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, sf.postListAdapter.getLastPost(), null, true);
            }
            else {
                submissions = subms.search(sf.subreddit, sf.searchQuery, QuerySyntax.PLAIN, sf.searchSort, sf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                sf.postListAdapter = new PostListAdapter(activity, submissions);
            }
            ImageLoader.preloadImages(submissions, activity);
            return submissions;
        } catch (RetrievalFailedException e) {
            exception = e;
        } catch (RedditError e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Submission> submissions) {
        if(exception != null) {
            DisplayToast.postsLoadError(activity);
            if(loadType == LoadType.extend) {
                sf.footerProgressBar.setVisibility(View.GONE);
                sf.showMore.setVisibility(View.VISIBLE);
            }
        }
        else {
            switch (loadType) {
                case init:
                    sf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        sf.contentView.setAdapter(sf.postListAdapter);
                        sf.showMore.setVisibility(View.VISIBLE);
                        sf.hasPosts = true;
                    }
                    else {
                        sf.hasPosts = false;
                        DisplayToast.noResults(activity, sf.searchQuery);
                    }
                    break;
                case refresh:
                    sf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        sf.contentView.setAdapter(sf.postListAdapter);
                        sf.contentView.setVisibility(View.VISIBLE);
                        sf.showMore.setVisibility(View.VISIBLE);
                        sf.hasPosts = true;
                    }
                    else {
                        sf.hasPosts = false;
                        DisplayToast.noResults(activity, sf.searchQuery);
                    }
                    break;
                case extend:
                    sf.footerProgressBar.setVisibility(View.GONE);
                    sf.postListAdapter.addAll(submissions);
                    sf.showMore.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
