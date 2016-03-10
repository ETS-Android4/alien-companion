package com.gDyejeekis.aliencompanion.api.imgur;

import org.json.simple.JSONObject;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 3/10/2016.
 */
public class ImgurGallery extends ImgurItem {

    private boolean isAlbum;
    private String link;

    public ImgurGallery(JSONObject obj) {
        isAlbum = safeJsonToBoolean(obj.get("is_album"));
        if(isAlbum) {

        }
        else {
            link = safeJsonToString(obj.get("link"));
        }
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public String getLink() {
        return link;
    }
}
