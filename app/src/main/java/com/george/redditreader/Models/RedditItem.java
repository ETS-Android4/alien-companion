package com.george.redditreader.Models;

import com.george.redditreader.api.entity.Comment;

import java.util.List;

/**
 * Created by sound on 8/28/2015.
 */
public interface RedditItem {

    public int getViewType();

    public String getThumbnail();

    public void setThumbnailObject(Thumbnail thumbnailedObject);
}
