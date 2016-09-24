package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.BitmapTransform;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.PicassoDecoder;
import com.gDyejeekis.aliencompanion.Utils.PicassoRegionDecoder;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import okhttp3.OkHttpClient;

/**
 * Created by sound on 3/8/2016.
 */
public class ImageFragment extends Fragment {

    public static final String TAG = "ImageFragment";

    private ImageActivity activity;

    private String url;

    private SubsamplingScaleImageView imageView;

    private Button buttonRetry;

    private Picasso picasso;

    private OkHttpClient okHttpClient;

    public static ImageFragment newInstance(String url, OkHttpClient okHttpClient) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        fragment.setOkHttpClient(okHttpClient);

        return fragment;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

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
        }
        else {
            picasso = Picasso.with(imageView.getContext());

            imageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
                public ImageDecoder make() {
                    return new PicassoDecoder(url, picasso);
                }
            });

            imageView.setRegionDecoderFactory(new DecoderFactory<ImageRegionDecoder>() {
                @Override
                public ImageRegionDecoder make() throws IllegalAccessException, InstantiationException {
                    return new PicassoRegionDecoder(okHttpClient);
                }
            });
        }

        imageView.setImage(ImageSource.uri(url));
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
        if(picasso!=null) {
            picasso.cancelTag(url);
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
        Picasso.with(activity).load(url).into(saveTarget);
    }

    private final Target saveTarget = new Target(){

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Log.d(TAG, "Saving " + url + " to pictures directory");
                    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

                    File appFolder = new File(dir + "/AlienCompanion");

                    if(!appFolder.exists()) {
                        appFolder.mkdir();
                    }

                    String filename = url.replaceAll("https?://", "").replace("/", "(s)");
                    File file = new File(appFolder.getAbsolutePath(), filename);
                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                        ostream.flush();
                        ostream.close();

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(file);
                        mediaScanIntent.setData(contentUri);
                        activity.sendBroadcast(mediaScanIntent);

                        showImageSavedNotification(bitmap, contentUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            ToastUtils.displayShortToast(activity, "Failed to save image");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            ToastUtils.displayShortToast(activity, "Saving to photos..");
        }
    };

    private void showImageSavedNotification(Bitmap bitmap, Uri uri) {
        Bitmap decoded = new BitmapTransform(640, 480).transform(bitmap);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Image saved")
                //.setContentText(url)
                .setSubText(url)
                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                //.setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(decoded))
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(UUID.randomUUID().hashCode(), notif);
    }

    private void shareImage() {
        String label = "Share image to..";
        String link;
        if(activity.loadedFromLocal()) {
            link = "http://" + url.substring(url.lastIndexOf("/")+1).replace("(s)", "/");
        }
        else {
            link = url;
        }
        GeneralUtils.shareUrl(activity, label, link);
    }

}
