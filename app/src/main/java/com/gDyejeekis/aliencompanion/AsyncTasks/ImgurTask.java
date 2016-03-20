package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.GifFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageFragment;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurApiEndpoints;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import org.json.simple.JSONObject;

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
