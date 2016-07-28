package com.gDyejeekis.aliencompanion.Models;

/**
 * Created by sound on 8/28/2015.
 */
public interface RedditItem {

    public int getViewType();

    public String getThumbnail();

    public void setThumbnailObject(Thumbnail thumbnailedObject);

    public Thumbnail getThumbnailObject();

    public String getMainText();

}
