package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.GifFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageFragment;
import com.gDyejeekis.aliencompanion.R;
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
        ImgurItem item = null;
        final String url = params[0];
        try {
            String urlLC = url.toLowerCase();
            String id = LinkHandler.getImgurImgId(url);
            if (urlLC.contains("/a/")) {
                JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.ALBUM, id)).getResponseObject();
                JSONObject object = (JSONObject) response.get("data");
                item = new ImgurAlbum(object);
            } else if (urlLC.contains("/gallery/")) {
                JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.GALLERY, id)).getResponseObject();
                JSONObject object = (JSONObject) response.get("data");
                item = new ImgurGallery(object);
            } else {
                JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject();
                JSONObject object = (JSONObject) response.get("data");
                item = new ImgurImage(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public Context getContext() {
        return context;
    }

}
