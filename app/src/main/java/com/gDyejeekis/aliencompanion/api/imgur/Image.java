package com.gDyejeekis.aliencompanion.api.imgur;

import com.gDyejeekis.aliencompanion.enums.ImgurThumbnailSize;

import org.json.simple.JSONObject;

import java.io.Serializable;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/3/2016.
 */
public class Image extends ImgurItem {

    private boolean nsfw;
    private String gifv;
    private String link;
    private String section;
    private int bandwidth;
    private int views;
    private int size;
    private int height;
    private int width;
    private boolean animated;
    private String type;
    private int datetime;
    private String description;
    private String id;
    private String title;

    public Image(JSONObject obj) {
        setTitle(safeJsonToString(obj.get("title")));
        setId(safeJsonToString(obj.get("id")));
        setDescription(safeJsonToString(obj.get("description")));
        setDatetime(safeJsonToInteger(obj.get("datetime")));
        setType(safeJsonToString(obj.get("type")));
        setAnimated(safeJsonToBoolean(obj.get("animated")));
        setWidth(safeJsonToInteger(obj.get("width")));
        setHeight(safeJsonToInteger(obj.get("height")));
        setSize(safeJsonToInteger(obj.get("size")));
        setViews(safeJsonToInteger(obj.get("views")));
        setBandwidth(safeJsonToInteger(obj.get("bandwidth")));
        setSection(safeJsonToString(obj.get("section")));
        setLink(safeJsonToString(obj.get("link")));
        if(animated) setGifv(safeJsonToString(obj.get("gifv")));
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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

    public String getGifv() {
        return gifv;
    }

    public void setGifv(String gifv) {
        this.gifv = gifv;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public String getThumbnailId(ImgurThumbnailSize size) {
        return id + size.value();
    }

}
