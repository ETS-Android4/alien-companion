package com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 1/23/2016.
 */
public class MultisListener extends NavDrawerListener {

    public MultisListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layoutToggle) {
            getAdapter().toggleMultiredditItems();
        }
        else if(v.getId() == R.id.layoutEdit) {
            //TODO: start multi edit activity
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
