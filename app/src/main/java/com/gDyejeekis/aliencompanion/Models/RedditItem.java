package com.gDyejeekis.aliencompanion.Models;

import android.text.SpannableStringBuilder;

/**
 * Created by sound on 8/28/2015.
 */
public interface RedditItem {

    //public SpannableStringBuilder preparedText = null;

    public int getViewType();

    public String getThumbnail();

    public void setThumbnailObject(Thumbnail thumbnailedObject);

    public Thumbnail getThumbnailObject();

    public String getMainText();

    public SpannableStringBuilder getPreparedText();

    public void storePreparedText(SpannableStringBuilder stringBuilder);
}
