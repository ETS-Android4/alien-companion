package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by George on 5/28/2015.
 */
public class HttpResponse implements Response {
    private final String responseText;
    private final Object responseObject;
    private final HttpURLConnection connection;

    public HttpResponse(String responseText, Object responseObject, HttpURLConnection connection) {
        this.responseText = responseText;
        this.responseObject = responseObject;
        this.connection = connection;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public String getResponseText() {
        return responseText;
    }

    public int getStatusCode() throws IOException {
        return connection.getResponseCode();
    }

    public HttpURLConnection getConnection() {
        return connection;
    }
}
