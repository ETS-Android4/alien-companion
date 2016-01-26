package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import okhttp3.RequestBody;

public interface HttpClient {

    /**
     * Perform a get request to the Url specified using the cookie specified
     *
     * @param baseUrl The base url
     * @param urlPath The url to make a get request to
     * @param cookie The cookie to use when making the request
     *
     * @return <code>Response</code> an object conforming to the Response interface
     */
    Response get(String baseUrl, String urlPath, String cookie);

    /**
     * Perform a post request to the Url specified using the cookie specified
     *
     * @param baseUrl The base url
     * @param apiParams Name value pairs to be posted to the url
     * @param urlPath The url to make a get request to
     * @param cookie The cookie to use when making the request
     *
     * @return <code>Response</code> an object conforming to the Response interface
     */
    Response post(String baseUrl, RequestBody body, String urlPath, String cookie);


    /**
     * Set the userAgent to be used when making http requests
     *
     * @param agent the string to be used as the userAgent
     */
    void setUserAgent(String agent);
}
