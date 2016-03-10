package com.gDyejeekis.aliencompanion.api.imgur;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 3/10/2016.
 */
public class ImgurGallery extends ImgurItem {

    private boolean isAlbum;
    private String link;
    private List<ImgurImage> images;

    public ImgurGallery(JSONObject obj) {
        isAlbum = safeJsonToBoolean(obj.get("is_album"));
        if(isAlbum) {
            images = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) obj.get("images");
            for(Object object : jsonArray) {
                images.add(new ImgurImage((JSONObject) object));
            }
        }
        else {
            link = safeJsonToString(obj.get("link"));
            link = link.replace("\\", "");
        }
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public String getLink() {
        return link;
    }

    public List<ImgurImage> getImages() {
        return images;
    }
}
