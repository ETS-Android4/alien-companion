package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.MessageFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Messages;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.LoadType;

import java.util.List;

/**
 * Created by sound on 10/10/2015.
 */
public class LoadMessagesTask extends AsyncTask<Void, Void, List<RedditItem>> {

    public static final String TAG = "LoadMessagesTask";

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
            Messages msgRetrieval = new Messages(httpClient, MyApplication.currentUser);
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
    protected void onCancelled(List<RedditItem> messages) {
        try {
            MessageFragment fragment = (MessageFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
            fragment.loadMore = MyApplication.endlessPosts;
        } catch (NullPointerException e) {}
    }

    @Override
    public void onPostExecute(List<RedditItem> messages) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(context);
        }
        try {
            MessageFragment fragment = (MessageFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("listFragment");
            mf = fragment;
            mf.currentLoadType = null;
            mf.mainProgressBar.setVisibility(View.GONE);
            mf.swipeRefreshLayout.setRefreshing(false);
            mf.contentView.setVisibility(View.VISIBLE);

            if (exception != null) {
                ToastUtils.showToast(context, "Error loading messages");
                if(loadType == LoadType.extend) {
                    mf.adapter.setLoadingMoreItems(false);
                }
                else if(loadType == LoadType.init){
                    mf.adapter = new RedditItemListAdapter(context);
                    mf.updateContentViewAdapter(mf.adapter);
                }
            } else {
                if(messages.size()>0) mf.adapter = adapter;
                else ToastUtils.showToast(context, "No messages found");
                mf.hasMore = messages.size() == RedditConstants.DEFAULT_LIMIT;
                switch (loadType) {
                    case init:
                        try {
                            markNewMessagesRead((Message) messages.get(0));
                        } catch (Exception e) {}
                        mf.updateContentViewAdapter(mf.adapter);
                        //if(messages.size()==0) ToastUtils.showToast(context, "No messages found");
                        break;
                    case refresh:
                        if(messages.size()!=0) {
                            if(changedSort) {
                                mf.category = category;
                                mf.sort = sort;
                                mf.setActionBarSubtitle();
                            }
                            mf.updateContentViewAdapter(mf.adapter);
                        }
                        break;
                    case extend:
                        mf.adapter.setLoadingMoreItems(false);
                        mf.adapter.addAll(messages);
                        mf.loadMore = MyApplication.endlessPosts;
                        break;
                }
            }
        } catch (NullPointerException e) {}
    }

    private void markNewMessagesRead(Message message) {
        if(message.isNew) {
            Log.d(TAG, "Marking new messages as read..");
            MyApplication.newMessages = false;
            MainActivity.notifyDrawerChanged = true;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putBoolean("newMessages", MyApplication.newMessages);
                    editor.commit();

                    MarkActions markActions = new MarkActions(httpClient);
                    markActions.readAllNewMessages();
                }
            });
        }
    }
}
