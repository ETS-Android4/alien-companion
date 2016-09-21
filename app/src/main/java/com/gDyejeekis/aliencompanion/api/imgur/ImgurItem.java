package com.gDyejeekis.aliencompanion.api.imgur;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sound on 3/9/2016.
 */
public abstract class ImgurItem implements Serializable {

    private static final long serialVersionUID = 1234524L;

    protected String id;
    protected String title;
    protected String description;

    public abstract boolean isAlbum();

    public abstract List<ImgurImage> getImages();

    public abstract void setImages(List<ImgurImage> images);

    public boolean hasInfo() {
        if(title != null && !title.isEmpty()) {
            return true;
        }
        else if(description != null && !description.isEmpty()) {
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
