package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.Fragments.UserFragment;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
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
    //private UserSubmissionsCategory userContent;
    private Activity activity;
    private UserFragment uf;
    private HttpClient httpClient;
    private RedditItemListAdapter adapter;
    private UserSubmissionsCategory userCategory;
    private UserOverviewSort userSort;
    private boolean changedSort;

    public LoadUserContentTask(Activity activity, UserFragment userFragment, LoadType loadType) {
        this.activity = activity;
        this.uf = userFragment;
        mLoadType = loadType;
        this.userCategory = uf.userContent;
        this.userSort = uf.userOverviewSort;
        httpClient = new RedditHttpClient();
        changedSort = false;
    }

    public LoadUserContentTask(Activity activity, UserFragment userFragment, LoadType loadType, UserSubmissionsCategory category, UserOverviewSort sort) {
        this.activity = activity;
        this.uf = userFragment;
        mLoadType = loadType;
        this.userCategory = category;
        this.userSort = sort;
        httpClient = new RedditHttpClient();
        changedSort = true;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> userContent = null;
            switch (this.userCategory) {
                case OVERVIEW: case GILDED: case SAVED:
                    UserMixed userMixed = new UserMixed(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        RedditItem lastItem = uf.userAdapter.getLastItem();
                        userContent = userMixed.ofUser(uf.username, this.userCategory, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, (Thing) lastItem, null, false);
                        adapter = uf.userAdapter;
                    }
                    else {
                        UserDetails userDetails = new UserDetails(httpClient, MainActivity.currentUser);
                        UserInfo userInfo = userDetails.ofUser(uf.username);
                        userInfo.retrieveTrophies(activity, httpClient);

                        userContent = userMixed.ofUser(uf.username, this.userCategory, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);
                        adapter = new RedditItemListAdapter(activity);
                        if(this.userCategory == UserSubmissionsCategory.OVERVIEW) adapter.add(userInfo);
                        adapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
                case COMMENTS:
                    Comments comments = new Comments(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Comment lastComment = (Comment) uf.userAdapter.getLastItem();
                        userContent = comments.ofUser(uf.username, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, lastComment, null, true);
                        adapter = uf.userAdapter;
                    }
                    else {
                        userContent = comments.ofUser(uf.username, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    break;
                case SUBMITTED: case LIKED: case DISLIKED: case HIDDEN:
                    Submissions submissions = new Submissions(httpClient, MainActivity.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Submission lastPost = (Submission) uf.userAdapter.getLastItem();
                        userContent = submissions.ofUser(uf.username, this.userCategory, this.userSort, -1, RedditConstants.DEFAULT_LIMIT, lastPost, null, false);
                        adapter = uf.userAdapter;
                    }
                    else {
                        userContent = submissions.ofUser(uf.username, this.userCategory, this.userSort, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    ImageLoader.preloadUserImages(userContent, activity);
                    break;
            }
            //ConvertUtils.preparePostsText(activity, userContent);
            return userContent;
        } catch (RetrievalFailedException | RedditError | NullPointerException | ClassCastException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<RedditItem> things) {
        try {
            UserFragment userFragment = (UserFragment) activity.getFragmentManager().findFragmentByTag("listFragment");
            uf = userFragment;
            uf.currentLoadType = null;
            uf.progressBar.setVisibility(View.GONE);
            uf.swipeRefreshLayout.setRefreshing(false);
            uf.contentView.setVisibility(View.VISIBLE);

            if (mException != null) {
                ToastUtils.userLoadError(activity);
                if (mLoadType == LoadType.extend) {
                    uf.userAdapter.setLoadingMoreItems(false);
                }
                else if(mLoadType == LoadType.init){
                    uf.userAdapter = new RedditItemListAdapter(activity);
                    uf.contentView.setAdapter(uf.userAdapter);
                }
            } else {
                if(things.size()>0) uf.userAdapter = adapter;
                else ToastUtils.displayShortToast(activity, "No posts found");
                uf.hasMore = things.size() >= RedditConstants.DEFAULT_LIMIT;
                switch (mLoadType) {
                    case init:
                        uf.contentView.setAdapter(uf.userAdapter);
                        //if(things.size() == 0) ToastUtils.displayShortToast(activity, "User not found");
                        break;
                    case refresh:
                        if (things.size() != 0) {
                            if(changedSort) {
                                uf.userContent = userCategory;
                                uf.userOverviewSort = userSort;
                                uf.setActionBarSubtitle();
                            }
                            uf.contentView.setAdapter(uf.userAdapter);
                        }
                        break;
                    case extend:
                        uf.userAdapter.setLoadingMoreItems(false);
                        uf.userAdapter.addAll(things);
                        if (MainActivity.endlessPosts) uf.loadMore = true;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
