package com.gDyejeekis.aliencompanion.models;

import org.json.simple.JSONObject;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToDouble;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToLong;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

import java.io.Serializable;

/**
 * Created by George on 10/1/2017.
 */

public class RedditVideo implements Serializable {

    private static final long serialVersionUID = 432242709L;

    private String fallbackUrl;
    private String scrubberMediaUrl;
    private String dashUrl;
    private String hlsUrl;
    private Boolean isGif;
    private String transcodingStatus;
    private Integer height;
    private Integer width;
    private Long duration;

    public RedditVideo(JSONObject obj) {
        setFallbackUrl(safeJsonToString(obj.get("fallback_url")));
        setScrubberMediaUrl(safeJsonToString(obj.get("scrubber_media_url")));
        setDashUrl(safeJsonToString(obj.get("dash_url")));
        setHlsUrl(safeJsonToString(obj.get("hls_url")));
        setGif(safeJsonToBoolean(obj.get("is_gif")));
        setTranscodingStatus(safeJsonToString(obj.get("transcoding_status")));
        setHeight(safeJsonToInteger(obj.get("height")));
        setWidth(safeJsonToInteger(obj.get("width")));
        setDuration(safeJsonToLong(obj.get("duration")));
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    public String getScrubberMediaUrl() {
        return scrubberMediaUrl;
    }

    public void setScrubberMediaUrl(String scrubberMediaUrl) {
        this.scrubberMediaUrl = scrubberMediaUrl;
    }

    public String getDashUrl() {
        return dashUrl;
    }

    public void setDashUrl(String dashUrl) {
        this.dashUrl = dashUrl;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public Boolean getGif() {
        return isGif;
    }

    public void setGif(Boolean gif) {
        isGif = gif;
    }

    public String getTranscodingStatus() {
        return transcodingStatus;
    }

    public void setTranscodingStatus(String transcodingStatus) {
        this.transcodingStatus = transcodingStatus;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
