package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.AsyncTasks.MediaDownloadTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.MediaLoadTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.BitmapTransform;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.File;
import java.util.UUID;

import okhttp3.OkHttpClient;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by sound on 3/8/2016.
 */
public class ImageFragment extends Fragment {

    public static final String TAG = "ImageFragment";

    private ImageActivity activity;

    private String url;

    private SubsamplingScaleImageView imageView;

    private Button buttonRetry;

    private MediaLoadTask loadTask;

    //private Picasso picasso;

    //private OkHttpClient okHttpClient;

    public static ImageFragment newInstance(String url) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        return fragment;
    }

    //public void setOkHttpClient(OkHttpClient okHttpClient) {
    //    this.okHttpClient = okHttpClient;
    //}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = (ImageActivity) getActivity();
        url = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.photoview);
        //imageView.setTag(saveTarget); //this keeps reference to imageview, causes OOM issues
        if(MyApplication.dismissImageOnTap) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finish();
                }
            });
        }
        buttonRetry = (Button) view.findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImage();
            }
        });

        loadImage();

        return view;
    }

    private void loadImage() {
        imageLoading();

        imageView.setMinimumTileDpi(160);
        imageView.setMinimumDpi(40);

        imageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
                Log.d(TAG, "onReady()");
            }

            @Override
            public void onImageLoaded() {
                Log.d(TAG, "onImageLoaded()");
                imageLoaded();
            }

            @Override
            public void onPreviewLoadError(Exception e) {
                Log.d(TAG, "onPreviewLoadError()");
            }

            @Override
            public void onImageLoadError(Exception e) {
                Log.d(TAG, "onImageLoadError()");
                imageLoadError();
            }

            @Override
            public void onTileLoadError(Exception e) {
                Log.d(TAG, "onTileLoadError()");
            }
        });

        if(url.startsWith("file:")) {
            url = url.replace("file:", "");
            imageView.setImage(ImageSource.uri(url));
        }
        else {
            loadTask = new MediaLoadTask(activity.getCacheDir()) {

                @Override
                protected void onPostExecute(String cachedPath) {
                    if(cachedPath!=null) {
                        imageView.setImage(ImageSource.uri(cachedPath));
                    }
                    else {
                        imageLoadError();
                        ToastUtils.displayShortToast(activity, "Error loading image");
                        GeneralUtils.clearMediaFromCache(activity.getCacheDir(), url); // this shouldn't throw any exceptions
                    }
                }
            };
            //loadTask.executeOnExecutor(THREAD_POOL_EXECUTOR, url);
            loadTask.execute(url);
            //picasso = Picasso.with(imageView.getContext());
//
            //imageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
            //    public ImageDecoder make() {
            //        return new PicassoDecoder(url, picasso);
            //    }
            //});
//
            //imageView.setRegionDecoderFactory(new DecoderFactory<ImageRegionDecoder>() {
            //    @Override
            //    public ImageRegionDecoder make() throws IllegalAccessException, InstantiationException {
            //        return new PicassoRegionDecoder(okHttpClient);
            //    }
            //});
        }
    }

    // call at the start of every image load
    private void imageLoading() {
        activity.setMainProgressBarVisible(true);
        imageView.setVisibility(View.VISIBLE); // can't hide the view until it is loaded because Android will not call its onDraw method
        buttonRetry.setVisibility(View.GONE);
    }

    // call on succesful image load
    private void imageLoaded() {
        activity.setMainProgressBarVisible(false);
        imageView.setVisibility(View.VISIBLE);
        buttonRetry.setVisibility(View.GONE);
    }

    // call on image load error
    private void imageLoadError() {
        activity.setMainProgressBarVisible(false);
        imageView.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        //if(picasso!=null) {
        //    picasso.cancelTag(url);
        //}
        //Log.d(TAG, "imageFragment onDestroy");
        if(loadTask!=null) {
            loadTask.cancelOperation();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_high_quality:
                //blank
                return true;
            case R.id.action_save:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        saveImageToPhotos();
                    }
                    else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                    }
                }
                else {
                    saveImageToPhotos();
                }
                return true;
            case R.id.action_share:
                shareImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 11) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                saveImageToPhotos();
            }
            else {
                ToastUtils.displayShortToast(activity, "Failed to save image to photos (permission denied)");
            }
        }
    }

    private void saveImageToPhotos() {
        final int notifId = UUID.randomUUID().hashCode();
        showSavingImgNotif(notifId);
        final File saveTarget = getSaveDestination();
        new MediaDownloadTask(url, saveTarget, activity.getCacheDir()) {

            @Override
            protected void onPostExecute(Boolean success) {
                if(success) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(saveTarget);
                    mediaScanIntent.setData(contentUri);
                    activity.sendBroadcast(mediaScanIntent);

                    showImageSavedNotif(notifId, saveTarget);
                }
                else {
                    showImageSaveFailedNotif(notifId);
                }
            }
        }.execute();
    }

    private File getSaveDestination() {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        File appFolder = new File(dir + "/AlienCompanion");

        if(!appFolder.exists()) {
            appFolder.mkdir();
        }

        String filename = GeneralUtils.urlToFilename(url);
        if(!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
            filename = filename.concat(".jpg");
        }
        return new File(appFolder.getAbsolutePath(), filename);
    }

    private void showSavingImgNotif(int id) {
        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Saving image..")
                .setContentText(url)
                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                .setProgress(1, 0, true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void showImageSavedNotif(int id, File file) {
        Bitmap resizedBitmap = new BitmapTransform(640, 480).transform(GeneralUtils.getBitmapFromPath(file.getAbsolutePath()));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Image saved")
                //.setContentText(url)
                .setSubText(url)
                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                //.setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(resizedBitmap))
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void showImageSaveFailedNotif(int id) {
        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Failed to save image")
                .setContentText(url)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notif);
    }

    private void shareImage() {
        String label = "Share image to..";
        GeneralUtils.shareUrl(activity, label, activity.getOriginalUrl());
    }

}
