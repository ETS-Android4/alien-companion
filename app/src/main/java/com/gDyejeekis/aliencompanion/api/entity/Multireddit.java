package com.gDyejeekis.aliencompanion.api.entity;

import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToDouble;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToLong;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 1/24/2016.
 */
public class Multireddit {

    private String weightingScheme;
    private String iconName;
    private Boolean canEdit;
    private String displayName;
    private String name;
    private String copiedFrom;
    private String iconUrl;
    private List<String> subreddits;
    private Double createdUTC;
    private Double created;
    private String keyColor;
    private String visibility;
    private String path;
    private String descriptionHtml;
    private String description;

    public Multireddit(JSONObject obj) {
        try {
            setCanEdit(safeJsonToBoolean(obj.get("can_edit")));
            setDisplayName(safeJsonToString(obj.get("display_name")));
            setName(safeJsonToString(obj.get("name")));
            setCopiedFrom(safeJsonToString(obj.get("copied_from")));
            setIconUrl(safeJsonToString(obj.get("icon_url")));

            subreddits = new ArrayList<>();
            JSONArray subredditArray = (JSONArray) obj.get("subreddits");
            for(Object o : subredditArray) {
                JSONObject subredditObject = (JSONObject) o;
                subreddits.add(safeJsonToString(subredditObject.get("name")));
            }

            setCreatedUTC(safeJsonToDouble(obj.get("created_utc")));
            setCreated(safeJsonToDouble(obj.get("created")));
            setKeyColor(safeJsonToString(obj.get("key_color")));
            setVisibility(safeJsonToString(obj.get("visibility")));
            setIconName(safeJsonToString(obj.get("icon_name")));
            setWeightingScheme(safeJsonToString(obj.get("weighting_scheme")));
            setPath(safeJsonToString(obj.get("path")));
            setDescriptionHtml(safeJsonToString(obj.get("description_html")));
            setDescription(safeJsonToString(obj.get("description")));
        } catch (Exception e) {
            Log.e("Api error", "error creating multireddit");
            e.printStackTrace();
        }
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getWeightingScheme() {
        return weightingScheme;
    }

    public void setWeightingScheme(String weightingScheme) {
        this.weightingScheme = weightingScheme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getKeyColor() {
        return keyColor;
    }

    public void setKeyColor(String keyColor) {
        this.keyColor = keyColor;
    }

    public Double getCreated() {
        return created;
    }

    public void setCreated(Double created) {
        this.created = created;
    }

    public Double getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(Double createdUTC) {
        this.createdUTC = createdUTC;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

}
