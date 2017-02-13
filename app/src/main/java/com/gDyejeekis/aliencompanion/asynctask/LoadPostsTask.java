package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.utils.ImageLoader;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

import java.io.File;
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
    private Context context;
    private PostListFragment plf;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private RedditItemListAdapter adapter;
    private SubmissionSort sort;
    private TimeSpan time;
    private boolean changedSort;

    private int viewTypeValue;

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        sort = plf.submissionSort;
        time = plf.timeSpan;
        changedSort = false;
        viewTypeValue = plf.getCurrentViewTypeValue();
    }

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType, SubmissionSort sort, TimeSpan time) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        this.sort = sort;
        this.time = time;
        changedSort = true;
        viewTypeValue = plf.getCurrentViewTypeValue();
    }

    private List<RedditItem> readPostsFromFile(String filename) {
        List<RedditItem> posts = null;
        try {
            FileInputStream fis = new FileInputStream(new File(GeneralUtils.getActiveDir(context), filename.toLowerCase()));
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
        try {
            List<RedditItem> submissions;
            if(MyApplication.offlineModeEnabled) {
                // wait until nav drawer is closed to start
                SystemClock.sleep(MyApplication.NAV_DRAWER_CLOSE_TIME - 50);

                String filename = "";
                if(plf.subreddit == null) filename = "frontpage";
                else {
                    if(plf.isMulti) filename = MyApplication.MULTIREDDIT_FILE_PREFIX;
                    filename = filename + plf.subreddit.toLowerCase();
                }
                submissions = readPostsFromFile(filename + DownloaderService.LOCA_POST_LIST_SUFFIX);
                if(submissions!=null) adapter = new RedditItemListAdapter(context, viewTypeValue, submissions);
            }
            else {
                Submissions subms = new Submissions(httpClient, MyApplication.currentUser);

                if (loadType == LoadType.extend) { // extend case
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    } else {
                        if(plf.isMulti) submissions = subms.ofMultireddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                        else submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.adapter.getLastItem(), null, MyApplication.showHiddenPosts);
                    }
                    adapter = plf.adapter;
                } else { // init or refresh case
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    } else {
                        if(plf.isMulti) submissions = subms.ofMultireddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                        else submissions = subms.ofSubreddit(plf.subreddit, sort, time, -1, RedditConstants.DEFAULT_LIMIT, null, null, MyApplication.showHiddenPosts);
                    }
                    adapter = new RedditItemListAdapter(context, viewTypeValue, submissions);
                }
            }
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
                    plf.adapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init) {
                    plf.adapter = new RedditItemListAdapter(context, viewTypeValue);
                    plf.updateContentView(adapter, viewTypeValue);
                }
                if(MyApplication.offlineModeEnabled) ToastUtils.displayShortToast(context, "No posts found");
                else ToastUtils.postsLoadError(context);
            } else {
                if(submissions.size()>0) {
                    if(!MyApplication.noThumbnails) ImageLoader.preloadThumbnails(submissions, context); //TODO: used to throw indexoutofboundsexception in offline mode
                    plf.adapter = adapter;
                }
                plf.hasMore = submissions.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;

                switch (loadType) {
                    case init:
                        if(submissions.size()==0) {
                            plf.updateContentView(new RedditItemListAdapter(context, viewTypeValue), viewTypeValue);
                            String message = "No posts found";
                            if(MyApplication.hideNSFW) message = message.concat(" (NSFW filter is enabled)");
                            ToastUtils.displayShortToast(context, message);
                        }
                        else {
                            plf.updateContentView(plf.adapter, viewTypeValue);
                        }
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            if(changedSort) {
                                plf.submissionSort = sort;
                                plf.timeSpan = time;
                                //plf.setActionBarSubtitle();
                            }
                            //else if(MyApplication.offlineModeEnabled) {
                            //    plf.setActionBarSubtitle();
                            //}
                            plf.setActionBarSubtitle();
                            plf.updateContentView(adapter, viewTypeValue);
                        }
                        else ToastUtils.displayShortToast(context, "No posts found");
                        break;
                    case extend:
                        plf.adapter.setLoadingMoreItems(false);
                        plf.adapter.addAll(submissions);
                        plf.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
