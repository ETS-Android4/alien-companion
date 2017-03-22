package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.utils.SnackbarUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.utils.ImageLoader;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.entity.Thing;
import com.gDyejeekis.aliencompanion.api.entity.UserInfo;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.retrieval.Submissions;
import com.gDyejeekis.aliencompanion.api.retrieval.UserDetails;
import com.gDyejeekis.aliencompanion.api.retrieval.UserMixed;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

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
    private HttpClient httpClient = new RedditHttpClient();
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
        changedSort = false;
    }

    public LoadUserContentTask(Activity activity, UserFragment userFragment, LoadType loadType, UserSubmissionsCategory category, UserOverviewSort sort) {
        this.activity = activity;
        this.uf = userFragment;
        mLoadType = loadType;
        this.userCategory = category;
        this.userSort = sort;
        changedSort = true;
    }

    @Override
    protected List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> userContent = null;
            switch (this.userCategory) {
                case OVERVIEW: case GILDED: case SAVED:
                    UserMixed userMixed = new UserMixed(httpClient, MyApplication.currentUser);
                    if(mLoadType == LoadType.extend) {
                        RedditItem lastItem = uf.adapter.getLastItem();
                        userContent = userMixed.ofUser(uf.username, this.userCategory, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, (Thing) lastItem, null, false);
                        adapter = uf.adapter;
                    }
                    else {
                        UserDetails userDetails = new UserDetails(httpClient, MyApplication.currentUser);
                        UserInfo userInfo = userDetails.ofUser(uf.username);
                        userInfo.retrieveTrophyInfo(httpClient); //retrieves trophy info

                        userContent = userMixed.ofUser(uf.username, this.userCategory, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);
                        adapter = new RedditItemListAdapter(activity);
                        if(this.userCategory == UserSubmissionsCategory.OVERVIEW) adapter.add(userInfo);
                        adapter.addAll(userContent);
                    }
                    //ImageLoader.preloadUserImages(userContent, activity);
                    break;
                case COMMENTS:
                    Comments comments = new Comments(httpClient, MyApplication.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Comment lastComment = (Comment) uf.adapter.getLastItem();
                        userContent = comments.ofUser(uf.username, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, lastComment, null, true);
                        adapter = uf.adapter;
                    }
                    else {
                        userContent = comments.ofUser(uf.username, this.userSort, TimeSpan.ALL, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    break;
                case SUBMITTED: case LIKED: case DISLIKED: case HIDDEN:
                    Submissions submissions = new Submissions(httpClient, MyApplication.currentUser);
                    if(mLoadType == LoadType.extend) {
                        Submission lastPost = (Submission) uf.adapter.getLastItem();
                        userContent = submissions.ofUser(uf.username, this.userCategory, this.userSort, -1, RedditConstants.DEFAULT_LIMIT, lastPost, null, false);
                        adapter = uf.adapter;
                    }
                    else {
                        userContent = submissions.ofUser(uf.username, this.userCategory, this.userSort, -1, RedditConstants.DEFAULT_LIMIT, null, null, false);
                        adapter = new RedditItemListAdapter(activity);
                        adapter.addAll(userContent);
                    }
                    //ImageLoader.preloadUserImages(userContent, activity);
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
    protected void onCancelled(List<RedditItem> submissions) {
        try {
            UserFragment fragment = (UserFragment) activity.getFragmentManager().findFragmentByTag("listFragment");
            fragment.loadMore = MyApplication.endlessPosts;
        } catch (NullPointerException e) {}
    }

    @Override
    protected void onPostExecute(List<RedditItem> things) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(activity);
        }
        try {
            UserFragment userFragment = (UserFragment) activity.getFragmentManager().findFragmentByTag("listFragment");
            uf = userFragment;
            uf.currentLoadType = null;
            uf.mainProgressBar.setVisibility(View.GONE);
            uf.swipeRefreshLayout.setRefreshing(false);
            uf.contentView.setVisibility(View.VISIBLE);

            if (mException != null) {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uf.refreshList();
                    }
                };
                SnackbarUtils.showSnackbar(uf.getSnackbarParentView(), "Error loading user", "Retry", listener, Snackbar.LENGTH_INDEFINITE);

                if (mLoadType == LoadType.extend) {
                    uf.adapter.setLoadingMoreItems(false);
                }
                else if(mLoadType == LoadType.init){
                    uf.adapter = new RedditItemListAdapter(activity);
                    uf.updateContentViewAdapter(uf.adapter);
                }
            }
            else {
                if(things.size()>0) {
                    //load trophy images here
                    if(!MyApplication.noThumbnails) ImageLoader.preloadUserImages(things, activity);
                    uf.adapter = adapter;
                }
                else {
                    ToastUtils.showToast(activity, "No posts found");
                }
                uf.hasMore = things.size() >= RedditConstants.DEFAULT_LIMIT - Submissions.postsSkipped;
                switch (mLoadType) {
                    case init:
                        uf.updateContentViewAdapter(uf.adapter);
                        break;
                    case refresh:
                        if (things.size() != 0) {
                            if(changedSort) {
                                uf.userContent = userCategory;
                                uf.userOverviewSort = userSort;
                                uf.setActionBarSubtitle();
                                try {
                                    ((UserActivity) activity).setAddToSyncedVisible(userCategory == UserSubmissionsCategory.SAVED);
                                } catch (Exception e) {}
                            }
                            uf.updateContentViewAdapter(uf.adapter);
                        }
                        break;
                    case extend:
                        uf.adapter.setLoadingMoreItems(false);
                        uf.adapter.addAll(things);
                        uf.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
