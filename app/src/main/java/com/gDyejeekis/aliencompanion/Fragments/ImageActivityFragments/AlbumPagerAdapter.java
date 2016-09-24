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
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by sound on 3/12/2016.
 */
public class AlbumPagerAdapter extends FragmentStatePagerAdapter {

    //private ImageActivity activity;

    private List<ImgurImage> images;

    private ArrayList<String> thumbUrls = new ArrayList<>();

    private OkHttpClient okHttpClient;

    public AlbumPagerAdapter(Activity activity, FragmentManager fm, List<ImgurImage> images, boolean loadFromLocal) {
        super(fm);
        //this.activity = activity;
        this.images = images;
        this.okHttpClient = new OkHttpClient();

        for(ImgurImage image : images) {
            if(loadFromLocal) {
                File file = GeneralUtils.findFile(activity.getFilesDir(), activity.getFilesDir().getAbsolutePath(), image.getId() + "-thumb");
                if(file!=null) {
                    thumbUrls.add("file:" + file.getAbsolutePath());
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
                return ImageFragment.newInstance(url, okHttpClient);
            } else if (url.matches("(?i).*\\.(gifv|gif)")) {
                return GifFragment.newInstance(url);
            }
        }
        return null;
    }

}
