package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;

import org.apache.commons.lang.StringUtils;

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
                index = postFragment.postAdapter.nextTimeFilteredIndex(start, timestampThresholdMilis);
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
                index = postFragment.postAdapter.previousTimeFilteredIndex(start, timestampThresholdMilis);
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
        int index = postFragment.postAdapter.firstTimeFilteredIndex(timestampThresholdMilis);
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

    public String getAmaUsernamesString() {
        if(amaUsernames!=null && amaUsernames.size()>0) {
            return StringUtils.join(amaUsernames.toArray(), ", ");
        }
        return null;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_reply:
                ToastUtils.displayShortToast(postFragment.getActivity(), "Submit a comment");
                return true;
            case R.id.fab_comment_nav_setting:
                String toastMsg = "Navigate between ";
                switch (postFragment.commentNavSetting) {
                    case threads:
                        toastMsg += "top level parent comments";
                        break;
                    case searchText:
                        toastMsg += "text results";
                        if(searchQuery!=null) {
                            toastMsg += " for '" + searchQuery + "'";
                        }
                        break;
                    case time:
                        toastMsg += timeFilterString == null ? "time filtered comments" : "comments submitted in the last " + timeFilterString;
                        break;
                    case op:
                        toastMsg += "comments submitted by the original poster";
                        break;
                    case gilded:
                        toastMsg += "gilded posts and comments";
                        break;
                    case ama:
                        toastMsg += "questions and answers";
                        String usersString = getAmaUsernamesString();
                        if(usersString!=null) {
                            toastMsg += " - Selected AMA participants: " + usersString;
                        }
                        break;
                }
                ToastUtils.displayShortToast(postFragment.getActivity(), toastMsg);
                return true;
        }
        return false;
    }
}
