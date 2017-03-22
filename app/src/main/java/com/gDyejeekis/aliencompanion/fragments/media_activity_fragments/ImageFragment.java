package com.gDyejeekis.aliencompanion.fragments.media_activity_fragments;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.gDyejeekis.aliencompanion.activities.MediaActivity;
import com.gDyejeekis.aliencompanion.asynctask.MediaLoadTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickImage;

/**
 * Created by sound on 3/8/2016.
 */
public class ImageFragment extends Fragment {

    public static final String TAG = "ImageFragment";

    private MediaActivity activity;

    public String getUrl() {
        return url;
    }

    private String url;

    private SubsamplingScaleImageView imageView;

    private Button buttonRetry;

    private MediaLoadTask loadTask;

    private boolean convertedToRgb;

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

        activity = (MediaActivity) getActivity();
        url = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.photoview);
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
                if(convertedToRgb) {
                    imageLoadError();
                    ToastUtils.showToast(activity, "Error decoding image");
                }
                else {
                    ToastUtils.showToast(activity, "Converting image to RGB..");
                    convertAndShowImg();
                }
            }

            @Override
            public void onTileLoadError(Exception e) {
                Log.d(TAG, "onTileLoadError()");
            }
        });

        if(url.startsWith("file:")) {
            imageView.setImage(ImageSource.uri(url.replace("file:", "")));
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
                        ToastUtils.showToast(activity, "Error loading image");
                        CleaningUtils.clearMediaFromCache(activity.getCacheDir(), url); // this shouldn't throw any exceptions
                    }
                }
            };
            //loadTask.executeOnExecutor(THREAD_POOL_EXECUTOR, url);
            loadTask.execute(url);
        }
    }

    private void convertAndShowImg() {
        convertedToRgb = true;

        final String cachedPath;
        if(url.startsWith("file:")) {
            cachedPath = url.replace("file:", "");
        }
        else {
            cachedPath = GeneralUtils.checkCacheForMedia(activity.getCacheDir(), url);
        }

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    ImageInfo imageInfo = new ImageInfo(cachedPath);
                    MagickImage magickImage = new MagickImage(imageInfo);
                    boolean success = magickImage.transformRgbImage(ColorspaceType.RGBColorspace);
                    if(success) {
                        magickImage.writeImage(imageInfo);
                    }
                    return success;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                imageView.setImage(ImageSource.uri(cachedPath));
                if(!success) {
                    ToastUtils.showToast(activity, "Error converting image");
                }
            }
        }.execute();
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
                        activity.saveMedia();
                    }
                    else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                    }
                }
                else {
                    activity.saveMedia();
                }
                return true;
            case R.id.action_share:
                activity.shareMedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 11) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                activity.saveMedia();
            }
            else {
                ToastUtils.showToast(activity, "Failed to save image to photos (permission denied)");
            }
        }
    }

}
