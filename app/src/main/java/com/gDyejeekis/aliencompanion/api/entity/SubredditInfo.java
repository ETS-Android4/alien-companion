package com.gDyejeekis.aliencompanion.api.entity;

import org.json.simple.JSONObject;

/**
 * Created by sound on 2/29/2016.
 */
public class SubredditInfo {

    public Boolean userIsSubscriber;
    public long subscribers;
    public long activeAccounts;
    public String submitTextHtml;
    public String descriptionHtml;
    public String publicDescriptionHtml;
    public String headerImgUrl;

    public SubredditInfo(JSONObject obj) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
