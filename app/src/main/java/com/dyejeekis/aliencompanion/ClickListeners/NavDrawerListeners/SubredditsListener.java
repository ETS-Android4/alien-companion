package com.dyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;

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
            ToastUtils.displayShortToast(getActivity(), "Coming soon!");
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
