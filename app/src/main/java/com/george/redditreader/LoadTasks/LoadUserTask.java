package com.george.redditreader.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.NavDrawerAdapter;
import com.george.redditreader.Models.NavDrawer.NavDrawerMenuItem;
import com.george.redditreader.Models.SavedAccount;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Subreddit;
import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.exception.ActionFailedException;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;
import com.george.redditreader.enums.MenuType;

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
    private boolean changedUser;

    public LoadUserTask(Context context, /*SavedAccount account, */NavDrawerAdapter adapter) {
        this.context = context;
        //httpClient = new RedditHttpClient();
        this.adapter = adapter;
        //MainActivity.currentUser = new User(httpClient, account.getUsername(), account.getPassword());
        changedUser = false;
    }

    @Override
    protected List<String> doInBackground(Void... unused) {
        try {
            if(MainActivity.currentUser == null) MainActivity.currentUser = new User(new RedditHttpClient(), MainActivity.currentAccount.getUsername(), MainActivity.currentAccount.getPassword());
            else changedUser = true;
            MainActivity.currentUser.connect(); //user connects every time main activity is started

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
            if(exception instanceof NullPointerException) ToastUtils.displayShortToast(context, "Error getting subscribed reddits");
            else ToastUtils.displayShortToast(context, "Error logging in");
        }
        else {
            if(changedUser) ToastUtils.displayShortToast(context, "Logged in as " + MainActivity.currentUser.getUsername());
            adapter.updateSubredditItems(subreddits);
            adapter.add(1, new NavDrawerMenuItem(MenuType.profile));
            adapter.add(2, new NavDrawerMenuItem(MenuType.messages));
        }
    }
}
