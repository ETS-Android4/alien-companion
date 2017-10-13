package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by George on 10/1/2017.
 */

public class RedditVideoTask extends AsyncTask<String, Void, String> {

    private Context context;

    public RedditVideoTask(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }
}
