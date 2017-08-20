package com.gDyejeekis.aliencompanion.fragments.media_activity_fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 3/12/2016.
 */
public class AlbumPagerAdapter extends FragmentStatePagerAdapter {

    private List<ImgurImage> images;

    private ArrayList<String> thumbUrls = new ArrayList<>();

    public AlbumPagerAdapter(Activity activity, FragmentManager fm, List<ImgurImage> images, boolean loadFromLocal) {
        super(fm);
        this.images = images;

        for(ImgurImage image : images) {
            if(loadFromLocal) {
                File mediaDir = GeneralUtils.getSyncedMediaDir(activity);
                File imgFile = StorageUtils.findFile(mediaDir, mediaDir.getAbsolutePath(), LinkHandler.getImgurImgId(image.getLink()));
                if(imgFile!=null) {
                    image.setLink("file:" + imgFile.getAbsolutePath());
                }

                File thumbsDir = GeneralUtils.getSyncedThumbnailsDir(activity);
                File thumbFile = StorageUtils.findFile(thumbsDir, thumbsDir.getAbsolutePath(), image.getId() + "-thumb");
                if(thumbFile!=null) {
                    thumbUrls.add("file:" + thumbFile.getAbsolutePath());
                }
                else {
                    thumbUrls.add("null");
                }
            }
            else {
                thumbUrls.add("http://i.imgur.com/" + LinkHandler.getImgurImgId(image.getLink()) + "s.jpg");
            }
        }
    }

    @Override
    public int getCount() {
        return images.size()+1;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == images.size()) {
            return AlbumGridviewFragment.newInstance(thumbUrls);
        }
        else {
            String url = images.get(position).getLink();
            if (url.matches("(?i).*\\.(png|jpg|jpeg)")) {
                return ImageFragment.newInstance(url);
            } else if (url.matches("(?i).*\\.(mp4|gifv|gif)")) {
                return GifFragment.newInstance(url, (position == 0));
            }
        }
        return null;
    }

}
