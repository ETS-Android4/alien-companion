package com.gDyejeekis.aliencompanion.models;

/**
 * Created by sound on 8/28/2015.
 */
public interface RedditItem {

    public String getIdentifier();

    public int getViewType();

    public String getThumbnail();

    public void setThumbnailObject(Thumbnail thumbnailedObject);

    public Thumbnail getThumbnailObject();

    public String getMainText();

}
