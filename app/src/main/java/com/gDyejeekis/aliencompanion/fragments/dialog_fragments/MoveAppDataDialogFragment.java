package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by George on 7/17/2016.
 */
public class MoveAppDataDialogFragment extends DialogFragment {

    private boolean moveToExternal;

    private static final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            if(filename.equals(AppConstants.SAVED_ACCOUNTS_FILENAME)
                    || filename.equals(AppConstants.SYNC_PROFILES_FILENAME)
                    || filename.equals(AppConstants.OFFLINE_USER_ACTIONS_FILENAME)
                    || filename.equals("Pictures")) {
                return false;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveToExternal = getArguments().getBoolean("external");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_please_wait, container, false);

        ProgressBar progressBar = view.findViewById(R.id.progressBar_operation);
        progressBar.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        TextView textView = (TextView) view.findViewById(R.id.textView_operation);
        String string = (moveToExternal) ? "external" : "internal";
        string = "Moving data to " + string + " storage";
        textView.setText(string);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                //internal dir
                File internalDir = getActivity().getFilesDir();
                //secondary external dir
                File[] externalDirs = ContextCompat.getExternalFilesDirs(getActivity(), null);
                File externalDir = (externalDirs.length > 1) ? externalDirs[1] : externalDirs[0];

                File srcDir;
                String targetPath;
                File redditDataDir;
                File mediaDir;
                File articlesDir;
                File thumbsDir;
                if(moveToExternal) {
                    srcDir = internalDir;
                    targetPath = externalDir.getAbsolutePath();
                }
                else {
                    srcDir = externalDir;
                    targetPath = internalDir.getAbsolutePath();
                }
                redditDataDir = new File(srcDir, AppConstants.SYNCED_REDDIT_DATA_DIR_NAME);
                mediaDir = new File(srcDir, AppConstants.SYNCED_MEDIA_DIR_NAME);
                articlesDir = new File(srcDir, AppConstants.SYNCED_ARTICLES_DIR_NAME);
                thumbsDir = new File(srcDir, AppConstants.SYNCED_THUMBNAILS_DIR_NAME);

                if(redditDataDir.exists()) {
                    StorageUtils.moveFileBetweenDisksRecursive(redditDataDir, targetPath);
                }
                if(mediaDir.exists()){
                    StorageUtils.moveFileBetweenDisksRecursive(mediaDir, targetPath);
                }
                if(articlesDir.exists()) {
                    StorageUtils.moveFileBetweenDisksRecursive(articlesDir, targetPath);
                }
                if(thumbsDir.exists()) {
                    StorageUtils.moveFileBetweenDisksRecursive(thumbsDir, targetPath);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismissAllowingStateLoss();
            }
        }.execute();

        setCancelable(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

}
