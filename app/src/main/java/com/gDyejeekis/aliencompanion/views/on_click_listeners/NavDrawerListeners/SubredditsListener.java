package com.gDyejeekis.aliencompanion.views.on_click_listeners.NavDrawerListeners;

import android.content.Intent;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.EditSubredditsActivity;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;

/**
 * Created by George on 6/26/2015.
 */
public class SubredditsListener extends NavDrawerListener {

    public SubredditsListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layoutToggle) {
            getAdapter().toggleSubredditItems();
        }
        else if(v.getId() == R.id.layoutEdit) {
            Intent intent = new Intent(getActivity(), EditSubredditsActivity.class);
            ArrayList<String> subreddits = (ArrayList) MyApplication.currentAccount.getSubreddits();

            intent.putStringArrayListExtra("subreddits", subreddits);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
