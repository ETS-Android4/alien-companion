package com.gDyejeekis.aliencompanion.api.entity;

import android.text.SpannableStringBuilder;
import android.util.Log;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToBoolean;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToDouble;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToLong;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;

import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.Utils.ThumbnailUtils;
import com.gDyejeekis.aliencompanion.api.imgur.Image;
import com.gDyejeekis.aliencompanion.enums.ImgurThumbnailSize;
import com.gDyejeekis.aliencompanion.enums.YoutubeThumbnailSize;
import com.gDyejeekis.aliencompanion.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import java.io.Serializable;
import java.util.List;


/**
 * This class represents a vote on a link submission on Reddit.
 *
 * @author Omer Elnour
 * @author Andrei Sfat
 * @author Raul Rene Lepsa
 * @author Jonny Krysh
 * @author Danny Tsegai
 * @author Simon Kassing
 */
public class Submission extends Thing implements Serializable, MultiLevelExpIndListAdapter.ExpIndData, RedditItem {

	public int getViewType(){
		return RedditItemListAdapter.VIEW_TYPE_POST;
	}

	public String getMainText() {
		return selftextHTML;
	}

	public SpannableStringBuilder getPreparedText() {
		return selfTextPrepared;
	}

	public void storePreparedText(SpannableStringBuilder stringBuilder) {
		//selfTextPrepared = stringBuilder;
		//if(selfTextPrepared.toString().length()>200) selfTextPreparedPreview = selfTextPrepared.toString().substring(0, 200) + " ...";
	}

    //private HttpClient restClient;

    /** This is the user that will vote on a submission. */
    private User user;

    private String url;
    private String permalink;
    private String author;
    private String title;
    private String subreddit;
    private String subredditId;
    private String thumbnail;

    private String selftext;
    private String selftextHTML;
    private String domain;
    private String bannedBy;
    private String approvedBy;

	public SpannableStringBuilder selfTextPrepared;
	//public String selfTextPreparedPreview;
	public String agePrepared;
    
    private Long gilded;
    private Long commentCount;
    private Long reportCount;
    private Long score;
    private Long upVotes;
    private Long downVotes;

    private Double created;
    private Double createdUTC;
    //private Boolean visited;
    private Boolean self;
    private Boolean saved;
    private Boolean edited;
    private Boolean stickied;
    private Boolean nsfw;
    private Boolean hidden;
    private Boolean clicked;

	private Thumbnail thumbnailObject;

	private List<Comment> syncedComments;

	public boolean hasImageButton;

	private List<Image> imgurs;
    
    // 
    private String likes;
    //private String authorFlairCSSClass;
    //private String linkFlairCSSClass;
    //private String distinguished;

	public void setSyncedComments(List<Comment> comments) {
		syncedComments = comments;
	}

	public List<Comment> getSyncedComments() {
		if(syncedComments!=null) return syncedComments;
		return null;
	}

	public List<Image> getImgurUrls() {
		return imgurs;
	}

	public void setImgurUrls(List<Image> images) {
		this.imgurs = images;
	}

    /**
	 * @return the approvedBy
	 */
	public String getApprovedBy() {
		return approvedBy;
	}

	/**
	 * @param approvedBy the approvedBy to set
	 */
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	/**
	 * @return the hidden
	 */
	public Boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the clicked
	 */
	public Boolean isClicked() {
		return clicked;
	}

	/**
	 * @param clicked the clicked to set
	 */
	public void setClicked(Boolean clicked) {
		this.clicked = clicked;
	}

	public Submission(String id) {
		super("t3_"+id);
	}

	public Submission() {
		super();
	}

	/**
     * Create a Submission from a JSONObject
     *
     * @param obj The JSONObject to load Submission data from
     */
    public Submission(JSONObject obj) {
    	super(safeJsonToString(obj.get("name")));

		updateSubmission(obj);

		handleThumbnail();
		//hasImageButton = !thumbnail.equals("") && (domain.equals("imgur.com") || domain.equals("i.imgur.com") || domain.equals("youtube.com") || domain.equals("youtu.be"));
		//if(nsfw) hasImageButton = (MyApplication.showNSFWpreview);
    }

	private void handleThumbnail() {
		if(!(nsfw && !MyApplication.showNSFWpreview)) {
			if((domain.equals("youtube.com") || domain.equals("youtu.be") || domain.equals("m.youtube.com"))) {
				hasImageButton = true;
				thumbnail = ThumbnailUtils.getYoutubeThumbnail(url, YoutubeThumbnailSize.MEDIUM_QUALITY);
			}
			else if(domain.equals("imgur.com") || domain.equals("i.imgur.com")) {
				if(url.contains("/a/") || url.contains("/gallery/")) hasImageButton = false;
				else {
					hasImageButton = true;
					thumbnail = ThumbnailUtils.getImgurThumbnail(url, ImgurThumbnailSize.MEDIUM_THUMBNAIL);
				}
			}
		}
		else hasImageButton = false;

		Log.d("geotest", thumbnail);
	}

	public void updateSubmission(JSONObject obj) {
		try {

			setURL(safeJsonToString(obj.get("url")));
			setPermalink(safeJsonToString(obj.get("permalink")));
			setAuthor(safeJsonToString(obj.get("author")));
			setTitle(safeJsonToString(obj.get("title")));
			setSubreddit(safeJsonToString(obj.get("subreddit")));
			setSubredditId(safeJsonToString(obj.get("subreddit_id")));
			setThumbnail(safeJsonToString(obj.get("thumbnail")));

			setSelftext(safeJsonToString(obj.get("selftext")));
			setSelftextHTML(safeJsonToString(obj.get("selftext_html")));
			setDomain(safeJsonToString(obj.get("domain")));
			setBannedBy(safeJsonToString(obj.get("banned_by")));
			setApprovedBy(safeJsonToString(obj.get("approved_by")));

			setGilded(safeJsonToLong(obj.get("gilded")));
			setCommentCount(safeJsonToLong(obj.get("num_comments")));
			setReportCount(safeJsonToLong(obj.get("num_reports")));
			setScore(safeJsonToLong(obj.get("score")));
			setUpVotes(safeJsonToLong(obj.get("ups")));
			setDownVotes(safeJsonToLong(obj.get("downs")));

			setCreated(safeJsonToDouble(obj.get("created")));
			setCreatedUTC(safeJsonToDouble(obj.get("created_utc")));

			//setVisited(safeJsonToBoolean(obj.get("visited")));
			setSelf(safeJsonToBoolean(obj.get("is_self")));
			setSaved(safeJsonToBoolean(obj.get("saved")));
			setEdited(safeJsonToBoolean(obj.get("edited")));
			setStickied(safeJsonToBoolean(obj.get("stickied")));
			setNSFW(safeJsonToBoolean(obj.get("over_18")));
			setHidden(safeJsonToBoolean(obj.get("hidden")));
			setClicked(safeJsonToBoolean(obj.get("clicked")));
			setLikes(safeJsonToString(obj.get("likes")));

			title = StringEscapeUtils.unescapeHtml(title);
			selftextHTML = StringEscapeUtils.unescapeHtml(selftextHTML);

			//if(selftextHTML!=null) {
			//	SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(selftextHTML, null, new MyHtmlTagHandler()));
			//	String text = stringBuilder.toString();
			//	if(text.length()>200) selfTextPreparedPreview = text.substring(0,200) + " ...";
			//	else selfTextPreparedPreview = text;
			//}
			agePrepared = ConvertUtils.getSubmissionAge(createdUTC);

			//Log.e("self text html", selftextHTML);

		} catch (Exception e) {
			Log.e("Api error", "Error creating Submission");
		}
	}

	public Thumbnail getThumbnailObject() {
		return thumbnailObject;
	}

	public void setThumbnailObject(Thumbnail thumbnail) {
		this.thumbnailObject = thumbnail;
	}

	@Override
	public List<? extends MultiLevelExpIndListAdapter.ExpIndData> getChildren() {
		return null;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public void setIsGroup(boolean value) {
	}

	@Override
	public void setGroupSize(int groupSize) {

	}

    // this is very stinky..
    //public void setRestClient(HttpClient restClient) {
    //    this.restClient = restClient;
    //}

	public void setLikes(String likes) {
		this.likes = (likes==null) ? "null" : likes;
	}

	public String getLikes() {
		return likes;
	}

    public void setUpVotes(Long upVotes) {
        this.upVotes = upVotes;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreatedUTC(Double createdUTC) {
        this.createdUTC = createdUTC;
    }

    public void setDownVotes(Long downVotes) {
        this.downVotes = downVotes;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getURL() {
        return url;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public Long getUpVotes() {
        return upVotes;
    }

    public Long getDownVotes() {
        return downVotes;
    }

    public Long getScore() {
        return score;
    }

    public Double getCreatedUTC() {
        return createdUTC;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }
    
    /**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * @return the thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	/**
	 * @param thumbnail the thumbnail to set
	 */
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * @return the selftext
	 */
	public String getSelftext() {
		return selftext;
	}

	/**
	 * @param selftext the selftext to set
	 */
	public void setSelftext(String selftext) {
		this.selftext = selftext;
	}

	/**
	 * @return the selftextHTML
	 */
	public String getSelftextHTML() {
		return selftextHTML;
	}

	/**
	 * @param selftextHTML the selftextHTML to set
	 */
	public void setSelftextHTML(String selftextHTML) {
		this.selftextHTML = selftextHTML;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the bannedBy
	 */
	public String getBannedBy() {
		return bannedBy;
	}

	/**
	 * @param bannedBy the bannedBy to set
	 */
	public void setBannedBy(String bannedBy) {
		this.bannedBy = bannedBy;
	}

	/**
	 * @return the gilded
	 */
	public Long getGilded() {
		return gilded;
	}

	/**
	 * @param gilded the gilded to set
	 */
	public void setGilded(Long gilded) {
		this.gilded = gilded;
	}

	/**
	 * @return the reportCount
	 */
	public Long getReportCount() {
		return reportCount;
	}

	/**
	 * @param reportCount the reportCount to set
	 */
	public void setReportCount(Long reportCount) {
		this.reportCount = reportCount;
	}

	/**
	 * @return the created
	 */
	public Double getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Double created) {
		this.created = created;
	}

	///**
	// * @return the visited
	// */
	//public Boolean isVisited() {
	//	return visited;
	//}

	///**
	// * @param visited the visited to set
	// */
	//public void setVisited(Boolean visited) {
	//	this.visited = visited;
	//}

	/**
	 * @return the self
	 */
	public Boolean isSelf() {
		return self;
	}

	/**
	 * @param self the self to set
	 */
	public void setSelf(Boolean self) {
		this.self = self;
	}

	/**
	 * @return the saved
	 */
	public Boolean isSaved() {
		return saved;
	}

	/**
	 * @param saved the saved to set
	 */
	public void setSaved(Boolean saved) {
		this.saved = saved;
	}

	/**
	 * @return the edited
	 */
	public Boolean isEdited() {
		return edited;
	}

	/**
	 * @param edited the edited to set
	 */
	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	/**
	 * @return the stickied
	 */
	public Boolean isStickied() {
		return stickied;
	}

	/**
	 * @param stickied the stickied to set
	 */
	public void setStickied(Boolean stickied) {
		this.stickied = stickied;
	}

	/**
	 * @return the nsfw
	 */
	public Boolean isNSFW() {
		return nsfw;
	}

	/**
	 * @param nsfw the nsfw to set
	 */
	public void setNSFW(Boolean nsfw) {
		this.nsfw = nsfw;
	}

	/**
     * String representation of this Submission.
     * @return String representation
     */
    public String toString() {
    	return "Submission(" + this.getFullName() + ")<" + title + ">";
    }
    
    @Override
    public boolean equals(Object other) {
    	return (other instanceof Submission && this.getFullName().equals(((Submission) other).getFullName()));
    }

	public int compareTo(Thing o) {
		return this.getFullName().compareTo(o.getFullName());
	}
    
}