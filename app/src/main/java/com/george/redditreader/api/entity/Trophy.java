package com.george.redditreader.api.entity;

import org.json.simple.JSONObject;
import static com.george.redditreader.api.utils.httpClient.JsonUtils.safeJsonToString;

/**
 * Created by George on 6/20/2015.
 */
public class Trophy {

    private String description;
    private String icon40url;
    private String icon70url;
    private String redditUrl;
    private String awardId;
    private String id;
    private String name;

    public Trophy() {

    }

    public Trophy(JSONObject object) {
        try {
            setDescription(safeJsonToString(object.get("description")));
            setIcon40url(safeJsonToString(object.get("icon_40")));
            setIcon70url(safeJsonToString(object.get("icon_70")));
            setRedditUrl(safeJsonToString(object.get("url")));
            setAwardId(safeJsonToString(object.get("award_id")));
            setId(safeJsonToString(object.get("id")));
            setName(safeJsonToString(object.get("name")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("JSON Object could not be parsed into a Trophy. Provide a JSON Object with a valid structure.");
        }
    }

    public String getDescription() {
        return description;
    }

    public String getIcon40url() {
        return icon40url;
    }

    public String getIcon70url() {
        return icon70url;
    }

    public String getRedditUrl() {
        return redditUrl;
    }

    public String getAwardId() {
        return awardId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String string) {
        this.description = string;
    }

    public void setIcon40url(String string) {
        this.icon40url = string;
    }

    public void setIcon70url(String string) {
        this.icon70url = string;
    }

    public void setRedditUrl(String string) {
        this.redditUrl = string;
    }

    public void setAwardId(String string) {
        this.awardId = string;
    }

    public void setId(String string) {
        this.id = string;
    }

    public void setName(String string) {
        this.name = string;
    }

}
