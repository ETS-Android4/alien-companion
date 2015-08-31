package com.george.redditreader.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.RedditItemListAdapter;
import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.Models.RedditItem;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Submissions;
import com.george.redditreader.api.utils.RedditConstants;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadPostsTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception exception;
    private LoadType loadType;
    //private Activity activity;
    private Context context;
    private PostListFragment plf;
    private HttpClient httpClient;

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            Submissions subms = new Submissions(httpClient, MainActivity.currentUser);
            List<RedditItem> submissions = null;

            if(loadType == LoadType.extend) {
                if(plf.subreddit == null) {
                    submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                }
                else {
                    submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                }
            }
            else {
                if(plf.subreddit == null) {
                    submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                }
                else {
                    submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                }
                plf.postListAdapter = new RedditItemListAdapter(context, submissions);
            }
            ImageLoader.preloadThumbnails(submissions, context); //TODO: fix image preloading
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
                //plf.footerProgressBar.setVisibility(View.GONE);
                //plf.showMore.setVisibility(View.VISIBLE);
                //plf.postListAdapter.loadingMoreItems = false;
                //plf.postListAdapter.notifyDataSetChanged();
                plf.postListAdapter.setLoadingMoreItems(false);
            }
        }
        else {
            switch (loadType) {
                case init:
                    plf.mainProgressBar.setVisibility(View.GONE);
                    //plf.contentView.setAdapter(plf.postListAdapterOld);
                    plf.contentView.setAdapter(plf.postListAdapter);
                    //plf.showMore.setVisibility(View.VISIBLE);
                    plf.hasPosts = true;
                    break;
                case refresh:
                    plf.mainProgressBar.setVisibility(View.GONE);
                    if(submissions.size() != 0) {
                        //plf.contentView.setAdapter(plf.postListAdapterOld);
                        plf.contentView.setAdapter(plf.postListAdapter);
                        plf.contentView.setVisibility(View.VISIBLE);
                        plf.hasPosts = true;
                        //plf.showMore.setVisibility(View.VISIBLE);
                    }
                    else {
                        plf.hasPosts = false;
                        ToastUtils.subredditNotFound(context);
                    }
                    break;
                case extend:
                    //plf.footerProgressBar.setVisibility(View.GONE);
                    //plf.postListAdapterOld.addAll(submissions);
                    //plf.showMore.setVisibility(View.VISIBLE);
                    //plf.postListAdapter.loadingMoreItems = false;
                    //plf.postListAdapter.addAll(submissions);
                    //plf.postListAdapter.notifyDataSetChanged();
                    plf.postListAdapter.setLoadingMoreItems(false);
                    plf.postListAdapter.addAll(submissions);
                    break;
            }
        }
    }

}
