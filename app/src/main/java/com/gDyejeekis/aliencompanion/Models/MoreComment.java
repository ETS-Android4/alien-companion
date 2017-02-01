package com.gDyejeekis.aliencompanion.models;

import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 7/26/2016.
 */
public class MoreComment extends Comment implements MultiLevelExpIndListAdapter.ExpIndData {

    public int getViewType(){
        return PostAdapter.VIEW_TYPE_MORE;
    }

    private boolean loadingMore;
    private List<String> moreCommentIds;

    public MoreComment(JSONObject obj, JSONArray jsonArray) {
        super(obj, jsonArray);
        moreCommentIds = new ArrayList<>();
        for(int i=0;i<jsonArray.size();i++) {
            moreCommentIds.add(jsonArray.get(i).toString());
        }
    }

    public boolean isLoadingMore() {
        return loadingMore;
    }

    public void setLoadingMore(boolean loadingMore) {
        this.loadingMore = loadingMore;
    }

    public List<String> getMoreCommentIds() {
        return moreCommentIds;
    }

    public void setMoreCommentIds(List<String> moreCommentIds) {
        this.moreCommentIds = moreCommentIds;
    }

}
