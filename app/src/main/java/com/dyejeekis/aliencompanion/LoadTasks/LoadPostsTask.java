package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.Fragments.PostListFragment;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
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

    public LoadPostsTask(Context context, PostListFragment plf, LoadType loadType) {
        this.context = context;
        this.plf = plf;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
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
                        submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, (Submission) plf.postListAdapter.getLastItem(), null, MainActivity.showHiddenPosts);
                    }
                    adapter = plf.postListAdapter;
                } else {
                    if (plf.subreddit == null) {
                        submissions = subms.frontpage(plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                    } else {
                        submissions = subms.ofSubreddit(plf.subreddit, plf.submissionSort, plf.timeSpan, -1, RedditConstants.DEFAULT_LIMIT, null, null, MainActivity.showHiddenPosts);
                    }
                    //plf.postListAdapter = new RedditItemListAdapter(context, submissions);
                    adapter = new RedditItemListAdapter(context, submissions);
                }
                ImageLoader.preloadThumbnails(submissions, context); //TODO: fix image preloading
            }
            return submissions;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<RedditItem> submissions) {
        PostListFragment.currentlyLoading = false;
        try {
            PostListFragment fragment = (PostListFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            plf = fragment;
            plf.postListAdapter = adapter;
            plf.mainProgressBar.setVisibility(View.GONE);

            if (exception != null || submissions == null) {
                if (MainActivity.offlineModeEnabled)
                    ToastUtils.displayShortToast(context, "No posts found");
                else {
                    ToastUtils.postsLoadError(context);
                    if (loadType == LoadType.extend) {
                        plf.postListAdapter.setLoadingMoreItems(false);
                    }
                }
            } else {
                switch (loadType) {
                    case init:
                        plf.contentView.setAdapter(plf.postListAdapter);
                        plf.hasPosts = true;
                        break;
                    case refresh:
                        if (submissions.size() != 0) {
                            plf.contentView.setAdapter(plf.postListAdapter);
                            plf.contentView.setVisibility(View.VISIBLE);
                            plf.hasPosts = true;
                        } else {
                            plf.hasPosts = false;
                            ToastUtils.subredditNotFound(context);
                        }
                        break;
                    case extend:
                        plf.postListAdapter.setLoadingMoreItems(false);
                        plf.postListAdapter.addAll(submissions);
                        if (MainActivity.endlessPosts) plf.loadMore = true;
                        break;
                }
            }
        } catch (NullPointerException e) {}
    }

}
