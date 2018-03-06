package com.gDyejeekis.aliencompanion.api.utils;

import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

public class RedditConstants {

	public static final int MAX_LIMIT_LISTING = 100;
	public static final int DEFAULT_LIMIT = 25;
	public static final int MAX_LIMIT_COMMENTS = 100;

	public static final int MAX_COMMENT_DEPTH = 4;

	public static final CommentSort DEFAULT_COMMENT_SORT = CommentSort.TOP;
	
	/**
	 * Approximately the maximum listing size, including pagination until
	 * the end. This differs from request to request, but after some observations
	 * this is a nice upper bound.
	 */
	public static final int APPROXIMATE_MAX_LISTING_AMOUNT = 1300;

	public static final String[] defaultSubredditStrings = {"All", "popular", "pics", "videos", "gaming", "technology",
			"movies", "iama", "askreddit", "aww", "worldnews", "books", "music"};
}
