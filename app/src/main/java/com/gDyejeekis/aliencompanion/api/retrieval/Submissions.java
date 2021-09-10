package com.gDyejeekis.aliencompanion.api.retrieval;

import android.util.Log;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Kind;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.params.QuerySyntax;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.ParamFormatter;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;


/**
 * This class offers the following functionality:
 * 1) Parsing the results of a request into Submission objects (see <code>Submissions.parse()</code>).
 * 2) The ability to get submissions from a subreddit (see <code>Submissions.ofSubreddit()</code>).
 * 3) The ability to search submissions on Reddit (see <code>Submissions.search()</code>).
 * 4) The ability to get submissions of a user (see <code>Submissions.ofUser()</code>).
 * 
 * @author <a href="http://www.omrlnr.com">Omer Elnour</a>
 * @author <a href="http://www.deltacdev.com">Simon Kassing</a>
 */
public class Submissions implements ActorDriven {

	// when true stickied posts will always show up as stickied regardless of how they are retrieved (subreddit, user, multireddit, etc)
	// when false stickied posts will only show up as stickied when using the ofSubreddit() method (excluding /r/all)
	public static final boolean alwaysShowStickied = false;

	/**
	 * Handle to REST client instance.
	 */
    private final HttpClient httpClient;
    private User user;

	public static int postsSkipped;

    /**
     * Constructor.
     * Default general actor will be used.
     * @param httpClient REST client handle
     */
    public Submissions(HttpClient httpClient) {
        this.httpClient = httpClient;
		postsSkipped = 0;
    }
    
    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     * 
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public Submissions(HttpClient httpClient, User actor) {
    	this.httpClient = httpClient;
        this.user = actor;
		postsSkipped = 0;
    }
    
    /**
     * Switch the current user for the new user who will
     * be used when invoking retrieval requests.
     * 
     * @param new_actor New user
     */
    public void switchActor(User new_actor) {
    	this.user = new_actor;
    }
    
    /**
     * Parses a JSON feed received from Reddit (URL) into a nice list of Submission objects.
     * 
     * @param url 	URL
     * @return 		Listing of submissions
     */
    public List<RedditItem> parse(String url, boolean showStickied) throws RetrievalFailedException, RedditError {
		Log.d("parse url", url);
    	
    	// Determine cookie
    	String cookie = (user == null) ? null : user.getCookie();
    	
    	// List of submissions
        List<RedditItem> submissions = new LinkedList<>();
        
        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();
        
        if (response instanceof JSONObject) {
        	
	        JSONObject object = (JSONObject) response;
	        if (object.get("error") != null) {
	        	throw new RedditError("Response contained error code " + object.get("error") + ".");
	        }
	        JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");

	        // Iterate over the submission results
	        JSONObject data;
	        Submission submission;
	        for (Object anArray : array) {
	            data = (JSONObject) anArray;
	            
	            // Make sure it is of the correct kind
	            String kind = safeJsonToString(data.get("kind"));
				if (kind != null) {
					if (kind.equals(Kind.LINK.value())) {

                        // Create and add submission
                        data = ((JSONObject) data.get("data"));
                        submission = new Submission(data);
						submission.showAsStickied = showStickied;
						if(!submission.isNSFW() || MyApplication.showNsfwPosts) {
							submission.setUser(user);
							submissions.add(submission);
						}
						else postsSkipped++;
                    }
				}
			}
        
        } else {
        	Log.e("Api error", "Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        // Finally return list of submissions 
        return submissions;
        
    }

	protected List<RedditItem> frontpage(String sort, String timeSpan, String count, String limit, String after, String before, String show) throws RetrievalFailedException, RedditError {

		// Format parameters
		String params = "";

		params = ParamFormatter.addParameter(params, "t", timeSpan);
		params = ParamFormatter.addParameter(params, "count", count);
		params = ParamFormatter.addParameter(params, "limit", limit);
		params = ParamFormatter.addParameter(params, "after", after);
		params = ParamFormatter.addParameter(params, "before", before);
		params = ParamFormatter.addParameter(params, "show", show);

		// Retrieve submissions from the given URL
		return parse(String.format(ApiEndpointUtils.SUBMISSIONS_GET_FRONT, sort, params), alwaysShowStickied);
	}

	public List<RedditItem> frontpage(SubmissionSort sort, TimeSpan timeSpan, int count, int limit, Submission after, Submission before, boolean show_all) throws RetrievalFailedException, RedditError {

		return frontpage(
				(sort != null) ? sort.value() : "hot",
				(timeSpan != null) ? timeSpan.value() : "",
				String.valueOf(count),
				String.valueOf(limit),
				(after != null) ? after.getFullName() : "",
				(before != null) ? before.getFullName() : "",
				(show_all) ? "all" : ""
		);
	}

    /**
     * Gets all the submissions of a particular subreddit using the given parameters.
     * The parameters here are in Strings instead of wrapper objects, which allows users
     * to manually adjust the parameters (if the API changes and jReddit is not updated
     * in time yet).
     * 
     * @param subreddit			Name of the reddit (e.g. "funny")
     * @param sort				Sorting method
     * @param count				Count at which the submissions are started being numbered
     * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
     * @param after				The submission after which needs to be retrieved
     * @param before			The submission before which needs to be retrieved
     * @param show				Show all (disables filters such as "hide links that I have voted on")
     * @return 					The linked list containing submissions
     */
    protected List<RedditItem> ofSubreddit(String subreddit, String sort, String timeSpan, String count, String limit, String after, String before, String show) throws RetrievalFailedException, RedditError {
    	assert subreddit != null;
    	
    	// Encode the reddit name for the URL:
    	try {
			subreddit = URLEncoder.encode(subreddit, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	// Format parameters
    	String params = "";

		params = ParamFormatter.addParameter(params, "t", timeSpan);
    	params = ParamFormatter.addParameter(params, "count", count);
    	params = ParamFormatter.addParameter(params, "limit", limit);
    	params = ParamFormatter.addParameter(params, "after", after);
    	params = ParamFormatter.addParameter(params, "before", before);
    	params = ParamFormatter.addParameter(params, "show", show);

		boolean showStickied = alwaysShowStickied ? true : !subreddit.equalsIgnoreCase("all");
    	
        // Retrieve submissions from the given URL
        return parse(String.format(ApiEndpointUtils.SUBMISSIONS_GET, subreddit, sort, params), showStickied);
        
    }
    
    /**
     * Gets all the submissions of a particular subreddit using the given parameters.
     * 
     * @param subreddit			Name of the reddit (e.g. "funny")
     * @param sort				Sorting method, hot default
     * @param count				Count at which the submissions are started being numbered
     * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
     * @param after				The submission after which needs to be retrieved
     * @param before			The submission before which needs to be retrieved
     * @param show_all			Show all (disables filters such as "hide links that I have voted on")
     * @return 					The linked list containing submissions
     */
    public List<RedditItem> ofSubreddit(String subreddit, SubmissionSort sort, TimeSpan timeSpan, int count, int limit, Submission after, Submission before, boolean show_all) throws RetrievalFailedException, RedditError {
    	
    	if (subreddit == null || subreddit.isEmpty()) {
    		throw new IllegalArgumentException("The subreddit must be defined.");
    	}
    	
    	return ofSubreddit(
    			subreddit, 
    			(sort != null) ? sort.value() : "hot",
				(timeSpan !=null) ? timeSpan.value() : "",
    			String.valueOf(count),
    			String.valueOf(limit),
    			(after != null) ? after.getFullName() : "",
    			(before != null) ? before.getFullName() : "",
    			(show_all) ? "all" : ""	
    	);
    }

	/**
	 * Gets all the submissions of a particular mutlireddit using the given parameters.
	 *
	 * @param multireddit			Name of the multireddit
	 * @param sort				Sorting method, hot default
	 * @param timeSpan			sorting timespan, null default
	 * @param count				Count at which the submissions are started being numbered
	 * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
	 * @param after				The submission after which needs to be retrieved
	 * @param before			The submission before which needs to be retrieved
	 * @param show_all			Show all (disables filters such as "hide links that I have voted on")
	 * @return 					The linked list containing submissions
	 */
	protected List<RedditItem> ofMultireddit(String multireddit, String sort, String timeSpan, String count, String limit, String after, String before, String show_all ) throws RetrievalFailedException, RedditError {

		// Format parameters
		String params = "";

		params = ParamFormatter.addParameter(params, "t", timeSpan);
		params = ParamFormatter.addParameter(params, "count", count);
		params = ParamFormatter.addParameter(params, "limit", limit);
		params = ParamFormatter.addParameter(params, "after", after);
		params = ParamFormatter.addParameter(params, "before", before);
		params = ParamFormatter.addParameter(params, "show", show_all);

		// Retrieve submissions from the given URL
		return parse(String.format(ApiEndpointUtils.MULTIREDDIT_SUBMISSIONS_GET, multireddit, sort, params), alwaysShowStickied);
	}

	/**
	 * Gets all the submissions of a particular mutlireddit using the given parameters.
	 *
	 * @param multireddit			Name of the multireddit
	 * @param sort				Sorting method, hot default
	 * @param timeSpan			sorting timespan, null default
	 * @param count				Count at which the submissions are started being numbered
	 * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
	 * @param after				The submission after which needs to be retrieved
	 * @param before			The submission before which needs to be retrieved
	 * @param show_all			Show all (disables filters such as "hide links that I have voted on")
	 * @return 					The linked list containing submissions
	 */
	public List<RedditItem> ofMultireddit(String multireddit, SubmissionSort sort, TimeSpan timeSpan, int count, int limit, Submission after, Submission before, boolean show_all) throws RetrievalFailedException, RedditError {

		if(multireddit == null || multireddit.isEmpty()) {
			throw new IllegalArgumentException("The subreddit must be defined.");
		}

		return ofMultireddit(
				multireddit,
				(sort != null) ? sort.value() : "hot",
				(timeSpan != null) ? timeSpan.value() : "",
				String.valueOf(count),
				String.valueOf(limit),
				(after != null) ? after.getFullName() : "",
				(before != null) ? before.getFullName() : "",
				(show_all) ? "all" : ""
		);
	}
    
    /**
     * Searches with the given query using the constraints given as parameters.
     * The parameters here are in Strings instead of wrapper objects, which allows users
     * to manually adjust the parameters (if the API changes and jReddit is not updated
     * in time yet).
     *
	 * @param subreddit 		current subreddit
     * @param query 			The query
     * @param syntax			The query syntax
     * @param sort				Search sorting method
     * @param time				Search time
     * @param count				Count at which the submissions are started being numbered
     * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
     * @param after				The submission after which needs to be retrieved
     * @param before			The submission before which needs to be retrieved
     * @param show				Show all (disables filters such as "hide links that I have voted on")
     * @return 					The linked list containing submissions
     */
    protected List<RedditItem> search(String subreddit, String query, String syntax, String sort, String time, String count, String limit, String after, String before, String show) throws RetrievalFailedException, RedditError {
    	assert query != null && user != null;
    	
    	// Format parameters
    	String params = "";
    	try {
			params = ParamFormatter.addParameter(params, "q", URLEncoder.encode(query, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	params = ParamFormatter.addParameter(params, "syntax", syntax);
    	params = ParamFormatter.addParameter(params, "sort", sort);
    	params = ParamFormatter.addParameter(params, "t", time);
    	params = ParamFormatter.addParameter(params, "count", count);
    	params = ParamFormatter.addParameter(params, "limit", limit);
    	params = ParamFormatter.addParameter(params, "after", after);
    	params = ParamFormatter.addParameter(params, "before", before);
    	params = ParamFormatter.addParameter(params, "show", show);
    	
        // Retrieve submissions from the given URL
		if(subreddit == null)
        	return parse(String.format(ApiEndpointUtils.SUBMISSIONS_SEARCH, params), alwaysShowStickied);
		else {
			params = ParamFormatter.addParameter(params, "restrict_sr", "on");
			return parse(String.format(ApiEndpointUtils.SUBREDDIT_SEARCH, subreddit, params), alwaysShowStickied);
		}
    }
    
    /**
     * Searches with the given query using the constraints given as parameters.
     *
	 * @param subreddit			current subreddit
     * @param query 			The query
     * @param syntax			The query syntax
     * @param sort				Search sorting method
     * @param time				Search time
     * @param count				Count at which the submissions are started being numbered
     * @param limit				Maximum amount of submissions that can be returned (0-100, 25 default (see Reddit API))
     * @param after				The submission after which needs to be retrieved
     * @param before			The submission before which needs to be retrieved
     * @param show_all			Show all (disables filters such as "hide links that I have voted on")
     * @return 					The linked list containing submissions
     */
    public List<RedditItem> search(String subreddit, String query, QuerySyntax syntax, SearchSort sort, TimeSpan time, int count, int limit, Submission after, Submission before, boolean show_all) throws RetrievalFailedException, IllegalArgumentException {
    	
    	if (query == null || query.isEmpty()) {
    		throw new IllegalArgumentException("The query must be defined.");
    	}
    	
    	//if (limit < -1 || limit > RedditConstants.MAX_LIMIT_LISTING) {
    	//	throw new IllegalArgumentException("The limit needs to be between 0 and 100 (or -1 for default).");
    	//}
		if (limit > RedditConstants.MAX_LIMIT_LISTING) {
			limit = RedditConstants.MAX_LIMIT_LISTING;
		}
    	
    	return search(
				subreddit,
    			query, 
    			(syntax != null) ? syntax.value() : "",
    			(sort != null) ? sort.value() : "",
    			(time != null) ? time.value() : "",
    			String.valueOf(count),
    			String.valueOf(limit),
    			(after != null) ? after.getFullName() : "",
    			(before != null) ? before.getFullName() : "",
    			(show_all) ? "all" : ""		
    	);
    }
    
    /**
     * Get the submissions of a user.
     * In this variant all parameters are Strings.
     *
     * @param username	 		Username of the user you want to retrieve from.
     * @param category    		(Optional, set null/empty if not used) Category in the user overview to retrieve submissions from
     * @param sort	    		(Optional, set null/empty if not used) Sorting method.
     * @param count        		(Optional, set null/empty if not used) Number at which the counter starts
     * @param limit        		(Optional, set null/empty if not used) Integer representing the maximum number of comments to return
     * @param after				(Optional, set null/empty if not used) After which comment to retrieve
     * @param before			(Optional, set null/empty if not used) Before which comment to retrieve
     * @param show				(Optional, set null/empty if not used) Show parameter ('given' is only acceptable value)
     * 
     * @return Comments of a user.
     */
    protected List<RedditItem> ofUser(String username, String category, String sort, String count, String limit, String after, String before, String show) throws RetrievalFailedException, RedditError {
    	
    	// Format parameters
    	String params = "";
    	params = ParamFormatter.addParameter(params, "sort", sort);
    	params = ParamFormatter.addParameter(params, "count", count);
    	params = ParamFormatter.addParameter(params, "limit", limit);
    	params = ParamFormatter.addParameter(params, "after", after);
    	params = ParamFormatter.addParameter(params, "before", before);
    	params = ParamFormatter.addParameter(params, "show", show);
    	
        // Retrieve submissions from the given URL
        return parse(String.format(ApiEndpointUtils.USER_SUBMISSIONS_INTERACTION, username, category, params), alwaysShowStickied);
        
    }
    
    /**
     * Get the submissions of a user.
     * In this variant all parameters are Strings.
     *
     * @param username	 		Username of the user you want to retrieve from.
     * @param category    		Category in the user overview to retrieve submissions from
     * @param sort	    		(Optional, set null if not used) Sorting method.
     * @param count        		(Optional, set -1 if not used) Number at which the counter starts
     * @param limit        		(Optional, set -1 if not used) Integer representing the maximum number of comments to return
     * @param after				(Optional, set null if not used) After which comment to retrieve
     * @param before			(Optional, set null if not used) Before which comment to retrieve
     * @param show_given		(Optional, set false if not used) Show parameter ('given' is only acceptable value)
     * 
     * @return Submissions of a user.
     */
    public List<RedditItem> ofUser(String username, UserSubmissionsCategory category, UserOverviewSort sort, int count, int limit, Submission after, Submission before, boolean show_given) throws RetrievalFailedException, IllegalArgumentException {
    	
    	if (username == null || username.isEmpty()) {
    		throw new IllegalArgumentException("The username must be defined.");
    	}

    	if (category == null) {
    		throw new IllegalArgumentException("The category must be defined.");
    	}
    	
    	//if (limit < -1 || limit > RedditConstants.MAX_LIMIT_LISTING) {
    	//	throw new IllegalArgumentException("The limit needs to be between 0 and 100 (or -1 for default).");
    	//}
		if (limit > RedditConstants.MAX_LIMIT_LISTING) {
			limit = RedditConstants.MAX_LIMIT_LISTING;
		}
    	
    	return ofUser(
    			username,
				category.value(),
    			(sort != null) ? sort.value() : "",
    			String.valueOf(count),
    			String.valueOf(limit),
    			(after != null) ? after.getFullName() : "",
    			(before != null) ? before.getFullName() : "",
    			(show_given) ? "given" : ""		
    	);
    }

}