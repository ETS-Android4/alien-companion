package com.gDyejeekis.aliencompanion.api.entity;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.api.retrieval.Trophies;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.squareup.picasso.Picasso;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Encapsulates user information (regarding karma, emails, identifiers, statuses, created time and current modhash)
 *
 * @author Raul Rene Lepsa
 */
public class UserInfo implements RedditItem {

    @Override
    public String getIdentifier() {
        return id;
    }

    public int getViewType(){
        return RedditItemListAdapter.VIEW_TYPE_USER_INFO;
    }

    public String getThumbnail() {
        return null;
    }

    public void setThumbnailObject(Thumbnail thumbnailObject) {

    }

    public Thumbnail getThumbnailObject() {
        return null;
    }

    public String getMainText() {
        return "";
    }

    public SpannableStringBuilder getPreparedText() {
        return null;
    }

    public void storePreparedText(SpannableStringBuilder stringBuilder) {}

    // User identifier
    private String id;

    // The user's name
    private String name;

    // Modhash token
    private String modhash;

    // Karma points for all the comments
    private long commentKarma;

    // Karma points for all the submitted links
    private long linkKarma;

    //number of messages in the user's inbox
    private long inboxCount;

    // Whether the user is a moderator
    private boolean isMod;

    // Whether or not the user has moderator email
    private Boolean hasModMail;

    // Whether the account is associated with an email address
    private Boolean hasMail;

    // Indicates whether the user has verified the email address
    private Boolean hasVerifiedEmail;

    // Whether the user is a gold member
    private boolean isGold;

    // Timestamp of the creation date
    private double created;

    // UTC timestamp of creation date
    private double createdUTC;

    // Indicates whether this user is friends with the currently connected one. Believe it or not, you can actually be 
    // friends with yourself. http://www.reddit.com/r/reddit.com/comments/duf7q/random_reddit_protip_you_can_add_yourself_as_a/
    private boolean isFriend;

    // Indicates whether the user is over 18
    private Boolean over18;

    // List of user trophies
    private List<Trophy> trophyList;

    public UserInfo() {

    }

    public UserInfo(JSONObject info) {

        setCreatedUTC((Double) info.get("created_utc"));
        setGold((Boolean) info.get("is_gold"));
        setLinkKarma((Long) info.get("link_karma"));
        setCommentKarma((Long) info.get("comment_karma"));
        setMod((Boolean) info.get("is_mod"));
        setModhash((String) info.get("modhash"));
        setHasVerifiedEmail((Boolean) info.get("has_verified_email"));
        setId((String) info.get("id"));
        setOver18((Boolean) info.get("over_18"));
        setCreated((Double) info.get("created"));
        setName((String) info.get("name"));
        //if(RedditOAuth.useOAuth2 && name.equals(~current account name here~) {
        //    setInboxCount((Long) info.get("inbox_count"));
        //}
        //else if(!RedditOAuth.useOAuth2){
        //    setHasMail((Boolean) info.get("has_mail"));
        //    setHasModMail((Boolean) info.get("has_mod_mail"));
        //    setFriend((Boolean) info.get("is_friend"));
        //}
    }

    public void retrieveTrophyInfo(HttpClient httpClient) {
        Trophies trophies = new Trophies(httpClient);
        trophyList = trophies.ofUser(name);
    }

    public void preleoadTrophyImages(Context context, List<Trophy> trophyList) {
        for(Trophy trophy : trophyList) {
            Picasso.with(context).load(trophy.getIcon70url()).fetch();
        }
    }

    public long getInboxCount() {
        return inboxCount;
    }

    public void setInboxCount(long inboxCount) {
        this.inboxCount = inboxCount;
    }

    public void setTrophyList(List<Trophy> trophyList) {
        this.trophyList = trophyList;
    }

    public List<Trophy> getTrophyList() {
        return trophyList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public long getCommentKarma() {
        return commentKarma;
    }

    public void setCommentKarma(long commentKarma) {
        this.commentKarma = commentKarma;
    }

    public long getLinkKarma() {
        return linkKarma;
    }

    public void setLinkKarma(long linkKarma) {
        this.linkKarma = linkKarma;
    }

    public boolean isMod() {
        return isMod;
    }

    public void setMod(boolean isMod) {
        this.isMod = isMod;
    }

    public Boolean getHasModMail() {
        return hasModMail;
    }

    public void setHasModMail(Boolean hasModMail) {
        this.hasModMail = hasModMail;
    }

    public Boolean getHasMail() {
        return hasMail;
    }

    public void setHasMail(Boolean hasMail) {
        this.hasMail = hasMail;
    }

    public Boolean isHasVerifiedEmail() {
        return hasVerifiedEmail;
    }

    public void setHasVerifiedEmail(Boolean hasVerifiedEmail) {
        this.hasVerifiedEmail = hasVerifiedEmail;
    }

    public boolean isGold() {
        return isGold;
    }

    public void setGold(boolean isGold) {
        this.isGold = isGold;
    }

    public double getCreated() {
        return created;
    }

    public void setCreated(double created) {
        this.created = created;
    }

    public double getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(double createdUTC) {
        this.createdUTC = createdUTC;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public Boolean getOver18() {
        return over18;
    }

    public void setOver18(Boolean over18) {
        this.over18 = over18;
    }
    
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	String newLine = System.getProperty("line.separator");
    	
    	result.append("id: ").append(id).append(newLine)
    		  .append("name: ").append(name).append(newLine)
    		  .append("modhash: ").append(modhash).append(newLine)
    		  .append("commentKarma: ").append(commentKarma).append(newLine)
    		  .append("linkKarma: ").append(linkKarma).append(newLine)
    		  .append("isModerator: ").append(isMod).append(newLine)
    		  .append("hasModMail: ").append(hasModMail).append(newLine)
    		  .append("hasMail: ").append(hasMail).append(newLine)
    		  .append("hasVerifiedEmail: ").append(hasVerifiedEmail).append(newLine)
    		  .append("isGold: ").append(isGold).append(newLine)
    		  .append("Created: ").append(created).append(newLine)
    		  .append("CreatedUTC: ").append(createdUTC).append(newLine)
    		  .append("isFriend: ").append(isFriend).append(newLine)
    		  .append("over18: ").append(over18);
    	
    	return result.toString();
    }
}
