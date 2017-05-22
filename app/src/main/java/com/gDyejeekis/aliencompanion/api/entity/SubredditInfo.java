package com.gDyejeekis.aliencompanion.api.entity;

import com.gDyejeekis.aliencompanion.utils.HtmlFormatUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToLong;
import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

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
            userIsSubscriber = safeJsonToBoolean(obj.get("user_is_subscriber"));
            subscribers = safeJsonToLong(obj.get("subscribers"));
            activeAccounts = safeJsonToLong(obj.get("accounts_active"));
            submitTextHtml = safeJsonToString(obj.get("submit_text_html"));
            descriptionHtml = safeJsonToString(obj.get("description_html"));
            publicDescriptionHtml = safeJsonToString(obj.get("public_description_html"));
            headerImgUrl = safeJsonToString(obj.get("header_img"));

            submitTextHtml = StringEscapeUtils.unescapeHtml4(submitTextHtml);
            submitTextHtml = HtmlFormatUtils.modifySpoilerHtml(submitTextHtml);
            descriptionHtml = StringEscapeUtils.unescapeHtml4(descriptionHtml);
            descriptionHtml = HtmlFormatUtils.modifySpoilerHtml(descriptionHtml);
            publicDescriptionHtml = StringEscapeUtils.unescapeHtml4(publicDescriptionHtml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
