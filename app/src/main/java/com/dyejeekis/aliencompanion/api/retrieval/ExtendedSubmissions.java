package com.dyejeekis.aliencompanion.api.retrieval;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.retrieval.params.QuerySyntax;
import com.dyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;

public class ExtendedSubmissions {
	
	private Submissions submissions;

	public ExtendedSubmissions(Submissions submissions) {
		this.submissions = submissions;
	}


    /**
     * Get submissions from the specified subreddit after a specific submission, as the given user, 
     * attempting to retrieve the desired amount.
     * 
     * @param redditName 		Subreddit name (e.g. 'fun', 'wtf', 'programming')
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @param after				Submission after which the submissions need to be fetched.
     * @return					List of the submissions
     */
    public List<RedditItem> ofSubreddit(String redditName, SubmissionSort sort, int amount, Submission after) {
    	
    	if (amount < 0) {
    		System.err.println("You cannot retrieve a negative amount of submissions.");
    		return null;
    	}

    	// List of submissions
        List<RedditItem> result = new LinkedList<>();

        // Do all iterations
        int counter = 0;
		while (amount >= 0) {
			
			// Determine how much still to retrieve in this iteration
			int limit = (amount < RedditConstants.MAX_LIMIT_LISTING) ? amount : RedditConstants.MAX_LIMIT_LISTING;
			amount -= limit;
			
			// Retrieve submissions
			List<RedditItem> subresult = submissions.ofSubreddit(redditName, sort, null, counter, limit, after, null, true);//TODO: modify to add support for timespan sorting
			if (subresult == null) {
				return new ArrayList<>();
			}
			result.addAll(subresult);
			
			// Increment counter
			counter += limit;
			
			// If the end of the submission stream has been reached
			if (subresult.size() < limit) {
				// System.out.println("API Stream finished prematurely: received " + subresult.size() + " but wanted " + limit + ".");
				break;
			}
			
			// If nothing is left desired, exit.
			if (amount <= 0) {
				break;
			}
			
			// Previous last submission
			after = (Submission) subresult.get(subresult.size() - 1);
			
		}
		
		return result;
    	
    }
    
    /**
     * Get submissions from the specified subreddit, as the given user, 
     * attempting to retrieve the desired amount after the given submission.
     * 
     * @param redditName 		Subreddit name (e.g. 'fun', 'wtf', 'programming')
     * @param sort				Subreddit sorting method
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @param after				Submission after which to get
     * @return					List of the submissions
     */
    public List<RedditItem> get(String redditName, SubmissionSort sort, int amount, Submission after) {
    	return ofSubreddit(redditName, sort, amount, after);
    }
    
    /**
     * Get submissions from the specified subreddit, as the specified user, using the given sorting method.
     * 
     * @param redditName 	The subreddit at which submissions you want to retrieve submissions.
     * @param sort			Subreddit sorting method
     * @return <code>List</code> of submissions on the subreddit.
     */
    public List<RedditItem> ofSubreddit(String redditName, SubmissionSort sort) {
    	return ofSubreddit(redditName, sort, RedditConstants.APPROXIMATE_MAX_LISTING_AMOUNT, null);
    }
    
    /**
     * Get submissions from the specified subreddit attempting to retrieve the desired amount.
     * @param redditName 		Subreddit name (e.g. 'fun', 'wtf', 'programming')
     * @param sort				Subreddit sorting method
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @return					List of the submissions
     */
    public List<RedditItem> ofSubreddit(String redditName, SubmissionSort sort, int amount) {
    	return ofSubreddit(redditName, sort, amount, null);
    }
    
    /**
     * Get submissions from the specified subreddit after a specific submission, as the given user, attempting to retrieve the desired amount.
     * 
     * @param query 			Search query
     * @param sort				Search sorting method (e.g. new or top)
     * @param time				Search time (e.g. day or all)
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @param after				Submission after which the submissions need to be fetched.
     * @return					List of the submissions
     */
    public List<RedditItem> search(String query, SearchSort sort, TimeSpan time, int amount, Submission after) {
    	
    	if (amount < 0) {
    		System.err.println("You cannot retrieve a negative amount of submissions.");
    		return null;
    	}

    	// List of submissions
        List<RedditItem> result = new LinkedList<>();

        // Do all iterations
        int counter = 0;
		while (amount >= 0) {
			
			// Determine how much still to retrieve in this iteration
			int limit = (amount < RedditConstants.MAX_LIMIT_LISTING) ? amount : RedditConstants.MAX_LIMIT_LISTING;
			amount -= limit;
			
			// Retrieve submissions
			List<RedditItem> subresult = submissions.search(null, query, QuerySyntax.LUCENE, sort, time, counter, limit, after, null, true); //TODO: modify to support subreddit restriction
			if (subresult == null) {
				return new ArrayList<>();
			}
			result.addAll(subresult);
			
			// Increment counter
			counter += limit;
			
			// If the end of the submission stream has been reached
			if (subresult.size() != limit) {
				System.out.println("API Stream finished prematurely: received " + subresult.size() + " but wanted " + limit + ".");
				break;
			}
			
			// If nothing is left desired, exit.
			if (amount <= 0) {
				break;
			}
			
			// Previous last submission
			after = (Submission) subresult.get(subresult.size() - 1);
			
		}
		
		return result;
    	
    }
    
    /**
     * Search for submissions using the query with the given sorting method and within the given time as the given user and with maximum amount returned.
	 * 
     * @param query 	Search query
     * @param sort		Search sorting method
     * @param time		Search time
     * @param amount	How many to retrieve (if possible, result <= amount guaranteed)
     * @return <code>List</code> of submissions that match the query.
     */
    public List<RedditItem> search(String query, SearchSort sort, TimeSpan time, int amount) {
    	return search(query, sort, time, amount, null);
    }
    
    /**
     * Search for submissions using the query with the given sorting method and within the given time as the given user.
	 * 
     * @param query 	Search query
     * @param sort		Search sorting method
     * @param time		Search time
     * @return <code>List</code> of submissions that match the query.
     */
    public List<RedditItem> search(String query, SearchSort sort, TimeSpan time) {
    	return search(query, sort, time, RedditConstants.APPROXIMATE_MAX_LISTING_AMOUNT);
    }

    
    /**
     * Get submissions from the specified user.
     * 
     * @param username 			Username
     * @param category			UserSubmissionsCategory
     * @param sort				Search sorting method (e.g. new or top)
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @param after				Submission after which the submissions need to be fetched.
     * @return					List of the submissions
     */
    public List<RedditItem> ofUser(String username, UserSubmissionsCategory category, UserOverviewSort sort, int amount, Submission after) {
    	
    	if (amount < 0) {
    		System.err.println("You cannot retrieve a negative amount of submissions.");
    		return null;
    	}

    	// List of submissions
        List<RedditItem> result = new LinkedList<>();

        // Do all iterations
        int counter = 0;
		while (amount >= 0) {
			
			// Determine how much still to retrieve in this iteration
			int limit = (amount < RedditConstants.MAX_LIMIT_LISTING) ? amount : RedditConstants.MAX_LIMIT_LISTING;
			amount -= limit;
			
			// Retrieve submissions
			List<RedditItem> subresult = submissions.ofUser(username, category, sort, counter, limit, after, null, true);
			if (subresult == null) {
				return new ArrayList<>();
			}
			result.addAll(subresult);
			
			// Increment counter
			counter += limit;
			
			// If the end of the submission stream has been reached
			if (subresult.size() != limit) {
				// System.out.println("API Stream finished prematurely: received " + subresult.size() + " but wanted " + limit + ".");
				break;
			}
			
			// If nothing is left desired, exit.
			if (amount <= 0) {
				break;
			}
			
			// Previous last submission
			after = (Submission) subresult.get(subresult.size() - 1);
			
		}
		
		return result;
    	
    }
    
    /**
     * Get submissions from the specified user.
     * 
     * @param username 			Username
     * @param category			UserSubmissionsCategory
     * @param sort				Search sorting method (e.g. new or top)
     * @param amount			Desired amount which will be attempted. No guarantee! See request limits.
     * @return					List of the submissions
     */
    public List<RedditItem> ofUser(String username, UserSubmissionsCategory category, UserOverviewSort sort, int amount) {
    	return ofUser(username, category, sort, amount, null);
    }
	
}
