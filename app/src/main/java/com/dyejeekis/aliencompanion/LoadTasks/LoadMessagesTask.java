package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.Fragments.MessageFragment;
import com.dyejeekis.aliencompanion.Fragments.SearchFragment;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.entity.Message;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Messages;
import com.dyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.dyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.dyejeekis.aliencompanion.enums.LoadType;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Created by sound on 10/10/2015.
 */
public class LoadMessagesTask extends AsyncTask<Void, Void, List<RedditItem>> {

    private Exception exception;
    private Context context;
    private MessageFragment mf;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private LoadType loadType;
    private RedditItemListAdapter adapter;
    private MessageCategory category;
    private MessageCategorySort sort;
    private boolean changedSort;

    public LoadMessagesTask(Context context, MessageFragment mf, LoadType loadType) {
        this.context = context;
        this.mf = mf;
        this.loadType = loadType;
        this.category = mf.category;
        this.sort = mf.sort;
        changedSort = false;
        //httpClient = new PoliteRedditHttpClient();
    }

    public LoadMessagesTask(Context context, MessageFragment mf, LoadType loadType, MessageCategory category, MessageCategorySort sort) {
        this.context = context;
        this.mf = mf;
        this.loadType = loadType;
        this.category = category;
        this.sort = sort;
        changedSort = true;
        //httpClient = new PoliteRedditHttpClient();
    }

    @Override
    public List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> messages;
            Messages msgRetrieval = new Messages(httpClient, MainActivity.currentUser);
            if(loadType == LoadType.extend) {
                messages = msgRetrieval.ofUser(this.category, this.sort, -1, RedditConstants.DEFAULT_LIMIT, (Message) mf.adapter.getLastItem(), null, true);
                adapter = mf.adapter;
            }
            else {
                messages = msgRetrieval.ofUser(this.category, this.sort, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                adapter = new RedditItemListAdapter(context, messages);
            }
            //ConvertUtils.preparePostsText(context, messages);
            return messages;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPostExecute(List<RedditItem> messages) {
        try {
            MessageFragment fragment = (MessageFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            mf = fragment;
            mf.currentLoadType = null;
            mf.mainProgressBar.setVisibility(View.GONE);
            mf.swipeRefreshLayout.setRefreshing(false);
            mf.contentView.setVisibility(View.VISIBLE);

            if (exception != null) {
                ToastUtils.displayShortToast(context, "Error loading messages");
                if(loadType == LoadType.extend) {
                    mf.adapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init){
                    mf.adapter = new RedditItemListAdapter(context);
                    mf.contentView.setAdapter(mf.adapter);
                }
            } else {
                if(messages.size()>0) mf.adapter = adapter;
                else ToastUtils.displayShortToast(context, "No messages found");
                mf.hasMore = messages.size() == RedditConstants.DEFAULT_LIMIT;
                switch (loadType) {
                    case init:
                        mf.contentView.setAdapter(mf.adapter);
                        //if(messages.size()==0) ToastUtils.displayShortToast(context, "No messages found");
                        break;
                    case refresh:
                        if(messages.size()!=0) {
                            if(changedSort) {
                                mf.category = category;
                                mf.sort = sort;
                                mf.setActionBarSubtitle();
                            }
                            mf.contentView.setAdapter(mf.adapter);
                        }
                        break;
                    case extend:
                        mf.adapter.setLoadingMoreItems(false);
                        mf.adapter.addAll(messages);
                        if(MainActivity.endlessPosts) mf.loadMore = true;
                        break;
                }
            }
        } catch (NullPointerException e) {}
    }
}
