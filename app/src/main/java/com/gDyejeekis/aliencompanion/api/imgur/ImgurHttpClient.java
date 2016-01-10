package com.gDyejeekis.aliencompanion.api.imgur;

import android.util.Log;

import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpResponse;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.Response;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by George on 1/3/2016.
 */
public class ImgurHttpClient {

    public static final String CLIENT_ID = "438a0d502455ec6";

    public Response get(String urlPath) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d("inputstream imgur: ", content);
            Object responseObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, responseObject, connection);

            //printHeaderFields(connection);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null) connection.disconnect();
        }

        return null;
    }
}
