package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SubredditSidebarDialogFragment;
import com.gDyejeekis.aliencompanion.api.entity.SubredditInfo;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;

/**
 * Created by sound on 2/29/2016.
 */
public class LoadSubredditSidebarTask extends AsyncTask<Void, Void, SubredditInfo> {

    private String subreddit;
    private SubredditSidebarDialogFragment dialog;
    private Exception exception;

    public LoadSubredditSidebarTask(String subreddit, SubredditSidebarDialogFragment dialog) {
        this.subreddit = subreddit;
        this.dialog = dialog;
    }

    @Override
    protected SubredditInfo doInBackground(Void... unused) {
        try {

        } catch (RetrievalFailedException | RedditError e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(SubredditInfo subredditInfo) {

    }
}
