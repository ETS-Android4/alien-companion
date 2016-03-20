package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.BitmapTransform;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.Views.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by sound on 3/8/2016.
 */
public class ImageFragment extends Fragment {

    public static final String TAG = "ImageFragment";

    private static final int MAX_WIDTH = 1600;
    private static final int MAX_HEIGHT = 900;

    private static final int HQ_MAX_WIDTH = 1920;
    private static final int HQ_MAX_HEIGHT = 1200;

    private static final int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

    private static final int hqSize = (int) Math.ceil(Math.sqrt(HQ_MAX_WIDTH * HQ_MAX_HEIGHT));

    private ImageActivity activity;

    private String url;

    private TouchImageView imageView;

    private Button buttonRetry;

    private boolean attemptSecondSave = true;

    public static ImageFragment newInstance(String url) {
        ImageFragment fragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);

        return fragment;
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

        imageView = (TouchImageView) view.findViewById(R.id.photoview);
        //imageView.setTag(target); //this keeps reference to imageview, causes OOM issues
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
        Log.d(TAG, "Loading image from " + url);
        imageView.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);
        activity.setMainProgressBarVisible(true);

        Picasso.with(activity).load(url).transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT)).skipMemoryCache().resize(size, size).centerInside().into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                activity.setMainProgressBarVisible(false);
                imageView.setVisibility(View.VISIBLE);
                buttonRetry.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                activity.setMainProgressBarVisible(false);
                imageView.setVisibility(View.GONE);
                buttonRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Picasso.with(activity).cancelRequest(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_high_quality:
                //loadOriginalBitmap();
                loadBitmapResized(2560, 1440);
                return true;
            case R.id.action_save:
                saveImageToPhotos();
                return true;
            case R.id.action_share:
                shareImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadOriginalBitmap() {
        imageView.setVisibility(View.GONE);
        activity.setMainProgressBarVisible(true);
        Picasso.with(activity).load(url).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Loaded original size bitmap");
                imageView.setVisibility(View.VISIBLE);
                activity.setMainProgressBarVisible(false);
            }

            @Override
            public void onError() {
                loadBitmapResized(3840, 2160);
            }
        });
    }

    private void loadBitmapResized(final int width, final int height) {
        int size = (int) Math.ceil(Math.sqrt(width * height));
        Picasso.with(activity).load(url).transform(new BitmapTransform(width, height)).skipMemoryCache().resize(size, size).centerInside().into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Loaded bitmap with dimensions " + width + "x" + height);
                imageView.setVisibility(View.VISIBLE);
                activity.setMainProgressBarVisible(false);
            }

            @Override
            public void onError() {
                if(width <= 0 || height <= 0) {
                    Log.d(TAG, "Failed to load bitmap");
                    return;
                }
                loadBitmapResized(width - 200, height - 200);
            }
        });
    }

    private void saveImageToPhotos() {
        Picasso.with(activity).load(url).into(target);
    }

    private final Target target = new Target(){

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

                    String filename = url.replace("/", "(s)").replaceAll("https?:", "");
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

                        showImageSavedNotification(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if(attemptSecondSave) {
                Log.d(TAG, "onBitmapFailed, resizing image..");
                attemptSecondSave = false;
                Picasso.with(activity).load(url).transform(new BitmapTransform(HQ_MAX_WIDTH, HQ_MAX_HEIGHT)).resize(hqSize, hqSize).into(target);
            }
            else {
                ToastUtils.displayShortToast(activity, "Failed to save image");
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if(attemptSecondSave) {
                ToastUtils.displayShortToast(activity, "Saving to photos..");
            }
        }
    };

    private void showImageSavedNotification(Bitmap bitmap) {
        Bitmap decoded = new BitmapTransform(640, 480).transform(bitmap);

        Notification notif = new Notification.Builder(activity)
                .setContentTitle("Image saved")
                //.setContentText(url)
                .setSubText(url)
                .setSmallIcon(R.mipmap.ic_photo_white_24dp)
                //.setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(decoded))
                .build();

        NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(UUID.randomUUID().hashCode(), notif);
    }

    private void shareImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        activity.startActivity(Intent.createChooser(intent, "Share image to.."));
    }

}
