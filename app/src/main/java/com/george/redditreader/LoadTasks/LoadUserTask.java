package com.george.redditreader.LoadTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Adapters.UserAdapter;
import com.george.redditreader.Fragments.UserFragment;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.enums.LoadType;
import com.george.redditreader.enums.UserContent;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.entity.Thing;
import com.george.redditreader.api.entity.UserInfo;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Comments;
import com.george.redditreader.api.retrieval.Submissions;
import com.george.redditreader.api.retrieval.UserDetails;
import com.george.redditreader.api.retrieval.UserOverview;
import com.george.redditreader.api.retrieval.params.TimeSpan;
import com.george.redditreader.api.retrieval.params.UserSubmissionsCategory;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadUserTask extends AsyncTask<Void, Void, List<Object>> {

    private Exception mException;
    private LoadType mLoadType;
    private UserContent userContent;
    private Activity activity;
    private UserFragment uf;
    private HttpClient httpClient;

    public LoadUserTask(Activity activity, UserFragment userFragment, LoadType loadType, UserContent userContent) {
        this.activity = activity;
        this.uf = userFragment;
        this.userContent = userContent;
        mLoadType = loadType;
        httpClient = new RedditHttpClient();
    }

    @Override
    protected List<Object> doInBackground(Void... unused) {
        try {
            List<Object> userContent = null;
            switch (this.userContent) {
                case overview:
                    UserOverview userOverview = new UserOverview(httpClient);
                    if(mLoadType == LoadType.extend) {
                        Object lastObject = uf.userAdapter.getLastObject();
                        userContent = userOverview.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, (Thing) lastObject, null, true);

                        uf.userAdapter.addAll(userContent);
                    }
                    else {
                        UserDetails userDetails = new UserDetails(httpClient);
                        UserInfo userInfo = userDetails.ofUser(uf.username);
                        userInfo.retrieveTrophies(activity, httpClient);

                        userContent = userOverview.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);

                        uf.userAdapter = new UserAdapter(activity);
                        uf.userAdapter.add(userInfo);
                        uf.userAdapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
                case comments:
                    Comments comments = new Comments(httpClient);
                    if(mLoadType == LoadType.extend) {
                        Comment lastComment = (Comment) uf.userAdapter.getLastObject();
                        userContent = (List<Object>) (List<?>) comments.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, lastComment, null, true);

                        uf.userAdapter.addAll(userContent);
                    }
                    else {
                        userContent = (List<Object>) (List<?>) comments.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);

                        uf.userAdapter = new UserAdapter(activity);
                        uf.userAdapter.addAll(userContent);
                    }
                    break;
                case submitted:
                    Submissions submissions = new Submissions(httpClient);
                    if(mLoadType == LoadType.extend) {
                        Submission lastPost = (Submission) uf.userAdapter.getLastObject();
                        userContent = (List<Object>) (List<?>) submissions.ofUser(uf.username, UserSubmissionsCategory.SUBMITTED, uf.userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, lastPost, null, true);

                        uf.userAdapter.addAll(userContent);
                    }
                    else {
                        userContent = (List<Object>) (List<?>) submissions.ofUser(uf.username, UserSubmissionsCategory.SUBMITTED, uf.userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);

                        uf.userAdapter = new UserAdapter(activity);
                        uf.userAdapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
            }
            return userContent;
        } catch (RetrievalFailedException e) {
            mException = e;
        } catch (RedditError e) {
            mException = e;
        } catch (NullPointerException e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Object> things) {
        if(mException != null) {
            ToastUtils.userLoadError(activity);
            if(mLoadType == LoadType.extend) {
                uf.footerProgressBar.setVisibility(View.GONE);
                uf.showMore.setVisibility(View.VISIBLE);
            }
        }
        else {
            switch (mLoadType) {
                case init:
                    uf.progressBar.setVisibility(View.GONE);
                    uf.contentView.setAdapter(uf.userAdapter);
                    break;
                case refresh:
                    if(things.size() != 0) {
                        uf.progressBar.setVisibility(View.GONE);
                        uf.contentView.setAdapter(uf.userAdapter);
                        uf.contentView.setVisibility(View.VISIBLE);
                    }
                    break;
                case extend:
                    uf.footerProgressBar.setVisibility(View.GONE);
                    uf.showMore.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
