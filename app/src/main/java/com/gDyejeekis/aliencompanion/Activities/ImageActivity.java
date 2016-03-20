package com.gDyejeekis.aliencompanion.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.AsyncTasks.GfycatTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.ImgurTask;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.AlbumPagerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.GifFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import java.io.File;
import java.util.List;

/**
 * Created by sound on 3/4/2016.
 */
public class ImageActivity extends BackNavActivity {

    private String url;

    private String domain;

    private boolean loadFromLocal = false;

    private ProgressBar progressBar;

    private ViewPager viewPager;

    private FragmentManager fragmentManager;

    private int albumSize = -1;

    private boolean showHqAction;

    private boolean showGridviewAction;

    private boolean showSaveAction = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#78000000"));
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

        if(MyApplication.offlineModeEnabled) {
            final File appFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AlienCompanion");

            String toFind;
            if(domain.equals("gfycat.com")) {
                toFind = LinkHandler.getGfycatId(url);
            }
            else if(domain.contains("imgur.com")) {
                toFind = LinkHandler.getImgurImgId(url);
            }
            else {
                toFind = url.replace("/", "(s)").replaceAll("https?:", "");
            }
            File file = GeneralUtils.findFile(appFolder, appFolder.getAbsolutePath(), toFind);
            if(file!=null) {
                loadFromLocal = true;
                String filename = file.getName();
                if(filename.endsWith(".mp4") || filename.endsWith(".gif")) {
                    addGifFragment(file.getAbsolutePath());
                }
                else {
                    addImageFragment("file:" + file.getAbsolutePath());
                }
            }
        }
        if(!loadFromLocal) {
            if (domain.contains("gfycat.com")) {
                addGifFragment(GeneralUtils.getGfycatMobileUrl(url));

            }
            else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                addImageFragment(url);
            }
            else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                if (domain.contains("imgur.com")) {
                    url = url.replace(".gifv", ".mp4");
                    url = url.replace(".gif", ".mp4");
                }
                addGifFragment(url);
            }
            else if (domain.contains("imgur.com")) {
                new ImgurTask(this) {
                    @Override
                    protected void onPostExecute(ImgurItem item) {
                        if (item == null) {
                            ToastUtils.displayShortToast(getContext(), "Error retrieving imgur info");
                        } else {
                            if (item instanceof ImgurImage) {
                                ImgurImage image = (ImgurImage) item;
                                if (image.isAnimated()) {
                                    addGifFragment(image.getMp4());
                                } else {
                                    addImageFragment(image.getLink());
                                }
                            } else if (item instanceof ImgurAlbum) {
                                setupAlbumView(((ImgurAlbum) item).getImages());
                            } else if (item instanceof ImgurGallery) {
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
        }
    }

    private void setupAlbumView(final List<ImgurImage> images) {
        albumSize = images.size();
        showGridviewAction = true;
        setHqMenuItemVisible(!images.get(0).isAnimated());
        getSupportActionBar().setTitle("Album");
        getSupportActionBar().setSubtitle("1 of " + images.size());
        viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new AlbumPagerAdapter(fragmentManager, images));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String subtitle = "";
                if(position==albumSize) {
                    setMainProgressBarVisible(false);
                    showSaveAction = false;
                    showGridviewAction = false;
                    setHqMenuItemVisible(false);
                    subtitle = albumSize + " items";
                }
                else {
                    showSaveAction = true;
                    showGridviewAction = true;
                    if(images.get(position).isAnimated()) {
                        setHqMenuItemVisible(false);
                    }
                    else {
                        setHqMenuItemVisible(true);
                    }
                    subtitle = (position + 1) + " of " + albumSize;
                }
                getSupportActionBar().setSubtitle(subtitle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setViewPagerPosition(int position) {
        viewPager.setCurrentItem(position);
    }

    public void addImageFragment(String url) {
        setHqMenuItemVisible(true);
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
        MenuItem saveAction = menu.findItem(R.id.action_save);
        MenuItem hq_action = menu.findItem(R.id.action_high_quality);
        MenuItem gridview_action = menu.findItem(R.id.action_album_gridview);
        saveAction.setVisible(showSaveAction && !loadFromLocal);
        hq_action.setVisible(showHqAction);
        gridview_action.setVisible(showGridviewAction);
        //gridview_action.setVisible(albumSize != -1);
        return true;
    }

    public void setHqMenuItemVisible(boolean flag) {
        showHqAction = flag;
        invalidateOptionsMenu();
    }

    public String getOriginalUrl() {
        return url;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_album_gridview:
                setViewPagerPosition(albumSize);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
