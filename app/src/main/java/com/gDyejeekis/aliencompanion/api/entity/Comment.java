package com.gDyejeekis.aliencompanion.api.entity;

import android.text.SpannableStringBuilder;

import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToInteger;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToDouble;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;

/**
 * A Reddit comment. Contains the edited timestamp, the body
 *
 * @author Benjamin Jakobus
 * @author Raul Rene Lepsa
 * @author Simon Kassing
 */
public class Comment extends Thing implements MultiLevelExpIndListAdapter.ExpIndData, RedditItem, Serializable {

    public int getViewType(){
        return RedditItemListAdapter.VIEW_TYPE_USER_COMMENT;
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
        return bodyHTML;
    }

    public SpannableStringBuilder getPreparedText() {
        return bodyPrepared;
    }

    public void storePreparedText(SpannableStringBuilder stringBuilder) {
        bodyPrepared = stringBuilder;
    }

    private String author;			// Username of the author
    private String parentId;		// Parent identifier
    private String subreddit;		// Subreddit name
    private String subredditId;		// Subreddit identifier
    private String linkId;			// Submission (aka. link) identifier
    private String linkTitle;       // Submission title
    private String bodyHTML;		// The body with HTML markup
    private Boolean scoreHidden;	// Whether the score is hidden
    private String body;            // The actual body
    private Boolean edited;         // Edited timestamp
    private double created;         // Created timestamp
    private double createdUTC;      // Created UTC timestamp
    private Boolean hasReplies;		// If replies exist on reddit
    private Boolean saved;
    //private List<Comment> replies;  // Replies if retrieved
    private Integer gilded;        	// Amount of times the comment been gilded
    private Integer score;        	// Karma score
    private Integer upvotes;        // Number of upvotes that this body received
    private Integer downvotes;      // Number of downvotes that this body received

    public SpannableStringBuilder bodyPrepared;
    public String agePrepared;

    private boolean mIsGroup;
    private int mGroupSize;
    private List<Comment> mChildren;
    private int mIndentation;
    
    // Possible fields to add as well:
//    private String bannedBy;
    String likes;
//    private String approvedBy;
//    private String authorFlairCSSClass;
//    private String authorFlairText;
//    String num_reports = null;
//    String distinguished = null;

    public Comment(JSONObject obj) {
    	super(safeJsonToString(obj.get("name")));
    	
        try {
        	
            this.setAuthor(safeJsonToString(obj.get("author")));
            this.setParentId(safeJsonToString(obj.get("parent_id")));
            this.setBody(safeJsonToString(obj.get("body")));
            this.setEdited(safeJsonToBoolean(obj.get("edited")));
            this.setCreated(safeJsonToDouble(obj.get("created")));
            this.setCreatedUTC(safeJsonToDouble(obj.get("created_utc")));
            hasReplies = (obj.get("replies") != null) ? !safeJsonToString(obj.get("replies")).isEmpty() : false;
            //this.replies = new LinkedList<Comment>();
            this.setGilded(safeJsonToInteger(obj.get("gilded")));
            this.setScore(safeJsonToInteger(obj.get("score")));
            this.setUpvotes(safeJsonToInteger(obj.get("ups")));
            this.setDownvotes(safeJsonToInteger(obj.get("downs")));
            this.setSubreddit(safeJsonToString(obj.get("subreddit")));
            this.setSubredditId(safeJsonToString(obj.get("subreddit_id")));
            this.setLinkId(safeJsonToString(obj.get("link_id")));
            this.setBodyHTML(safeJsonToString(obj.get("body_html")));
            this.setScoreHidden(safeJsonToBoolean(obj.get("score_hidden")));
            this.setSaved(safeJsonToBoolean(obj.get("saved")));

            this.setLinkTitle(safeJsonToString(obj.get("link_title")));
            setLikes(safeJsonToString(obj.get("likes")));

            linkTitle = StringEscapeUtils.unescapeHtml(linkTitle);
            if(!MyApplication.useBypassParsing) bodyHTML = StringEscapeUtils.unescapeHtml(bodyHTML);

            //bodyPrepared = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(bodyHTML, null, new MyHtmlTagHandler()));
            agePrepared = ConvertUtils.getSubmissionAge(createdUTC);

            setIndentation(0);
            mChildren = new LinkedList<>();
            
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new IllegalArgumentException("JSON Object could not be parsed into a Comment. Provide a JSON Object with a valid structure.");
        }

    }

    public Comment(Message message) {
        super(message.getFullName());

        author = message.author;
        body = message.body;
        bodyHTML = message.bodyHTML;
        created = message.created;
        createdUTC = message.createdUTC;
    }

    public List<? extends MultiLevelExpIndListAdapter.ExpIndData> getChildren() {
        return mChildren;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public void setIsGroup(boolean value) {
        mIsGroup = value;
    }

    public void setGroupSize(int groupSize) {
        mGroupSize = groupSize;
    }

    public int getGroupSize() {
        return mGroupSize;
    }

    public void addChild(Comment child) {
        mChildren.add(child);
        child.setIndentation(getIndentation() + 1);
    }

    public int getIndentation() {
        return mIndentation;
    }

    public void setIndentation(int indentation) {
        mIndentation = indentation;
    }

    //public void indentReplies() {
    //    for(Comment c : mChildren) {
    //        c.setIndentation(getIndentation()+1);
    //    }
    //}

    public void setLinkTitle(String title) {
        this.linkTitle = title;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLikes(String likes) {
        this.likes = (likes==null) ? "null" : likes;
    }

    public String getLikes() {
        return likes;
    }

    /**
     * Add a reply to this comment.
     * @param c Reply comment
     */
    public void addReply(Comment c) {
    	//this.replies.add(c);
        addChild(c);
    }
    
    /**
     * If the comment is retrieved recursively, this might have the replies.
     * @return Replies
     */
    public List<Comment> getReplies() {
        //return replies;
        return mChildren;
    }
    
    public void setReplies(List<Comment> replies) {
    	//this.replies = replies;
        mChildren = replies;
    }
    
    /**
     * Return whether the comment has replies, this is only set if the comment
     * is retrieved recursively.
     * @return Whether there are replies on Reddit for this comment
     */
    public Boolean hasRepliesSomewhere() {
    	return hasReplies;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public double getCreated() {
        return created;
    }

    public void setCreated(double created) {
        this.created = created;
    }

    public Integer getGilded() {
		return gilded;
	}

	public void setGilded(Integer gilded) {
		this.gilded = gilded;
	}

	public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }
    
    public double getCreatedUTC() {
		return createdUTC;
	}

	public void setCreatedUTC(double createdUTC) {
		this.createdUTC = createdUTC;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

    public void setSaved(Boolean flag) {
        this.saved = flag;
    }

    public Boolean isSaved() {
        return saved;
    }
	/**
	 * @return the subreddit
	 */
	public String getSubreddit() {
		return subreddit;
	}

	/**
	 * @param subreddit the subreddit to set
	 */
	public void setSubreddit(String subreddit) {
		this.subreddit = subreddit;
	}

	/**
	 * @return the subredditId
	 */
	public String getSubredditId() {
		return subredditId;
	}

	/**
	 * @param subredditId the subredditId to set
	 */
	public void setSubredditId(String subredditId) {
		this.subredditId = subredditId;
	}

	/**
	 * @return the linkId
	 */
	public String getLinkId() {
		return linkId;
	}

	/**
	 * @param linkId the linkId to set
	 */
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	/**
	 * @return the bodyHTML
	 */
	public String getBodyHTML() {
		return bodyHTML;
	}

	/**
	 * @param bodyHTML the bodyHTML to set
	 */
	public void setBodyHTML(String bodyHTML) {
		this.bodyHTML = bodyHTML;
	}

	/**
	 * @return the scoreHidden
	 */
	public Boolean isScoreHidden() {
		return scoreHidden;
	}

	/**
	 * @param scoreHidden the scoreHidden to set
	 */
	public void setScoreHidden(Boolean scoreHidden) {
		this.scoreHidden = scoreHidden;
	}

	@Override
    public String toString() {
    	return "Comment(" + identifier + ")<" + ((body.length() > 10) ? body.substring(0, 10) : body) + ">";
    }    
    
    @Override
    public boolean equals(Object other) {
    	return (other instanceof Comment && this.getFullName().equals(((Comment) other).getFullName()));
    }

	public int compareTo(Thing o) {
		return this.getFullName().compareTo(o.getFullName());
	}
    
}
