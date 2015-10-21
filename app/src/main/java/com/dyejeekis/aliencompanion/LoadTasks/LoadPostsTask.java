package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.Fragments.PostListFragment;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.Utils.ImageLoader;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Submissions;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    private RedditItemListAdapter adapter;
    private SubmissionSort sort;
    private TimeSpan time;
    private boolean changedSort;

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
        sort = plf.submissionSort;
        time = plf.timeSpan;
        changedSort = false;
    }

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType, SubmissionSort sort, TimeSpan time) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
        this.sort = sort;
        this.time = time;
        changedSort = true;
    }

    private List<RedditItem> readPostsFromFile(String filename) {
        List<RedditItem> posts = null;
        try {
            FileInputStream fis = context.openFileInput(filename.toLowerCase());
            ObjectInputStream ois = new ObjectInputStream(fis);
            posts = (List<RedditItem>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        //SystemClock.sleep(5000);
        try {
            List<RedditItem> submissions;
            if(MainActivity.offlineModeEnabled) {
                String filename;
                if(plf.subreddit == null) filename = "frontpage";
                else filename = plf.subreddit.toLowerCase();
                submissions = readPostsFromFile(filename);
                if(submissions!=null) adapter = new RedditItemListAdapter(context, submissions);//plf.postListAdapter = new RedditItemListAdapter(context, submissions);
            }
            else {
                Submissions subms = new Submissions(httpClient, MainActivity.currentUser);

                if (loadType == LoadType.extend) {
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                    }
                    adapter = plf.postListAdapter;
                } else {
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                    }
                    adapter = new RedditItemListAdapter(context, submissions);
                }
                ImageLoader.preloadThumbnails(submissions, context); //TODO: fix image preloading
            }
            //ConvertUtils.preparePostsText(context, submissions);
            return submissions;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<RedditItem> submissions) {
        try {
            PostListFragment fragment = (PostListFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            plf = fragment;
            plf.currentLoadType = null;
            plf.mainProgressBar.setVisibility(View.GONE);
            plf.swipeRefreshLayout.setRefreshing(false);
            plf.contentView.setVisibility(View.VISIBLE);

            if (exception != null || submissions == null) {
                if (MainActivity.offlineModeEnabled)
                    ToastUtils.displayShortToast(context, "No posts found");
                else {
                    ToastUtils.postsLoadError(context);
                    if (loadType == LoadType.extend) {
                        plf.postListAdapter.setLoadingMoreItems(false);
                    }
                    else if(loadType == LoadType.init){
                        plf.postListAdapter = new RedditItemListAdapter(context);
                        plf.contentView.setAdapter(plf.postListAdapter);
                    }
                }
            } else {
                if(submissions.size()>0) plf.postListAdapter = adapter;
                plf.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT;

                switch (loadType) {
                    case init:
                        plf.contentView.setAdapter(plf.postListAdapter);
                        if(submissions.size()==0) ToastUtils.subredditNotFound(context);
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            if(changedSort) {
                                plf.submissionSort = sort;
                                plf.timeSpan = time;
                                plf.setActionBarSubtitle();
                            }
                            plf.contentView.setAdapter(plf.postListAdapter);
                        }
                        break;
                    case extend:
                        plf.postListAdapter.setLoadingMoreItems(false);
                        plf.postListAdapter.addAll(submissions);
                        plf.loadMore = MainActivity.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //Log.d("geo test", "loadmore: " + plf.loadMore + " hasMore: " + plf.hasMore);
    }

}
