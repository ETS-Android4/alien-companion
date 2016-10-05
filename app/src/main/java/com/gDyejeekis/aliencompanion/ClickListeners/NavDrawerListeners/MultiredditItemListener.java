package com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMutliredditItem;

/**
 * Created by sound on 1/23/2016.
 */
public class MultiredditItemListener extends NavDrawerListener {

    public MultiredditItemListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerMutliredditItem multireddit = (NavDrawerMutliredditItem) getAdapter().getItemAt(position);
        getAdapter().notifyDataSetChanged();
        getDrawerLayout().closeDrawers();

        PostListFragment listFragment = getActivity().getListFragment();

        listFragment.isMulti = true;
        listFragment.isOther = false;
        listFragment.changeSubreddit(multireddit.getName().toLowerCase());
    }

    @Override
    public boolean onLongClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerMutliredditItem multireddit = (NavDrawerMutliredditItem) getAdapter().getItemAt(position);
        getAdapter().switchModeGracefully(multireddit.getName().toLowerCase(), true);
        return true;
    }
}
