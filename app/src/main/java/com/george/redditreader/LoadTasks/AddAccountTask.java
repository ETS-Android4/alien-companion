package com.george.redditreader.LoadTasks;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.SystemClock;

/**
 * Created by George on 8/18/2015.
 */
public class AddAccountTask extends AsyncTask<Void, Void, Void> {

    private DialogFragment dialogFragment;

    public AddAccountTask(DialogFragment dialogFragment) {
        this.dialogFragment = dialogFragment;
    }

    @Override
    protected Void doInBackground(Void... unused) {
        SystemClock.sleep(5000);
        dialogFragment.dismiss();
        return null;
    }
}
