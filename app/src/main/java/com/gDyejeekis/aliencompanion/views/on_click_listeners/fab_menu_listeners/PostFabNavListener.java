package com.gDyejeekis.aliencompanion.views.on_click_listeners.fab_menu_listeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ShowSyncedDialogFragment;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by George on 3/21/2017.
 */

public class PostFabNavListener implements View.OnClickListener, View.OnLongClickListener {

    private RedditContentFragment fragment;

    public PostFabNavListener(RedditContentFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_nav:
                fragment.toggleFabOptions();
                break;
            case R.id.fab_refresh:
                fragment.refreshList();
                break;
            case R.id.fab_submit:
                fragment.setFabSubmitOptionsVisible(true);
                break;
            case R.id.fab_sync:
                // TODO: 2/24/2017 add abstraction
                ((PostListFragment)fragment).addToSyncQueue();
                break;
            case R.id.fab_hide_read:
                fragment.removeClickedPosts();
                break;
            case R.id.fab_search:
                // TODO: 2/24/2017 add abstraction
                if(fragment instanceof PostListFragment) {
                    ((PostListFragment)fragment).showSearchDialog();
                }
                else if(fragment instanceof SearchFragment) {
                    ((SearchFragment)fragment).showSearchDialog();
                }
                break;
            case R.id.fab_submit_link:
                ((PostListFragment)fragment).startSubmitActivity(SubmitType.link);
                break;
            case R.id.fab_submit_text:
                ((PostListFragment)fragment).startSubmitActivity(SubmitType.self);
                break;
            case R.id.fab_view_synced:
                fragment.showSyncedReddits();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        String message = null;
        switch (v.getId()) {
            case R.id.fab_refresh:
                message = "Refresh posts";
                break;
            case R.id.fab_submit:
                message = "Submit post";
                break;
            case R.id.fab_submit_link:
                message = "Submit link";
                break;
            case R.id.fab_submit_text:
                message = "Submit text";
                break;
            case R.id.fab_search:
                message = "Search reddit";
                break;
            case R.id.fab_hide_read:
                message = "Hide read posts and go to top";
                break;
            case R.id.fab_sync:
                message = "Sync posts";
                break;
            case R.id.fab_view_synced:
                message = "View synced subreddits/multireddits";
                break;
        }
        if(message!=null) {
            //ToastUtils.showToast(fragment.getActivity(), message);
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message);
            return true;
        }
        return false;
    }
}
