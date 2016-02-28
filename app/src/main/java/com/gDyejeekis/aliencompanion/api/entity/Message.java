package com.gDyejeekis.aliencompanion.api.entity;

import android.text.SpannableStringBuilder;

import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToDouble;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 10/10/2015.
 */
public class Message extends Thing implements RedditItem {

    public int compareTo(Thing o) {
        return this.getFullName().compareTo(o.getFullName());
    }

    public int getViewType() {
        return RedditItemListAdapter.VIEW_TYPE_MESSAGE;
    }

    public void setThumbnailObject(Thumbnail thumbnail) {}

    public Thumbnail getThumbnailObject() {
        return null;
    }

    public String getThumbnail() {
        return null;
    }

    public String getMainText() {
        return bodyHTML;
    }

    public void storePreparedText(SpannableStringBuilder stringBuilder) {
        bodyPrepared = stringBuilder;
    }

    public SpannableStringBuilder getPreparedText() {
        return bodyPrepared;
    }

    //private User user;

    public String subject;
    public String body;
    public String bodyHTML;
    public String author;
    public String destination;
    public String subreddit;
    public String context;
    public double created;
    public double createdUTC;
    public Boolean isNew;
    public Boolean wasComment;
    public SpannableStringBuilder bodyPrepared;
    public String agePrepared;

    public Message(JSONObject obj) {
        super(safeJsonToString(obj.get("name")));

        try {
            this.subject = safeJsonToString(obj.get("subject"));
            this.author = safeJsonToString(obj.get("author"));
            this.destination = safeJsonToString(obj.get("dest"));
            this.body = safeJsonToString(obj.get("body"));
            this.bodyHTML = safeJsonToString(obj.get("body_html"));
            this.subreddit = safeJsonToString(obj.get("subreddit"));
            this.context = safeJsonToString(obj.get("context"));
            this.created = safeJsonToDouble(obj.get("created"));
            this.createdUTC = safeJsonToDouble(obj.get("created_utc"));
            this.isNew = safeJsonToBoolean(obj.get("new"));
            this.wasComment = safeJsonToBoolean(obj.get("was_comment"));

            if(!MyApplication.useMarkdownParsing) bodyHTML = StringEscapeUtils.unescapeHtml(bodyHTML);

            //bodyPrepared = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(bodyHTML, null, new MyHtmlTagHandler()));
            agePrepared = ConvertUtils.getSubmissionAge(createdUTC);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("JSON Object could not be parsed into a Comment. Provide a JSON Object with a valid structure.");
        }
    }

    //public void setUser(User user) {
    //    this.user = user;
    //}
    //
    //public User getUser() {
    //    return this.user;
    //}

}
