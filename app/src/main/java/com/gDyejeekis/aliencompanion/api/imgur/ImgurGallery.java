package com.gDyejeekis.aliencompanion.api.imgur;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 3/10/2016.
 */
public class ImgurGallery extends ImgurItem implements Serializable {

    private String id;
    private boolean isAlbum;
    private boolean isAnimated;
    private String mp4;
    private String link;
    private List<ImgurImage> images;

    public ImgurGallery(JSONObject obj) {
        id = safeJsonToString(obj.get("id"));
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
            isAnimated = safeJsonToBoolean(obj.get("animated"));
            if(isAnimated) {
                mp4 = safeJsonToString(obj.get("mp4"));
            }
        }
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public String getMp4() {
        return mp4;
    }

    public String getId() {
        return id;
    }

    public void setImages(List<ImgurImage> images) {
        this.images = images;
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
