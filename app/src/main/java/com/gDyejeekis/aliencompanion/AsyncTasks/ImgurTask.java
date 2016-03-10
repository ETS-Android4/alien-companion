package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

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
public class ImgurTask extends AsyncTask<Void, Void, ImgurItem> {

    private Activity activity;

    private String url;

    private ImgurHttpClient httpClient = new ImgurHttpClient();

    public ImgurTask(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    @Override
    protected ImgurItem doInBackground(Void... unused) {
        ImgurItem item = null;
        try {
            String urlLC = url.toLowerCase();
            String id = LinkHandler.getImgurImgId(url);
            if (urlLC.contains("/a/")) {

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

    @Override
    protected void onPostExecute(ImgurItem item) {
        if(item == null) {
            ToastUtils.displayShortToast(activity, "Error retrieving imgur info");
        }
        else if(item instanceof ImgurImage) {
            String directUrl = ((ImgurImage) item).getLink().replace("\\", "");
            //Log.d("geotest", directUrl);
            if(((ImgurImage)item).isAnimated()) {
                activity.getFragmentManager().beginTransaction().add(R.id.layout_fragment_holder, GifFragment.newInstance(directUrl), "gifFragment").commit();
            }
            else {
                activity.getFragmentManager().beginTransaction().add(R.id.layout_fragment_holder, ImageFragment.newInstance(directUrl), "imageFragment").commit();
            }
        }
        else if(item instanceof ImgurAlbum) {

        }
        else if(item instanceof ImgurGallery) {
            ImgurGallery gallery = (ImgurGallery) item;
            if(gallery.isAlbum()) {

            }
            else {
                String directUrl = gallery.getLink().replace("\\", "");
                activity.getFragmentManager().beginTransaction().add(R.id.layout_fragment_holder, ImageFragment.newInstance(directUrl), "imageFragment").commit();
            }
        }
    }
}
