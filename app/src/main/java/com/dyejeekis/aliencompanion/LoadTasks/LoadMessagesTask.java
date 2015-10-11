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
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.entity.Message;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Messages;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
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
    private HttpClient httpClient;
    private LoadType loadType;
    private RedditItemListAdapter adapter;

    public LoadMessagesTask(Context context, MessageFragment mf, LoadType loadType) {
        this.context = context;
        this.mf = mf;
        this.loadType = loadType;
        httpClient = new RedditHttpClient();
    }

    @Override
    public List<RedditItem> doInBackground(Void... unused) {
        try {
            List<RedditItem> messages;
            Messages msgRetrieval = new Messages(httpClient, MainActivity.currentUser);
            if(loadType == LoadType.extend) {
                messages = msgRetrieval.ofUser(mf.category, mf.sort, -1, RedditConstants.DEFAULT_LIMIT, (Message) mf.adapter.getLastItem(), null, true);
                adapter = mf.adapter;
            }
            else {
                messages = msgRetrieval.ofUser(mf.category, mf.sort, -1, RedditConstants.DEFAULT_LIMIT, null, null, true);
                adapter = new RedditItemListAdapter(context, messages);
            }
            return messages;
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPostExecute(List<RedditItem> messages) {
        MessageFragment.currentlyLoading = false;
        try {
            MessageFragment fragment = (MessageFragment) ((Activity) context).getFragmentManager().findFragmentByTag("listFragment");
            mf = fragment;
            mf.mainProgressBar.setVisibility(View.GONE);

            if (exception != null) {
                ToastUtils.displayShortToast(context, "Error loading messages");
                if(loadType == LoadType.extend) {
                    mf.adapter.setLoadingMoreItems(false);
                }
                else {
                    mf.adapter = new RedditItemListAdapter(context);
                    mf.contentView.setAdapter(mf.adapter);
                }
            } else {
                if(messages.size()>0) mf.adapter = adapter;
                else mf.adapter = new RedditItemListAdapter(context);

                if(messages.size()<RedditConstants.DEFAULT_LIMIT) mf.loadMore = false;
                else if(MainActivity.endlessPosts)  mf.loadMore = true;
                switch (loadType) {
                    case init:
                    case refresh:
                        if(messages.size()>0) {
                            mf.contentView.setAdapter(mf.adapter);
                            mf.contentView.setVisibility(View.VISIBLE);
                        }
                        break;
                    case extend:
                        mf.adapter.setLoadingMoreItems(false);
                        mf.adapter.addAll(messages);
                        break;
                }
            }
        } catch (NullPointerException e) {}
    }
}
