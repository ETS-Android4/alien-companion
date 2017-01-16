package com.gDyejeekis.aliencompanion.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.AsyncTasks.GyazoTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.ImgurTask;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.AlbumPagerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.GifFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageFragment;
import com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments.ImageInfoFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.Utils.StorageUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

/**
 * Created by sound on 3/4/2016.
 */
public class ImageActivity extends BackNavActivity {

    public static final String TAG = "ImageActivity";

    private String url;

    private boolean loadFromSynced = false;

    public boolean loadedFromLocal() {
        return loadFromSynced;
    }

    private ProgressBar progressBar;

    private ViewPager viewPager;

    private int viewPagerPosition;

    private FragmentManager fragmentManager;

    private int albumSize = -1;

    private boolean showHqAction;

    private boolean showGridviewAction;

    private boolean showSaveAction = true;

    private boolean showInfoAction;

    private boolean infoFragmentVisible;

    private String imageTitle;

    private String imageDescription;

    @Override
    public void onBackPressed() {
        if(infoFragmentVisible) {
            removeInfoFragment();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MyApplication.fontStyle, true);
        getTheme().applyStyle(MyApplication.fontFamily, true);
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
        String domain = getIntent().getStringExtra("domain");

        if(MyApplication.offlineModeEnabled) {
            File appFolder;
            if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(this)) {
                File[] externalDirs = ContextCompat.getExternalFilesDirs(this, null);
                String dir = (externalDirs.length > 1) ? externalDirs[1].getAbsolutePath() : externalDirs[0].getAbsolutePath();
                appFolder = new File(dir + "/Pictures");
            }
            else {
                appFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AlienCompanion");
            }

            String toFind = null;
            if(domain.contains("gfycat.com")) {
                toFind = LinkHandler.getGfycatId(url);
            }
            else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
                toFind = LinkHandler.getReddituploadsFilename(url);
            }
            else if(domain.contains("gyazo.com")) {
                toFind = LinkHandler.getGyazoId(url);
            }
            else if(domain.contains("imgur.com")) {
                String id = LinkHandler.getImgurImgId(url);
                if(url.contains("/a/")) {
                    ImgurAlbum album = (ImgurAlbum) findImgurItemFromFile(id);
                    if(album!=null) {
                        loadFromSynced = true;
                        if(album.getImages().size()==1) {
                            String path = album.getImages().get(0).getLink();
                            if(path.endsWith(".mp4") || path.endsWith(".gif")) {
                                addGifFragment(path);
                            }
                            else {
                                addImageFragment(path);
                            }
                        }
                        else {
                            setupAlbumView(album.getImages(), album.hasInfo(), album.getTitle(), album.getDescription());
                        }
                    }
                }
                else if(url.contains("/gallery/")) {
                    ImgurGallery gallery = (ImgurGallery) findImgurItemFromFile(id);
                    if(gallery!=null) {
                        if(gallery.isAlbum()) {
                            loadFromSynced = true;
                            if(gallery.getImages().size()==1) {
                                String path = gallery.getImages().get(0).getLink();
                                if(path.endsWith(".mp4") || path.endsWith(".gif")) {
                                    addGifFragment(path);
                                }
                                else {
                                    addImageFragment(path);
                                }
                            }
                            else {
                                setupAlbumView(gallery.getImages(), false, null, null);
                            }
                        }
                        else {
                            toFind = id;
                        }
                    }
                }
                else {
                    toFind = id;
                }
            }
            else {
                toFind = url.replaceAll("https?://", "").replace("/", "(s)");
            }

            if(toFind!=null) {
                File file = GeneralUtils.findFile(appFolder, appFolder.getAbsolutePath(), toFind);
                if (file != null) {
                    Log.d(TAG, "Locally saved image found " + file.getAbsolutePath());
                    loadFromSynced = true;
                    String filename = file.getName();
                    if (filename.endsWith(".mp4") || filename.endsWith(".gif")) {
                        addGifFragment(file.getAbsolutePath());
                    } else {
                        addImageFragment("file:" + file.getAbsolutePath());
                    }
                }
            }

        }

        if(!loadFromSynced) {
            Log.d(TAG, "No locally saved image found, loading from network..");
            if (domain.contains("gfycat.com")) {
                addGifFragment(GeneralUtils.getGfycatMobileUrl(url));
            }
            else if(domain.contains("gyazo.com") && !LinkHandler.isRawGyazoUrl(url)) {
                new GyazoTask(this) {
                    @Override
                    protected void onPostExecute(String rawUrl) {
                        if(rawUrl == null) {
                            ToastUtils.displayShortToast(getContext(), "Error retrieve gyazo info");
                        }
                        else {
                            if(rawUrl.endsWith(".jpg") || rawUrl.endsWith(",jpeg") || rawUrl.endsWith("png")) {
                                addImageFragment(rawUrl);
                            }
                            else if(rawUrl.endsWith(".gif") || rawUrl.endsWith(".mp4")) {
                                addGifFragment(rawUrl);
                            }
                        }
                    }
                }.execute(url);
            }
            else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
                addImageFragment(url);
            }
            else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                addImageFragment(url);
            }
            else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                if (domain.contains("imgur.com")) {
                    url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                    //url = url.replace(".gif", ".mp4");
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
                                checkImgurItemInfo(image);
                                if (image.isAnimated()) {
                                    addGifFragment(image.getMp4());
                                } else {
                                    addImageFragment(image.getLink());
                                }
                            } else if (item instanceof ImgurAlbum) {
                                checkImgurAlbumSize(item);
                                //setupAlbumView(item.getImages());
                            } else if (item instanceof ImgurGallery) {
                                ImgurGallery gallery = (ImgurGallery) item;
                                if (gallery.isAlbum()) {
                                    checkImgurAlbumSize(item);
                                    //setupAlbumView(gallery.getImages());
                                } else {
                                    checkImgurItemInfo(gallery);
                                    if(gallery.isAnimated()) {
                                        addGifFragment(gallery.getMp4());
                                    }
                                    else {
                                        addImageFragment(gallery.getLink());
                                    }
                                }
                            }
                        }
                    }
                }.execute(url);
            }
        }
    }

    private void checkImgurItemInfo(ImgurItem item) {
        showInfoAction = item.hasInfo();
        if(showInfoAction) {
            setInfoValues(item.getTitle(), item.getDescription());
        }
    }

    private void checkImgurAlbumSize(ImgurItem album) {
        if(album.getImages().size() == 1) {
            ImgurImage image = album.getImages().get(0);
            checkImgurItemInfo(image);
            if(image.isAnimated()) {
                addGifFragment(image.getMp4());
            }
            else {
                addImageFragment(image.getLink());
            }
        }
        else {
            setupAlbumView(album.getImages(), album.hasInfo(), album.getTitle(), album.getDescription());
        }
    }

    private ImgurItem findImgurItemFromFile(String id) {
        File dir = GeneralUtils.getActiveDir(this);
        File file = GeneralUtils.findFile(dir, dir.getAbsolutePath(), id + "-albumInfo");
        if(file!=null) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                ImgurItem item = (ImgurItem) ois.readObject();
                fis.close();
                ois.close();
                return item;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void setupAlbumView(final List<ImgurImage> images, final boolean hasAlbumInfo, final String title, final String description) {
        albumSize = images.size();
        showGridviewAction = true;
        checkImgurItemInfo(images.get(0));
        invalidateOptionsMenu();
        getSupportActionBar().setTitle("Album");
        getSupportActionBar().setSubtitle("1 of " + images.size());
        viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new AlbumPagerAdapter(this, fragmentManager, images, loadFromSynced));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerPosition = position;
                removeInfoFragment();
                String subtitle;
                if(position==albumSize) {
                    setMainProgressBarVisible(false);
                    showSaveAction = false;
                    showGridviewAction = false;
                    showInfoAction = hasAlbumInfo;
                    if(showInfoAction) {
                        setInfoValues(title, description);
                    }
                    invalidateOptionsMenu();
                    subtitle = albumSize + " items";
                }
                else {
                    showSaveAction = true;
                    showGridviewAction = true;
                    checkImgurItemInfo(images.get(position));
                    invalidateOptionsMenu();
                    subtitle = (position + 1) + " of " + albumSize;
                }
                getSupportActionBar().setSubtitle(subtitle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == SCROLL_STATE_DRAGGING || state == SCROLL_STATE_IDLE) {
                    //Log.d(TAG, "onPageScrollStateChanged");
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, viewPagerPosition);
                    if (fragment instanceof GifFragment) {
                        if (state == SCROLL_STATE_IDLE) {
                            ((GifFragment) fragment).resumePlayback();
                        }
                        else {
                            ((GifFragment) fragment).pausePlayback();
                        }
                    }
                }
            }
        });
    }

    public void setViewPagerPosition(int position) {
        viewPager.setCurrentItem(position);
    }

    public void addImageFragment(String url) {
        fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, ImageFragment.newInstance(url), ImageFragment.TAG).commitAllowingStateLoss();
    }

    public void addGifFragment(String url) {
        fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, GifFragment.newInstance(url, true), GifFragment.TAG).commitAllowingStateLoss();
    }

    //public void addGifFragment(String url, int position) {
    //    fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, GifFragment.newInstance(url, false), GifFragment.TAG + position).commitAllowingStateLoss();
    //}

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

        MenuItem infoAction = menu.findItem(R.id.action_info);
        infoAction.setVisible(showInfoAction);

        MenuItem saveAction = menu.findItem(R.id.action_save);
        saveAction.setVisible(showSaveAction && !loadFromSynced);

        MenuItem hq_action = menu.findItem(R.id.action_high_quality);
        //hq_action.setVisible(showHqAction);
        hq_action.setVisible(false);

        MenuItem gridview_action = menu.findItem(R.id.action_album_gridview);
        gridview_action.setVisible(showGridviewAction);
        //gridview_action.setVisible(albumSize != -1);
        return true;
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
            case R.id.action_info:
                if(infoFragmentVisible) {
                    removeInfoFragment();
                }
                else {
                    addInfoFragment(imageTitle, imageDescription);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addInfoFragment(String title, String description) {
        if(!infoFragmentVisible) {
            infoFragmentVisible = true;
            fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, ImageInfoFragment.newInstance(title, description), ImageInfoFragment.TAG).commitAllowingStateLoss();
        }
    }

    public void removeInfoFragment() {
        if(infoFragmentVisible) {
            infoFragmentVisible = false;
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(ImageInfoFragment.TAG)).commitAllowingStateLoss();
        }
    }

    private void setInfoValues(String title, String description) {
        this.imageTitle = title;
        this.imageDescription = description;
    }

    public boolean isInfoVisible() {
        return infoFragmentVisible;
    }

}
