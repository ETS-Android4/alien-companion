package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMutliredditItem;
import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by sound on 1/23/2016.
 */
public class MultiredditItemListener extends NavDrawerListener {

    public MultiredditItemListener(MainActivity activity, RecyclerView.ViewHolder viewHolder) {
        super(activity, viewHolder);
    }

    @Override
    public void onClick(View v) {
        NavDrawerMutliredditItem multireddit =
                (NavDrawerMutliredditItem) getAdapter().getItemAt(getViewHolder().getAdapterPosition());
        getAdapter().notifyDataSetChanged();
        getDrawerLayout().closeDrawers();

        PostListFragment listFragment = getActivity().getListFragment();

        listFragment.isMulti = true;
        listFragment.isOther = false;
        listFragment.changeSubreddit(multireddit.getName().toLowerCase());
    }

    @Override
    public boolean onLongClick(View v) {
        if (MyApplication.longTapSwitchMode) {
            NavDrawerMutliredditItem multireddit =
                    (NavDrawerMutliredditItem) getAdapter().getItemAt(getViewHolder().getAdapterPosition());
            getAdapter().switchModeGracefully(multireddit.getName().toLowerCase(), true);
            return true;
        }
        return false;
    }

}
