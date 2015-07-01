package com.george.redditreader.api.utils.restClient;

import java.io.IOException;

public interface Response {

    /**
     * @return <code>int</code> the Http response code
     */
    int getStatusCode() throws IOException;

    /**
     * @return <code>Object</code> the JSONSimple interpretation of the response body
     */
    Object getResponseObject();

    /**
     * @return <code>String</code> the response body
     */
    String getResponseText();
}
