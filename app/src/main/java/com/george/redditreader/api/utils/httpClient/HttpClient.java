package com.george.redditreader.api.utils.httpClient;

public interface HttpClient {

    /**
     * Perform a get request to the Url specified using the cookie specified
     *
     * @param urlPath The url to make a get request to
     * @param cookie The cookie to use when making the request
     *
     * @return <code>Response</code> an object conforming to the Response interface
     */
    Response get(String urlPath, String cookie);

    /**
     * Perform a post request to the Url specified using the cookie specified
     *
     * @param apiParams Name value pairs to be posted to the url
     * @param urlPath The url to make a get request to
     * @param cookie The cookie to use when making the request
     *
     * @return <code>Response</code> an object conforming to the Response interface
     */
    Response post(String apiParams, String urlPath, String cookie);


    /**
     * Set the userAgent to be used when making http requests
     *
     * @param agent the string to be used as the userAgent
     */
    void setUserAgent(String agent);
}
