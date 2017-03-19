package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import java.util.List;

/**
 * Created by George on 3/8/2017.
 */

public class CommentFabNavListener implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "CommentNavListener";

    public static final String NO_ADDITIONAL_ITEMS_MESSAGE = "No additional matching comments";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_nav:
                postFragment.toggleFabNavOptions();
                break;
            case R.id.fab_comment_nav_setting:
                postFragment.showCommentNavDialog();
                break;
            case R.id.fab_reply:
                postFragment.submitComment();
                break;
            case R.id.fab_up:
                previousComment();
                break;
            case R.id.fab_down:
                nextComment();
                break;
        }
    }

    private PostFragment  postFragment;

    private String searchQuery;
    public boolean matchCase;
    private List<String> amaUsernames;
    private long timeFilterMilis;
    private int currentAmaIndex;

    public void setSearchQuery(String searchQuery, boolean matchCase) {
        this.searchQuery = searchQuery;
        this.matchCase = matchCase;
        firstSearchResult();
    }

    public void setAmaUsernames(List<String> amaUsernames) {
        this.amaUsernames = amaUsernames;
        firstAmaComment();
    }

    public void setTimeFilterMilis(long timeFilterMilis) {
        this.timeFilterMilis = timeFilterMilis;
        firstTimeFiltered();
    }

    public CommentFabNavListener(PostFragment postFragment) {
        this.postFragment = postFragment;
    }

    private void nextComment() {
        int start = firstVisibleItemPosition();
        int index;
        switch (postFragment.commentNavSetting) {
            case threads:
                index = postFragment.postAdapter.nextTopParentCommentIndex(start);
                break;
            case ama:
                index = postFragment.postAdapter.nextAmaIndex(start, currentAmaIndex, amaUsernames);
                currentAmaIndex = index;
                break;
            case op:
                index = postFragment.postAdapter.nextOpCommentIndex(start);
                break;
            case searchText:
                index = postFragment.postAdapter.nextSearchResultIndex(start, searchQuery, matchCase);
                break;
            case time:
                index = postFragment.postAdapter.nextTimeFilteredIndex(start, timeFilterMilis);
                break;
            case gilded:
                index = postFragment.postAdapter.nextGildedIndex(start);
                break;
            default:
                index = PostAdapter.POSITION_NOT_FOUND;
                Log.e(TAG, "Invalid comment nav setting");
        }
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), NO_ADDITIONAL_ITEMS_MESSAGE);
        }
    }

    private void previousComment() {
        int start = firstVisibleItemPosition();
        int index;
        switch (postFragment.commentNavSetting) {
            case threads:
                index = postFragment.postAdapter.previousTopParentCommentIndex(start);
                break;
            case ama:
                index = postFragment.postAdapter.previousAmaIndex(start, currentAmaIndex, amaUsernames);
                currentAmaIndex = index;
                break;
            case op:
                index = postFragment.postAdapter.previousOpCommentIndex(start);
                break;
            case searchText:
                index = postFragment.postAdapter.previousSearchResultIndex(start, searchQuery, matchCase);
                break;
            case time:
                index = postFragment.postAdapter.previousTimeFilteredIndex(start, timeFilterMilis);
                break;
            case gilded:
                index = postFragment.postAdapter.previousGildedIndex(start);
                break;
            default:
                index = PostAdapter.POSITION_NOT_FOUND;
                Log.e(TAG, "Invalid comment nav setting");
        }
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), NO_ADDITIONAL_ITEMS_MESSAGE);
        }
    }

    public void firstTopParentComment() {
        int position = postFragment.postAdapter.getItemCount() > 0 ? 1 : 0;
        scrollToPosition(position);
    }

    private void firstSearchResult() {
        int index = postFragment.postAdapter.firstSearchResultIndex(searchQuery, matchCase);
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "Text not found in thread");
        }
    }

    public void firstGildedComment() {
        int index = postFragment.postAdapter.firstGildedIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No gilded posts/comments found");
        }
    }

    private void firstTimeFiltered() {
        int index = postFragment.postAdapter.firstTimeFilteredIndex(timeFilterMilis);
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No comments found within the specified time limit");
        }
    }

    public void firstOpComment() {
        int index = postFragment.postAdapter.firstOpCommentIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No OP comments found");
        }
    }

    private void firstAmaComment() {
        int index = postFragment.postAdapter.firstAmaIndex(amaUsernames);
        currentAmaIndex = index;
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No comments found from listed users");
        }
    }

    private boolean scrollToPosition(int position) {
        if(position != PostAdapter.POSITION_NOT_FOUND) {
            postFragment.mLayoutManager.scrollToPositionWithOffset(position, 0);
            return true;
        }
        return false;
    }

    private int firstVisibleItemPosition() {
        return postFragment.mLayoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO: 3/19/2017
        return false;
    }
}
