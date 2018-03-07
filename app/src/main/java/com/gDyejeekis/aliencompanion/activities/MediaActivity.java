package com.gDyejeekis.aliencompanion.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.asynctask.GfycatTask;
import com.gDyejeekis.aliencompanion.asynctask.GiphyTask;
import com.gDyejeekis.aliencompanion.asynctask.GyazoTask;
import com.gDyejeekis.aliencompanion.asynctask.ImgurTask;
import com.gDyejeekis.aliencompanion.asynctask.MediaDownloadTask;
import com.gDyejeekis.aliencompanion.asynctask.StreamableTask;
import com.gDyejeekis.aliencompanion.fragments.media_activity_fragments.AlbumPagerAdapter;
import com.gDyejeekis.aliencompanion.fragments.media_activity_fragments.GifFragment;
import com.gDyejeekis.aliencompanion.fragments.media_activity_fragments.ImageFragment;
import com.gDyejeekis.aliencompanion.fragments.media_activity_fragments.ImageInfoFragment;
import com.gDyejeekis.aliencompanion.fragments.media_activity_fragments.VideoFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.models.RedditVideo;
import com.gDyejeekis.aliencompanion.utils.BitmapTransform;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
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
public class MediaActivity extends BackNavActivity {

    public static final String TAG = "MediaActivity";

    private String url;

    private boolean loadFromSynced = false;

    public boolean loadedFromLocal() {
        return loadFromSynced;
    }

    private ProgressBar progressBar;

    private ViewPager viewPager;

    private AlbumPagerAdapter albumPagerAdapter;

    private int viewPagerPosition;

    private FragmentManager fragmentManager;

    private int albumSize = -1;

    private boolean showGridviewAction;

    private boolean showSaveAction = true;

    private boolean showInfoAction;

    private boolean infoFragmentVisible;

    private String imageTitle;

    private String imageDescription;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(-1, -1);
    }

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
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar5);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        setupFragments();
    }

    private void setupFragments() {
        String domain;
        //boolean isGif = false;
        RedditVideo redditVideo = (RedditVideo) getIntent().getSerializableExtra("redditVideo");
        if(redditVideo != null) {
            domain = "v.redd.it";
            //isGif = redditVideo.getGif();
            url = redditVideo.getFallbackUrl(); // TODO: 10/11/2017 properly support v.redd.it videos with sound
        }
        else {
            domain = getIntent().getStringExtra("domain");
            url = getIntent().getStringExtra("url");
        }

        if(MyApplication.offlineModeEnabled) {
            File mediaDir = GeneralUtils.checkSyncedMediaDir(this);

            String toFind = null;
            boolean hasSound = false;
            if(domain.contains("gfycat.com")) {
                toFind = LinkUtils.getGfycatId(url);
            }
            else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
                toFind = LinkUtils.urlToFilename(url); // TODO: 7/30/2017 maybe make getReddituploadsId method
            }
            else if(domain.equals("v.redd.it")) {
                toFind = LinkUtils.urlToFilename(url); // TODO: 10/11/2017 maybe use video id
            }
            else if(domain.contains("gyazo.com")) {
                toFind = LinkUtils.getGyazoId(url);
            }
            else if(domain.contains("giphy.com")) {
                toFind = LinkUtils.getGiphyId(url);
            }
            else if(domain.contains("streamable.com")) {
                toFind = LinkUtils.getStreamableId(url);
                hasSound = true;
            }
            else if(domain.contains("imgur.com")) {
                String id = LinkUtils.getImgurImgId(url);
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
                toFind = LinkUtils.urlToFilename(url);
                hasSound = url.endsWith(".mp4");
            }

            if(toFind!=null) {
                File file = StorageUtils.findFile(mediaDir, mediaDir.getAbsolutePath(), toFind);
                if (file != null) {
                    Log.d(TAG, "Locally saved image found " + file.getAbsolutePath());
                    loadFromSynced = true;
                    String filename = file.getName();
                    if(filename.endsWith(".webm")) {
                        addVideoFragment(file.getAbsolutePath());
                    }
                    else if (filename.endsWith(".mp4") || filename.endsWith(".gif")) {
                        if(hasSound) {
                            addVideoFragment(file.getAbsolutePath());
                        }
                        else {
                            addGifFragment(file.getAbsolutePath());
                        }
                    } else {
                        addImageFragment("file:" + file.getAbsolutePath());
                    }
                }
            }

        }

        if(!loadFromSynced) {
            Log.d(TAG, "No locally saved image found, loading from network..");
            // GFYCAT
            if (domain.contains("gfycat.com")) {
                //addGifFragment(GfycatTask.getGfycatDirectUrlSimple(url));
                new GfycatTask(this) {
                    @Override
                    protected void onPostExecute(String rawUrl) {
                        if (rawUrl==null) {
                            ToastUtils.showToast(getContext(), "Error retrieving gfycat info");
                        } else {
                            addGifFragment(rawUrl);
                        }
                    }
                }.execute(url);
            }
            // GYAZO
            else if(domain.contains("gyazo.com") && !LinkUtils.isRawGyazoUrl(url)) {
                new GyazoTask(this) {
                    @Override
                    protected void onPostExecute(String rawUrl) {
                        if(rawUrl == null) {
                            ToastUtils.showToast(getContext(), "Error retrieving gyazo info");
                        }
                        else {
                            if(rawUrl.endsWith(".jpg") || rawUrl.endsWith(".jpeg") || rawUrl.endsWith(".png")) {
                                addImageFragment(rawUrl);
                            }
                            else if(rawUrl.endsWith(".gif") || rawUrl.endsWith(".mp4")) {
                                addGifFragment(rawUrl);
                            }
                        }
                    }
                }.execute(url);
            }
            // GIPHY
            else if(domain.contains("giphy.com") && !LinkUtils.isMp4Giphy(url)) {
                addGifFragment(GiphyTask.getGiphyDirectUrlSimple(url));
                //new GiphyTask(this) {
                //    @Override
                //    protected void onPostExecute(String mp4Url) {
                //        if(mp4Url == null) {
                //            ToastUtils.showToast(getContext(), "Error retrieving giphy info");
                //        }
                //        else {
                //            addGifFragment(mp4Url);
                //        }
                //    }
                //}.execute(url);
            }
            // STREAMABLE
            else if(domain.contains("streamable.com")) {
                new StreamableTask(this) {
                    @Override
                    protected void onPostExecute(String url) {
                        if(url == null) {
                            ToastUtils.showToast(getContext(), "Error retrieving streamable info");
                        }
                        else {
                            addVideoFragment(url);
                        }
                    }
                }.execute(url);
            }
            // REDDIT (SMH FAM)
            else if(domain.equals("i.reddituploads.com") || domain.equals("i.redditmedia.com")) {
                addImageFragment(url);
            }
            // REDDIT VIDEO (>.<)
            else if(domain.equals("v.redd.it")) {
                // TODO: 10/11/2017 add approriate fragment (sound or no sound)
                //if(isGif) addGifFragment(url);
                //else addVideoFragment(url);
                addGifFragment(url);
            }
            // IMAGES
            else if (url.matches("(?i).*\\.(png|jpg|jpeg)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                addImageFragment(url);
            }
            // GIFs
            else if (url.matches("(?i).*\\.(gifv|gif)\\??(\\d+)?")) {
                url = url.replaceAll("\\?(\\d+)?", "");
                if (domain.contains("imgur.com")) {
                    url = url.replace(".gifv", ".mp4").replace(".gif", ".mp4");
                    //url = url.replace(".gif", ".mp4");
                }
                addGifFragment(url);
            }
            // IMGUR
            else if (domain.contains("imgur.com")) {
                new ImgurTask(this) {
                    @Override
                    protected void onPostExecute(ImgurItem item) {
                        if (item == null) {
                            ToastUtils.showToast(getContext(), "Error retrieving imgur info");
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
            // VIDEOS
            else if(url.endsWith(".mp4")) {
                addVideoFragment(url);
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
        File dir = GeneralUtils.checkSyncedMediaDir(this);
        if(dir!=null) {
            File file = StorageUtils.findFile(dir, dir.getAbsolutePath(), id + AppConstants.IMGUR_INFO_FILE_NAME);
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
        if(albumSize == 0) {
            ToastUtils.showToast(this, "No items in album");
            finish();
            return;
        }
        showGridviewAction = true;
        checkImgurItemInfo(images.get(0));
        invalidateOptionsMenu();
        getSupportActionBar().setTitle("Album");
        getSupportActionBar().setSubtitle("1 of " + images.size());
        viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setOffscreenPageLimit(1);
        albumPagerAdapter = new AlbumPagerAdapter(this, fragmentManager, images, loadFromSynced);
        viewPager.setAdapter(albumPagerAdapter);
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

    public void addVideoFragment(String url) {
        fragmentManager.beginTransaction().add(R.id.layout_fragment_holder, VideoFragment.newInstance(url), VideoFragment.TAG).commitAllowingStateLoss();
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

        MenuItem infoAction = menu.findItem(R.id.action_info);
        infoAction.setVisible(showInfoAction);

        MenuItem saveAction = menu.findItem(R.id.action_save);
        saveAction.setVisible(showSaveAction/* && !loadFromSynced*/);

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

    public Fragment getCurrentFragment() {
        if(albumPagerAdapter != null) {
            return albumPagerAdapter.getItem(viewPagerPosition);
        }
        return fragmentManager.findFragmentById(R.id.layout_fragment_holder);
    }

    public boolean isInfoVisible() {
        return infoFragmentVisible;
    }

    public void shareMedia() {
        String label = "Share via..";
        GeneralUtils.shareUrl(this, label, url);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 119871) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                saveMedia();
            }
            else {
                ToastUtils.showToast(this, "Failed to save media (permission denied)");
            }
        }
    }

    public void saveMedia() {
        // checek for permission on android M+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 119871);
                return;
            }
        }
        ToastUtils.showToast(this, "Saving to pictures..");
        Log.d(TAG, "Saving " + url + " to public pictures directory");

        final File saveTarget = getSaveDestination();
        final int saveId = url.hashCode();

        showSavingMediaNotif(saveId);

        if (loadFromSynced) {
            Fragment fragment = getCurrentFragment();
            // TODO: 3/14/2017 add abstraction
            final File file;
            if(fragment instanceof ImageFragment) {
                file = new File(((ImageFragment) fragment).getUrl().replace("file:", ""));
            }
            else if(fragment instanceof GifFragment) {
                file = new File(((GifFragment) fragment).getUrl().replace("file:", ""));
            }
            else {
                file = null;
            }
            if(file != null) {
                new AsyncTask<File, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(File... params) {
                        return StorageUtils.safeCopy(file, saveTarget);
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        onPostMediaSave(saveId, success, saveTarget);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
            }
        } else {
            new MediaDownloadTask(url, saveTarget, getCacheDir()) {
                @Override
                protected void onPostExecute(Boolean success) {
                    onPostMediaSave(saveId, success, saveTarget);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void onPostMediaSave(int saveId, boolean success, File saveTarget) {
        Log.d(TAG, url + " save operation " + (success ? "successful" : "failed"));
        if (success) {
            GeneralUtils.addFileToMediaStore(this, saveTarget);
        }
        showSavedMediaNotif(saveId, success, saveTarget);
    }

    private void showSavingMediaNotif(int id) {
        Notification notif = new Notification.Builder(this)
                .setContentTitle("Saving media..")
                .setContentText(url)
                .setSmallIcon(R.drawable.ic_photo_white_24dp)
                .setProgress(1, 0, true)
                .build();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void showSavedMediaNotif(int id, boolean success, File file) {
        PendingIntent pIntent = null;
        Bitmap resizedBitmap = null;
        boolean isImage = false;
        if(success) {
            try {
                resizedBitmap = new BitmapTransform(640, 480).transform(GeneralUtils.getBitmapFromPath(file.getAbsolutePath()));
                isImage = true;
            } catch (Exception e) {
                isImage = false;
            }
            String type = (isImage) ? "image/*" : "video/*";
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file), type);
            pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }

        String title = (success) ? "Media saved" : "Failed to save media";
        int smallIcon = (success) ? R.drawable.ic_photo_white_24dp : android.R.drawable.stat_notify_error;
        Notification.Builder notifBuilder = new Notification.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(smallIcon)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        if(success) {
            if (isImage) {
                notifBuilder.setSubText(file.getPath());
                if(resizedBitmap != null) {
                    notifBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(resizedBitmap));
                }
            }
            else {
                notifBuilder.setContentText(file.getPath());
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notifBuilder.build());
    }

    private File getSaveDestination() {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        File appFolder = new File(dir + "/" + AppConstants.SAVED_PICTURES_PUBLIC_DIR_NAME);

        if(!appFolder.exists()) {
            appFolder.mkdir();
        }

        String filename = LinkUtils.urlToFilename(url);
        if(!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
            filename = filename.concat(".jpg");
        }
        return new File(appFolder.getAbsolutePath(), filename);
    }

}
