package com.gDyejeekis.aliencompanion.views.on_click_listeners.fab_menu_listeners;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;

import org.apache.commons.lang3.StringUtils;

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
                fragment.toggleFabNavOptions();
                break;
            case R.id.fab_comment_nav_setting:
                fragment.showCommentNavDialog();
                break;
            case R.id.fab_reply:
                fragment.submitComment();
                break;
            case R.id.fab_up:
                previousComment();
                break;
            case R.id.fab_down:
                nextComment();
                break;
        }
    }

    private PostFragment fragment;

    public String searchQuery;
    public boolean matchCase;
    private List<String> amaUsernames;
    private String timeFilterString;
    private long timestampThresholdMilis;
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

    public void setTimeFilter(int hours, int minutes, int seconds) {
        long selectedTimeMilis = (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000);
        this.timestampThresholdMilis = System.currentTimeMillis() - selectedTimeMilis;
        this.timeFilterString = ConvertUtils.getHrsMinsSecsString(hours, minutes, seconds);
        firstTimeFiltered();
    }

    public CommentFabNavListener(PostFragment postFragment) {
        this.fragment = postFragment;
    }

    public void nextComment() {
        int start = firstVisibleItemPosition();
        int index;
        switch (fragment.commentNavSetting) {
            case threads:
                index = fragment.postAdapter.nextTopParentCommentIndex(start);
                break;
            case ama:
                index = fragment.postAdapter.nextAmaIndex(start, currentAmaIndex, amaUsernames);
                currentAmaIndex = index;
                break;
            case op:
                index = fragment.postAdapter.nextOpCommentIndex(start);
                break;
            case searchText:
                index = fragment.postAdapter.nextSearchResultIndex(start, searchQuery, matchCase);
                break;
            case time:
                index = fragment.postAdapter.nextTimeFilteredIndex(start, timestampThresholdMilis);
                break;
            case gilded:
                index = fragment.postAdapter.nextGildedIndex(start);
                break;
            default:
                index = PostAdapter.POSITION_NOT_FOUND;
                Log.e(TAG, "Invalid comment nav setting");
        }
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), NO_ADDITIONAL_ITEMS_MESSAGE);
        }
    }

    public void previousComment() {
        int start = firstVisibleItemPosition();
        int index;
        switch (fragment.commentNavSetting) {
            case threads:
                index = fragment.postAdapter.previousTopParentCommentIndex(start);
                break;
            case ama:
                index = fragment.postAdapter.previousAmaIndex(start, currentAmaIndex, amaUsernames);
                currentAmaIndex = index;
                break;
            case op:
                index = fragment.postAdapter.previousOpCommentIndex(start);
                break;
            case searchText:
                index = fragment.postAdapter.previousSearchResultIndex(start, searchQuery, matchCase);
                break;
            case time:
                index = fragment.postAdapter.previousTimeFilteredIndex(start, timestampThresholdMilis);
                break;
            case gilded:
                index = fragment.postAdapter.previousGildedIndex(start);
                break;
            default:
                index = PostAdapter.POSITION_NOT_FOUND;
                Log.e(TAG, "Invalid comment nav setting");
        }
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), NO_ADDITIONAL_ITEMS_MESSAGE);
        }
    }

    public void firstTopParentComment() {
        int position = fragment.postAdapter.getItemCount() > 0 ? 1 : 0;
        scrollToPosition(position);
    }

    private void firstSearchResult() {
        int index = fragment.postAdapter.firstSearchResultIndex(searchQuery, matchCase);
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "Text not found in thread");
        }
    }

    public void firstGildedComment() {
        int index = fragment.postAdapter.firstGildedIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "No gilded posts/comments found");
        }
    }

    private void firstTimeFiltered() {
        int index = fragment.postAdapter.firstTimeFilteredIndex(timestampThresholdMilis);
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "No comments found within the specified time limit");
        }
    }

    public void firstOpComment() {
        int index = fragment.postAdapter.firstOpCommentIndex();
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "No comments found from original poster");
        }
    }

    private void firstAmaComment() {
        int index = fragment.postAdapter.firstAmaIndex(amaUsernames);
        currentAmaIndex = index;
        boolean scrolled = scrollToPosition(index);
        if(!scrolled) {
            ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "No comments found from listed users");
        }
    }

    private boolean scrollToPosition(int position) {
        if(position != PostAdapter.POSITION_NOT_FOUND) {
            fragment.mLayoutManager.scrollToPositionWithOffset(position, 0);
            return true;
        }
        return false;
    }

    private int firstVisibleItemPosition() {
        return fragment.mLayoutManager.findFirstVisibleItemPosition();
    }

    public String getAmaUsernamesString() {
        if(amaUsernames!=null && amaUsernames.size()>0) {
            return StringUtils.join(amaUsernames, ", ");
        }
        return null;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_reply:
                ToastUtils.showSnackbar(fragment.getSnackbarParentView(), "Submit a comment");
                return true;
            case R.id.fab_comment_nav_setting:
                String message = "Navigate between ";
                int length = Snackbar.LENGTH_SHORT;
                switch (fragment.commentNavSetting) {
                    case threads:
                        message += "top level parent comments";
                        break;
                    case searchText:
                        message += "text results";
                        if(searchQuery!=null) {
                            message += " for \'" + searchQuery + "\'";
                            length = Snackbar.LENGTH_LONG;
                        }
                        break;
                    case time:
                        message += timeFilterString == null ? "time filtered comments" : "comments submitted in the last " + timeFilterString;
                        if(timeFilterString!=null) {
                            length = Snackbar.LENGTH_LONG;
                        }
                        break;
                    case op:
                        message += "comments submitted by the original poster";
                        break;
                    case gilded:
                        message += "gilded posts and comments";
                        break;
                    case ama:
                        message += "questions and answers";
                        String usersString = getAmaUsernamesString();
                        if(usersString!=null) {
                            message += " - Selected AMA participants: " + usersString;
                            length = Snackbar.LENGTH_LONG;
                        }
                        break;
                }
                ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message, length);
                return true;
        }
        return false;
    }
}
