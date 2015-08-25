package com.george.redditreader.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.NavDrawerAdapter;
import com.george.redditreader.Models.SavedAccount;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Subreddit;
import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.exception.ActionFailedException;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 8/25/2015.
 */
public class LoadUserTask extends AsyncTask<Void, Void, List<String>> {

    private Context context;
    //private HttpClient httpClient;
    private Exception exception;
    private NavDrawerAdapter adapter;

    public LoadUserTask(Context context, /*SavedAccount account, */NavDrawerAdapter adapter) {
        this.context = context;
        //httpClient = new RedditHttpClient();
        this.adapter = adapter;
        //MainActivity.currentUser = new User(httpClient, account.getUsername(), account.getPassword());
    }

    @Override
    protected List<String> doInBackground(Void... unused) {
        try {
            MainActivity.currentUser.connect();

            List<Subreddit> subreddits = MainActivity.currentUser.getSubscribed(0);
            List<String> subredditNames = new ArrayList<>();
            for(Subreddit subreddit : subreddits) {
                subredditNames.add(subreddit.getDisplayName());
            }
            return subredditNames;
        } catch (ActionFailedException | RetrievalFailedException | NullPointerException | ParseException | IOException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<String> subreddits) {
        if(exception != null) {
            ToastUtils.displayShortToast(context, "Error connecting to account");
        }
        else {
            ToastUtils.displayShortToast(context, "Logged in as " + MainActivity.currentUser.getUsername());
            adapter.updateSubredditItems(subreddits);
        }
    }
}
