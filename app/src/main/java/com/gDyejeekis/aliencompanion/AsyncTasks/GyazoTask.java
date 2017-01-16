package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

/**
 * Created by George on 1/16/2017.
 */

public class GyazoTask extends AsyncTask<String, Void, String> {

    private Context context;

    public GyazoTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String url = params[0];
            return GeneralUtils.getGyazoRawUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Context getContext() {
        return context;
    }

}
