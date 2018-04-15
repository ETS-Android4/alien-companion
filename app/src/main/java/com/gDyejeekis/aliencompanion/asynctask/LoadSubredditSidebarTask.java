package com.gDyejeekis.aliencompanion.asynctask;

import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SubredditSidebarDialogFragment;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.SubredditInfo;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import org.json.simple.JSONObject;

/**
 * Created by sound on 2/29/2016.
 */
public class LoadSubredditSidebarTask extends AsyncTask<Void, Void, SubredditInfo> {

    private String subreddit;
    private SubredditSidebarDialogFragment dialog;
    private Exception exception;
    private HttpClient httpClient;

    public LoadSubredditSidebarTask(String subreddit, SubredditSidebarDialogFragment dialog) {
        this.subreddit = subreddit;
        this.dialog = dialog;
        httpClient = new PoliteRedditHttpClient();
    }

    @Override
    protected SubredditInfo doInBackground(Void... unused) {
        try {
            String url = String.format(ApiEndpointUtils.SUBREDDIT_ABOUT, subreddit);
            Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, null).getResponseObject();
            if(response instanceof JSONObject) {
                JSONObject object = (JSONObject) response;
                return new SubredditInfo((JSONObject) object.get("data"));
            }
        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(SubredditInfo subredditInfo) {
        if(exception != null) {
            dialog.dismissAllowingStateLoss();
            ToastUtils.showToast(dialog.getActivity(), "Error retrieving subreddit sidebar");
        }
        else {
            dialog.bindData(subredditInfo);
        }
    }

}
