package com.gDyejeekis.aliencompanion.api.utils;


import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditOAuth;

/**
 * Some constants that are used for specifying Reddit API endpoints (example: /api/new_captcha)
 *
 * @author Andrei Sfat
 */
public class ApiEndpointUtils {

    public static final String REDDIT_BASE_URL_OAUTH = "https://oauth.reddit.com";

    public static final String REDDIT_BASE_URL = (RedditOAuth.useOAuth2) ? REDDIT_BASE_URL_OAUTH : "http://www.reddit.com";

    public static final String REDDIT_BASE_API_ENDPOINT = "/api";

    /* Captcha specific constants */
    
    public static final String CAPTCHA_NEW = REDDIT_BASE_API_ENDPOINT + "/new_captcha";

    public static final String CAPTCHA_NEEDS = REDDIT_BASE_API_ENDPOINT + "/needs_captcha.json";
    
    /* Message specific constants */

    public static final String MESSAGE_API_ENDPOINT = "/message";

    public static final String MESSAGE_ALL = MESSAGE_API_ENDPOINT + "/inbox.json?%s";

    public static final String MESSAGE_UNREAD = MESSAGE_API_ENDPOINT + "/unread.json?%s";

    public static final String MESSAGE_MESSAGES = MESSAGE_API_ENDPOINT + "/messages.json?%s";

    public static final String MESSAGE_COMMENT_REPLIES = MESSAGE_API_ENDPOINT + "/comments.json?%s";

    public static final String MESSAGE_POST_REPLIES = MESSAGE_API_ENDPOINT + "/selfreply.json?%s";

    public static final String MESSAGE_USERNAME_MENTIONS = MESSAGE_API_ENDPOINT + "/mentions.json?%s";

    public static final String MESSAGE_SENT = MESSAGE_API_ENDPOINT + "/sent.json?%s";
    
    public static final String MESSAGE_COMPOSE = REDDIT_BASE_API_ENDPOINT + "/compose";
    //
    //public static final String MESSAGE_READ = REDDIT_BASE_API_ENDPOINT + "/read_message";
//
    //public static final String MESSAGE_GET = "/message/%s.json";
    //
    //public static final String BLOCK = REDDIT_BASE_API_ENDPOINT + "/block";
    
    /* API functions */
    
    public static final String COMMENT = REDDIT_BASE_API_ENDPOINT + "/comment";
    
    public static final String EDITUSERTEXT = REDDIT_BASE_API_ENDPOINT + "/editusertext";
    
    public static final String SUBMISSION_MARK_AS_NSFW = REDDIT_BASE_API_ENDPOINT + "/marknsfw";
    
    public static final String SUBMISSION_UNMARK_AS_NSFW = REDDIT_BASE_API_ENDPOINT + "/unmarknsfw";
    
    public static final String VOTE = REDDIT_BASE_API_ENDPOINT + "/vote";

    public static final String SAVE = REDDIT_BASE_API_ENDPOINT + "/save";

    public static final String UNSAVE = REDDIT_BASE_API_ENDPOINT + "/unsave";
    
    public static final String HIDE = REDDIT_BASE_API_ENDPOINT + "/hide";
    
    public static final String UNHIDE = REDDIT_BASE_API_ENDPOINT + "/unhide";
    
    public static final String DELETE = REDDIT_BASE_API_ENDPOINT + "/del";
    
    public static final String REPORT = REDDIT_BASE_API_ENDPOINT + "/report";

    public static final String SUBMISSION_COMMENTS = "/comments/%s.json?%s";
    
    public static final String SUBMISSIONS_SEARCH = "/search.json?%s";

    public static final String SUBREDDIT_SEARCH = "/r/%s/search.json?%s";
    
    public static final String SUBMISSIONS_GET = "/r/%s/%s.json?%s";

    public static final String SUBMISSIONS_GET_FRONT = "/%s.json?%s";

    /* Flair specific constants */

    public static final String USER_DELETE_SUBREDDIT_FLAIR = "/r/%s/api/deleteflair";

    public static final String CLEAR_SUBREDDIT_FLAIR_TEMPLATES = "/r/%s/api/clearflairtemplates";

    public static final String DELETE_SUBREDDIT_FLAIR_TEMPLATE = "/r/%s/api/deleteflairtemplate";

    public static final String FLAIR = "/r/%s/api/flair";

    public static final String FLAIR_CONFIG = "/r/%s/api/flairconfig";

    public static final String FLAIR_LIST = "/r/%s/api/flairlist";

    public static final String FLAIR_SELECTOR = "/r/%s/api/flairselector";

    public static final String FLAIR_TEMPLATE = "/r/%s/api/flairtemplate";

    public static final String SELECT_FLAIR = "/r/%s/api/selectflair";

    public static final String FLAIR_ENABLED = "/r/%s/api/setflairenabled";
    
    /* User specific constants */
    
    public static final String USER_LOGIN = REDDIT_BASE_API_ENDPOINT + "/login/%s";
    
    public static final String USER_SUBMIT = REDDIT_BASE_API_ENDPOINT + "/submit";

    public static final String USER_ABOUT = "/user/%s/about.json";

    public static final String USER_TROPHIES = "/user/%s/trophies.json";

    //public static final String USER_OVERVIEW = "/user/%s/overview.json?%s";
    //
    //public static final String USER_GILDED = "/user/%s/gilded.json?%s";

    public static final String USER_MIXED = "/user/%s/%s.json?%s";

    public static final String USER_COMMENTS = "/user/%s/comments.json?%s";

    public static final String USER_GET_SUBSCRIBED = "/subreddits/mine/subscriber.json";

    public static final String USER_GET_CONTRIBUTED_TO = "/subreddits/mine/contributor.json";

    public static final String USER_GET_MODERATOR_OF = "/subreddits/mine/moderator.json";

    public static final String USER_SUBMISSIONS_INTERACTION = "/user/%s/%s.json?%s";

    public static final String USER_SUBMISSIONS = "/user/%s/submitted.json";

    public static final String USER_INFO = REDDIT_BASE_API_ENDPOINT + "/me.json";
    
    public static final String USER_UPDATE = REDDIT_BASE_API_ENDPOINT + "/update";
	
    public static final String USER_DELETE = REDDIT_BASE_API_ENDPOINT + "/delete_user";
    
    public static final String USER_REGISTER = REDDIT_BASE_API_ENDPOINT + "/register";

    public static final String USER_SUBSCRIBE = REDDIT_BASE_API_ENDPOINT + "/subscribe";

    /* Subreddits specific constants */

    public static final String SUBREDDITS = "/subreddits.json";

    public static final String SUBREDDITS_GET = "/subreddits/%s.json?%s";
    
    public static final String SUBREDDITS_SEARCH = "/subreddits/search.json?%s";
    
    /* Live Thread specific constants */
    
    public static final String LIVE_THREAD_CREATE = REDDIT_BASE_API_ENDPOINT + "/live/create";
    
    public static final String LIVE_THREAD_UPDATE = REDDIT_BASE_API_ENDPOINT + "/live/%s/update";

}
