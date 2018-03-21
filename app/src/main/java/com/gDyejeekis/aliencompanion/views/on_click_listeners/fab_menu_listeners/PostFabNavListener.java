package com.gDyejeekis.aliencompanion.views.on_click_listeners.fab_menu_listeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ShowSyncedDialogFragment;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.File;

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
                ((PostListFragment)fragment).submitPost(true);
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
                message = "Sync now";
                String lastSynced = getLastSynced();
                if (lastSynced!=null) {
                    message += " (last synced " + lastSynced + ")";
                }
                break;
            case R.id.fab_view_synced:
                message = "View synced subreddits/multireddits";
                break;
        }
        if(message!=null) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message);
            return true;
        }
        return false;
    }

    private String getLastSynced() {
        try {
            if (fragment instanceof PostListFragment) {
                String subreddit = ((PostListFragment) fragment).subreddit;
                String name = subreddit==null ? "frontpage" : subreddit.toLowerCase();
                File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(fragment.getContext()), name);
                File file = new File(dir, name + AppConstants.SYNCED_POST_LIST_SUFFIX);
                if (file.exists()) {
                    return ConvertUtils.getSubmissionAge((double) file.lastModified() / 1000);
                }
            }
        } catch (Exception e) {}
        return null;
    }

}
