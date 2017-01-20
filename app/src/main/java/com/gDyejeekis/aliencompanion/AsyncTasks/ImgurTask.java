package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

/**
 * Created by sound on 3/9/2016.
 */
public class ImgurTask extends AsyncTask<String, Void, ImgurItem> {

    private Context context;

    private ImgurHttpClient httpClient = new ImgurHttpClient();

    public ImgurTask(Context context) {
        this.context = context;
    }

    @Override
    protected ImgurItem doInBackground(String... params) {
        final String url = params[0];
        try {
            return GeneralUtils.getImgurDataFromUrl(httpClient, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

}
