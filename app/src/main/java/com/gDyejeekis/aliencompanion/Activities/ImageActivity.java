package com.gDyejeekis.aliencompanion.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.AsyncTasks.GfycatTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.ImgurTask;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.AlbumPagerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.GifFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageFragment;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import java.util.List;

/**
 * Created by sound on 3/4/2016.
 */
public class ImageActivity extends BackNavActivity {

    private String url;

    private String domain;

    private ProgressBar progressBar;

    private ViewPager viewPager;

    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#00000000"));
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_close_white_24dp);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar5);

        setupFragments();
    }

    private void setupFragments() {
        url = getIntent().getStringExtra("url");
        domain = getIntent().getStringExtra("domain");

        if(url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
            url = url.replace("\\?(\\d+)?", "");
            //Log.d("ImageActivity", "image fragment url " + url);
            addImageFragment(url);
        }
        else if(url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
            url = url.replace("\\?(\\d+)?", "");
            //Log.d("ImageActivity", "gif fragment url " + url);
            url = url.replace(".gifv", ".mp4");
            addGifFragment(url);
        }
        else if(domain.contains("imgur.com")) {
            new ImgurTask(this) {
                @Override protected void onPostExecute(ImgurItem item) {
                    if(item==null) {
                        ToastUtils.displayShortToast(getContext(), "Error retrieving imgur info");
                    }
                    else {
                        if (item instanceof ImgurImage) {
                            ImgurImage image = (ImgurImage) item;
                            if (image.isAnimated()) {
                                addGifFragment(image.getMp4());
                            } else {
                                addImageFragment(image.getLink());
                            }
                        }
                        else if (item instanceof ImgurAlbum) {
                            setupAlbumView(((ImgurAlbum)item).getImages());
                        }
                        else if (item instanceof ImgurGallery) {
                            ImgurGallery gallery = (ImgurGallery) item;
                            if (gallery.isAlbum()) {
                                setupAlbumView(gallery.getImages());
                            } else {
                                addImageFragment(gallery.getLink());
                            }
                        }
                    }
                }
            }.execute(url);
        }
        else if(domain.equals("gfycat.com")) {
            new GfycatTask(this) {
                @Override protected void onPostExecute(String url) {
                    if(url==null) {
                        ToastUtils.displayShortToast(getContext(), "Error retrieving gfycat info");
                    }
                    else {
                        addGifFragment(url);
                    }
                }
            }.execute(url);
        }
    }

    private void setupAlbumView(final List<ImgurImage> images) {
        getSupportActionBar().setTitle("Album");
        getSupportActionBar().setSubtitle("1 of " + images.size());
        viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new AlbumPagerAdapter(fragmentManager, images));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSubtitle((position+1) + " of " + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void addImageFragment(String url) {
        fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, ImageFragment.newInstance(url), "imageFragment").commit();
    }

    public void addGifFragment(String url) {
        fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, GifFragment.newInstance(url), "gifFragment").commit();
    }

    public void setMainProgressBarVisible(boolean flag) {
        if(flag) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    switch (item.getItemId()) {
    //        case R.id.action_save:
    //            return true;
    //        case R.id.action_share:
    //            return true;
    //        default:
    //            return super.onOptionsItemSelected(item);
    //    }
    //}
}
