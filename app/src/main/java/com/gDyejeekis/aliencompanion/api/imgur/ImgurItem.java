package com.gDyejeekis.aliencompanion.api.imgur;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sound on 3/9/2016.
 */
public abstract class ImgurItem implements Serializable {

    public abstract String getId();

    public abstract boolean isAlbum();

    public abstract List<ImgurImage> getImages();

    public abstract void setImages(List<ImgurImage> images);
}
