package com.gDyejeekis.aliencompanion.asynctask;

import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Multireddit;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 2/3/2016.
 */
public class RefreshUserMultisTask extends AsyncTask<Void, Void, List<String>> {

    private MainActivity activity;
    private Exception exception;

    public RefreshUserMultisTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected List<String> doInBackground(Void... unused) {
        List<String> multiNames = new ArrayList<>();
        try {
            List<Multireddit> multireddits = MyApplication.currentUser.getMultis(false);
            for (Multireddit multireddit : multireddits) {
                multiNames.add(multireddit.getName());
            }

        } catch (RetrievalFailedException | NullPointerException | RedditError e) {
            exception = e;
        }
        return multiNames;
    }

    @Override
    protected void onPostExecute(List<String> multiNames) {
        if(exception!=null) {
            ToastUtils.displayShortToast(activity, "Error retrieving multis");
            exception.printStackTrace();
        }
        else {
            activity.getNavDrawerAdapter().updateMultiredditItems(multiNames);
            MyApplication.currentAccount.setMultireddits(multiNames);
            GeneralUtils.saveAccountChanges(activity);
        }
    }
}
