package com.gDyejeekis.aliencompanion.utils;

import com.gDyejeekis.aliencompanion.enums.ImgurThumbnailSize;
import com.gDyejeekis.aliencompanion.enums.YoutubeThumbnailSize;

/**
 * Created by sound on 1/12/2016.
 */
public class ThumbnailUtils {

    public static final String BASE_YOUTUBE_THUMB_URL = "http://img.youtube.com/vi/";

    public static final String BASE_YOUTUBE_THUMB_URL_SHORT = "http://i3.ytimg.com/vi/";

    public static final String BASE_IMGUR_THUMB_URL = "http://i.imgur.com/";

    public static final String BASE_GFYCAT_THUMB_URL = "http://thumbs.gfycat.com/";

    public static String getYoutubeThumbnail(String url, YoutubeThumbnailSize size) {
        String id = LinkHandler.getYoutubeVideoId(url);

        return BASE_YOUTUBE_THUMB_URL_SHORT + id + "/" + size.value() + "default.jpg";
    }

    public static String getImgurThumbnail(String url, ImgurThumbnailSize size) {
        String id = LinkHandler.getImgurImgId(url);
        if (!id.equals("")) return BASE_IMGUR_THUMB_URL + id + size.value() + ".jpg";
        return "";
    }

    public static String getGfycatThumbnail(String url) {
        return BASE_GFYCAT_THUMB_URL + LinkHandler.getGfycatId(url) + "-thumb360.jpg";
    }
}
