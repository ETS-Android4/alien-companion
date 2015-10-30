package com.dyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.content.Intent;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.EditSubredditsActivity;
import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;

import java.util.ArrayList;
import java.util.Collections;

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
        else if(v.getId() == R.id.layoutEdit){
            //ToastUtils.displayShortToast(getActivity(), "Coming soon!");
            Intent intent = new Intent(getActivity(), EditSubredditsActivity.class);
            ArrayList<String> subreddits;
            if(MainActivity.currentUser != null) subreddits = MainActivity.currentAccount.getSubredditsArraylist();
            else {
                subreddits = new ArrayList<>();
                Collections.addAll(subreddits, RedditConstants.defaultSubscribed);  //TODO: set proper list
            }
            intent.putStringArrayListExtra("subreddits", subreddits);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
