package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 3/11/2016.
 */
public class GfycatTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "GfycatTask";

    private Context context;

    public GfycatTask(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return GeneralUtils.getGfycatMobileUrl(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
