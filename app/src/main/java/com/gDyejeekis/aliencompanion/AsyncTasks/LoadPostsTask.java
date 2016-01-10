package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.Utils.ImageLoader;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

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
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private RedditItemListAdapter adapter;
    private SubmissionSort sort;
    private TimeSpan time;
    private boolean changedSort;

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        //httpClient = new PoliteRedditHttpClient();
        sort = plf.submissionSort;
        time = plf.timeSpan;
        changedSort = false;
    }

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType, SubmissionSort sort, TimeSpan time) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        //httpClient = new PoliteRedditHttpClient();
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
            if(MyApplication.offlineModeEnabled) {
                String filename;
                if(plf.subreddit == null) filename = "frontpage";
                else filename = plf.subreddit.toLowerCase();
                submissions = readPostsFromFile(filename);
                if(submissions!=null) adapter = new RedditItemListAdapter(context, submissions);//plf.postListAdapter = new RedditItemListAdapter(context, submissions);
            }
            else {
                Submissions subms = new Submissions(httpClient, MyApplication.currentUser);

                if (loadType == LoadType.extend) {
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    }
                    adapter = plf.postListAdapter;
                } else {
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    }
                    adapter = new RedditItemListAdapter(context, submissions);
                }
                //ImageLoader.preloadThumbnails(submissions, context); //TODO: fix image preloading
            }
            //ConvertUtils.preparePostsText(context, submissions);
            return submissions;
        } catch (RetrievalFailedException | RedditError | NullPointerException e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled(List<RedditItem> submissions) {
        try {
            PostListFragment fragment = (PostListFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
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
            PostListFragment fragment = (PostListFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            plf = fragment;
            plf.currentLoadType = null;
            plf.mainProgressBar.setVisibility(View.GONE);
            plf.swipeRefreshLayout.setRefreshing(false);
            plf.contentView.setVisibility(View.VISIBLE);

            if (exception != null || submissions == null) {
                if(loadType == LoadType.extend) {
                    plf.postListAdapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init) {
                    plf.postListAdapter = new RedditItemListAdapter(context);
                    plf.contentView.setAdapter(plf.postListAdapter);
                }
                if(MyApplication.offlineModeEnabled) ToastUtils.displayShortToast(context, "No posts found");
                else ToastUtils.postsLoadError(context);
            } else {
                if(submissions.size()>0) {
                    //if(!MyApplication.offlineModeEnabled)
                        ImageLoader.preloadThumbnails(submissions, context); //TODO: throws indexoutofboundsexception in offline mode
                    plf.postListAdapter = adapter;
                }
                plf.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;

                switch (loadType) {
                    case init:
                        if(submissions.size()==0) {
                            plf.contentView.setAdapter(new RedditItemListAdapter(context));
                            ToastUtils.displayShortToast(context, "No posts found");
                        }
                        else plf.contentView.setAdapter(plf.postListAdapter);
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
                        else ToastUtils.displayShortToast(context, "No posts found");
                        break;
                    case extend:
                        plf.postListAdapter.setLoadingMoreItems(false);
                        plf.postListAdapter.addAll(submissions);
                        plf.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //Log.d("geo test", "loadmore: " + plf.loadMore + " hasMore: " + plf.hasMore);
    }

}
