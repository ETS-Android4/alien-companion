package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 3/12/2016.
 */
public class AlbumPagerAdapter extends FragmentStatePagerAdapter {

    //private ImageActivity activity;

    private List<ImgurImage> images;

    private ArrayList<String> thumbUrls = new ArrayList<>();

    public AlbumPagerAdapter(FragmentManager fm, List<ImgurImage> images) {
        super(fm);
        //this.activity = activity;
        this.images = images;

        for(ImgurImage image : images) {
            thumbUrls.add("http://i.imgur.com/" + LinkHandler.getImgurImgId(image.getLink()) + "s.jpg");
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
            } else if (url.matches("(?i).*\\.(gifv|gif)")) {
                return GifFragment.newInstance(url);
            }
        }
        return null;
    }

}
