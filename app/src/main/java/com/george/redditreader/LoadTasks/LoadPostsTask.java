package com.george.redditreader.LoadTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Adapters.PostListAdapter;
import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.Utils.DisplayToast;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Submissions;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.restClient.HttpRestClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadPostsTask extends AsyncTask<Void, Void, List<Submission>> {

    private Exception exception;
    private LoadType loadType;
    private Activity activity;
    private PostListFragment plf;
    private HttpRestClient restClient;

    public LoadPostsTask(Activity activity, PostListFragment plf, LoadType loadType) {
        this.activity = activity;
        this.plf = plf;
        this.loadType = loadType;
        restClient = new HttpRestClient();
    }

    @Override
    protected List<Submission> doInBackground(Void... unused) {
        try {
            Submissions subms = new Submissions(restClient);
            List<Submission> submissions = null;

            if(loadType == LoadType.extend) {
                if(plf.subreddit == null) {
                    submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, plf.postListAdapter.getLastPost(), null, true);
                }
                else {
                    submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, plf.postListAdapter.getLastPost(), null, true);
                }
            }
            else {
                if(plf.subreddit == null) {
                    submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                }
                else {
                    submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                }
                plf.postListAdapter = new PostListAdapter(activity, submissions);
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
                plf.footerProgressBar.setVisibility(View.GONE);
                plf.showMore.setVisibility(View.VISIBLE);
            }
        }
        else {
            switch (loadType) {
                case init:
                    plf.mainProgressBar.setVisibility(View.GONE);
                    plf.contentView.setAdapter(plf.postListAdapter);
                    plf.showMore.setVisibility(View.VISIBLE);
                    plf.hasPosts = true;
                    break;
                case refresh:
                    plf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        plf.contentView.setAdapter(plf.postListAdapter);
                        plf.contentView.setVisibility(View.VISIBLE);
                        plf.hasPosts = true;
                        plf.showMore.setVisibility(View.VISIBLE);
                    }
                    else {
                        plf.hasPosts = false;
                        DisplayToast.subredditNotFound(activity);
                    }
                    break;
                case extend:
                    plf.footerProgressBar.setVisibility(View.GONE);
                    plf.postListAdapter.addAll(submissions);
                    plf.showMore.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
