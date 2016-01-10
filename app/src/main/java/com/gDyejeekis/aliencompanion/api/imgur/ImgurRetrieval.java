package com.gDyejeekis.aliencompanion.api.imgur;

import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.ThumbnailSize;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 1/5/2016.
 */
public class ImgurRetrieval {

    public static final ThumbnailSize DEFAULT_THUMBNAIL_SIZE = ThumbnailSize.SMALL_THUMBNAIL;

    private ImgurHttpClient httpClient;
    private String id;
    private boolean isAlbum;

    public ImgurRetrieval(String url) {
        httpClient = new ImgurHttpClient();
        getUrlInfo(url, this.id, this.isAlbum);
    }

    private void getUrlInfo(String url, String id, boolean isAlbum) {
        String pattern = "imgur\\.com\\/(a\\/)?(\\w+)\\.?";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if(matcher.find()) {
            isAlbum = matcher.group(1).equals("a/");
            id = matcher.group(2);
        }
    }

    public List<Image> getImageInfo() {
        if(isAlbum) {
            Album album = new Album((JSONObject) httpClient.get(String.format(ImgurApiEndpoints.ALBUM, id)).getResponseObject());
            return album.getImages();
        }
        else {
            Image image = new Image((JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject());
            List<Image> list = new ArrayList<>();
            list.add(image);
            return list;
        }
    }

    public static String getThumbnailUrl(String id, ThumbnailSize size) {
        return "i.imgur.com/" + id + size.value() + ".png";
    }

}
