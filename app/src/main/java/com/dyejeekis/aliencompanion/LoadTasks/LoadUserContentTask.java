package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.Fragments.UserFragment;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.Utils.ImageLoader;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.entity.Thing;
import com.dyejeekis.aliencompanion.api.entity.UserInfo;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Comments;
import com.dyejeekis.aliencompanion.api.retrieval.Submissions;
import com.dyejeekis.aliencompanion.api.retrieval.UserDetails;
import com.dyejeekis.aliencompanion.api.retrieval.UserMixed;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadUserContentTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception mException;
    private LoadType mLoadType;
    private UserSubmissionsCategory userContent;
    private Activity activity;
    private UserFragment uf;
    private HttpClient httpClient;
    private RedditItemListAdapter adapter;

    public LoadUserContentTask(Activity activity, UserFragment userFragment, LoadType loadType, UserSubmissionsCategory userContent) {
        this.activity = activity;
        this.uf = userFragment;
        this.userContent = userContent;
        mLoadType = loadType;
        httpClient = new RedditHttpClient();
        //adapter = new RedditItemListAdapter(activity);
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> userContent = null;
            switch (this.userContent) {
                case OVERVIEW: case GILDED: case SAVED:
                    UserMixed userMixed = new UserMixed(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        RedditItem lastItem = uf.userAdapter.getLastItem();
                        userContent = userMixed.ofUser(uf.username, this.userContent, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, (Thing) lastItem, null, false);

                        //uf.userAdapter.addAll(userContent);
                        adapter = uf.userAdapter;
                        //adapter.addAll(userContent); //NOTE TO SELF: dont call notifydatasetchanged() on background thread
                    }
                    else {
                        UserDetails userDetails = new UserDetails(httpClient, MainActivity.currentUser);
                        UserInfo userInfo = userDetails.ofUser(uf.username);
                        userInfo.retrieveTrophies(activity, httpClient);

                        userContent = userMixed.ofUser(uf.username, this.userContent, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);

                        //uf.userAdapter = new RedditItemListAdapter(activity);
                        //if(this.userContent == UserSubmissionsCategory.OVERVIEW) uf.userAdapter.add(userInfo);
                        //uf.userAdapter.addAll(userContent);
                        adapter = new RedditItemListAdapter(activity);
                        if(this.userContent == UserSubmissionsCategory.OVERVIEW) adapter.add(userInfo);
                        adapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
                case COMMENTS:
                    Comments comments = new Comments(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Comment lastComment = (Comment) uf.userAdapter.getLastItem();
                        userContent = comments.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, lastComment, null, true);

                        //uf.userAdapter.addAll(userContent);
                        adapter = uf.userAdapter;
                        //adapter.addAll(userContent);
                    }
                    else {
                        userContent = comments.ofUser(uf.username, uf.userOverviewSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);

                        //uf.userAdapter = new RedditItemListAdapter(activity);
                        //uf.userAdapter.addAll(userContent);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    break;
                case SUBMITTED: case LIKED: case DISLIKED: case HIDDEN:
                    Submissions submissions = new Submissions(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Submission lastPost = (Submission) uf.userAdapter.getLastItem();
                        userContent = submissions.ofUser(uf.username, this.userContent, uf.userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, lastPost, null, false);

                        //uf.userAdapter.addAll(userContent);
                        adapter = uf.userAdapter;
                        //adapter.addAll(userContent);
                    }
                    else {
                        userContent = submissions.ofUser(uf.username, this.userContent, uf.userOverviewSort, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);

                        //uf.userAdapter = new RedditItemListAdapter(activity);
                        //uf.userAdapter.addAll(userContent);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
            }
            return userContent;
        } catch (RetrievalFailedException | RedditError | NullPointerException | ClassCastException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<RedditItem> things) {
        UserFragment.currentlyLoading = false;
        try {
            UserFragment userFragment = (UserFragment) activity.getFragmentManager().findFragmentByTag("listFragment");
            uf = userFragment;
            uf.progressBar.setVisibility(View.GONE);

            if (mException != null) {
                ToastUtils.userLoadError(activity);
                if (mLoadType == LoadType.extend) {
                    uf.userAdapter.setLoadingMoreItems(false);
                }
                else {
                    uf.userAdapter = new RedditItemListAdapter(activity);
                    uf.contentView.setAdapter(uf.userAdapter);
                }
            } else {
                if(things.size()>0) uf.userAdapter = adapter;
                //else uf.userAdapter = new RedditItemListAdapter(activity);
                uf.hasMore = things.size() == RedditConstants.DEFAULT_LIMIT;
                switch (mLoadType) {
                    case init:
                        //uf.progressBar.setVisibility(View.GONE);
                        uf.contentView.setAdapter(uf.userAdapter);
                        if (things.size() < RedditConstants.DEFAULT_LIMIT) uf.hasMore = false;
                        break;
                    case refresh:
                        if (things.size() != 0) {
                            //uf.progressBar.setVisibility(View.GONE);
                            uf.contentView.setAdapter(uf.userAdapter);
                            uf.contentView.setVisibility(View.VISIBLE);
                        }
                        if (things.size() == RedditConstants.DEFAULT_LIMIT) uf.hasMore = true;
                        break;
                    case extend:
                        uf.userAdapter.setLoadingMoreItems(false);
                        uf.userAdapter.addAll(things);
                        //if (!(things.size() == RedditConstants.DEFAULT_LIMIT)) uf.hasMore = false;
                        if (MainActivity.endlessPosts) uf.loadMore = true;
                        break;
                }
            }
        } catch (NullPointerException e) {}
    }
}
