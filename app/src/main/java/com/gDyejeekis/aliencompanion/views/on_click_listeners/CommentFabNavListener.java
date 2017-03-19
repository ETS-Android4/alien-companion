package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;

import java.util.List;

/**
 * Created by George on 3/8/2017.
 */

public class CommentFabNavListener implements View.OnClickListener {

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
    private String[] amaUsernames;
    private long timeFilterMilis;
    private int amaModeCurrentIndex;

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        firstSearchResult();
    }

    public void setAmaUsernames(String[] amaUsernames) {
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
        switch (postFragment.commentNavSetting) {
            case threads:
                nextTopParentComment();
                break;
            case ama:
                nextAmaComment();
                break;
            case op:
                nextOpComment();
                break;
            case searchText:
                nextSearchResult();
                break;
            case time:
                nextTimeFiltered();
                break;
            case gilded:
                nextGildedComment();
                break;
            default:
                throw new RuntimeException("Invalid comment nav setting");
        }
    }

    private void previousComment() {
        switch (postFragment.commentNavSetting) {
            case threads:
                previousTopParentComment();
                break;
            case ama:
                previousAmaComment();
                break;
            case op:
                previousOpComment();
                break;
            case searchText:
                previousSearchResult();
                break;
            case time:
                previousTimeFiltered();
                break;
            case gilded:
                previousGildedComment();
                break;
            default:
                throw new RuntimeException("Invalid comment nav setting");
        }
    }

    public void firstTopParentComment() {
        int position = postFragment.postAdapter.getItemCount() > 0 ? 1 : 0;
        scrollToPosition(position);
    }

    private void nextTopParentComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.nextTopParentCommentIndex(start);
        scrollToPosition(index);
    }

    private void previousTopParentComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.previousTopParentCommentIndex(start);
        scrollToPosition(index);
    }

    private void firstSearchResult() {
        // TODO: 3/8/2017
    }

    private void nextSearchResult() {
        // TODO: 3/8/2017
    }

    private void previousSearchResult() {
        // TODO: 3/8/2017
    }

    public void firstGildedComment() {
        int index = postFragment.postAdapter.firstGildedIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No gilded posts/comments found");
        }
    }

    private void nextGildedComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.nextGildedIndex(start);
        scrollToPosition(index);
    }

    private void previousGildedComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.previousGildedIndex(start);
        scrollToPosition(index);
    }

    private void firstTimeFiltered() {
        int index = postFragment.postAdapter.firstTimeFilteredIndex(timeFilterMilis);
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No comments found within the specified time limit");
        }
    }

    private void nextTimeFiltered() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.nextTimeFilteredIndex(start, timeFilterMilis);
        scrollToPosition(index);
    }

    private void previousTimeFiltered() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.previousTimeFilteredIndex(start, timeFilterMilis);
        scrollToPosition(index);
    }

    public void firstOpComment() {
        int index = postFragment.postAdapter.firstOpCommentIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.displayShortToast(postFragment.getActivity(), "No OP comments found");
        }
    }

    private void nextOpComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.nextOpCommentIndex(start);
        scrollToPosition(index);
    }

    private void previousOpComment() {
        int start = firstVisibleItemPosition();
        int index = postFragment.postAdapter.previousOpCommentIndex(start);
        scrollToPosition(index);
    }

    private void firstAmaComment() {
        // TODO: 3/8/2017
    }

    private void nextAmaComment() {
        // TODO: 3/8/2017
    }

    private void previousAmaComment() {
        // TODO: 3/8/2017
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

}
