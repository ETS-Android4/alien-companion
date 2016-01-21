package com.gDyejeekis.aliencompanion.Models;

import android.content.Context;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile implements Serializable {

    private String name;
    private List<String> subreddits;
    private double executeTime;

    public SyncProfile(String name, List<String> subreddits, double executeTime) {
        this.name = name;
        this.subreddits = subreddits;
        this.executeTime = executeTime;
    }

    public void startSync(Context context) {
        if(GeneralUtils.isNetworkAvailable(context)) {
            //start syncing
        }
        else {
            //raise a notification, network unavailable
        }
    }
}
