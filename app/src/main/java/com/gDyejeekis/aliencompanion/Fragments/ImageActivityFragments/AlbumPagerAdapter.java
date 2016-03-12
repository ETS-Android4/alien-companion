package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;

import java.util.List;

/**
 * Created by sound on 3/12/2016.
 */
public class AlbumPagerAdapter extends FragmentPagerAdapter {

    //private ImageActivity activity;

    private List<ImgurImage> images;

    public AlbumPagerAdapter(FragmentManager fm, List<ImgurImage> images) {
        super(fm);
        //this.activity = activity;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Fragment getItem(int position) {
        String url = images.get(position).getLink();
        if(url.matches("(?i).*\\.(png|jpg|jpeg)")) {
            return ImageFragment.newInstance(url);
        }
        else if(url.matches("(?i).*\\.(gifv|gif)")) {
            return GifFragment.newInstance(url);
        }
        return null;
    }

}
