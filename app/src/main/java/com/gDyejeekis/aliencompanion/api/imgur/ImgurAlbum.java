package com.gDyejeekis.aliencompanion.api.imgur;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/3/2016.
 */
public class ImgurAlbum extends ImgurItem {

    private String layout;
    private boolean nsfw;
    private String link;
    private String section;
    private int bandwidth;
    private int views;
    private String cover;
    private int coverHeight;
    private int coverWidth;
    private int datetime;
    private String description;
    private String id;
    private String title;
    private int imageCount;
    private List<ImgurImage> images;

    public ImgurAlbum(JSONObject obj) {
        setTitle(safeJsonToString(obj.get("title")));
        setId(safeJsonToString(obj.get("id")));
        setDescription(safeJsonToString(obj.get("description")));
        setDatetime(safeJsonToInteger(obj.get("datetime")));
        setCoverWidth(safeJsonToInteger(obj.get("cover_width")));
        setCoverHeight(safeJsonToInteger(obj.get("cover_height")));
        setCover(safeJsonToString(obj.get("cover")));
        setViews(safeJsonToInteger(obj.get("views")));
        setBandwidth(safeJsonToInteger(obj.get("bandwidth")));
        setSection(safeJsonToString(obj.get("section")));
        setLink(safeJsonToString(obj.get("link")));
        setNsfw(safeJsonToBoolean(obj.get("nsfw")));
        setLayout(safeJsonToString(obj.get("layout")));
        JSONArray jsonArray = (JSONArray) obj.get("images");
        images = new ArrayList<>();
        for(Object object : jsonArray) {
            images.add(new ImgurImage((JSONObject) object));
        }
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public List<ImgurImage> getImages() {
        return images;
    }

    public void setImgurImages(List<ImgurImage> imgurImages) {
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDatetime() {
        return datetime;
    }

    public void setDatetime(int datetime) {
        this.datetime = datetime;
    }

    public int getCoverWidth() {
        return coverWidth;
    }

    public void setCoverWidth(int width) {
        this.coverWidth = width;
    }

    public int getCoverHeight() {
        return coverHeight;
    }

    public void setCoverHeight(int height) {
        this.coverHeight = height;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
