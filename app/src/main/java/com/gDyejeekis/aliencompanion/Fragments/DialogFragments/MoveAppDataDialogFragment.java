package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.StorageUtils;

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
            if(filename.equals(MyApplication.SAVED_ACCOUNTS_FILENAME)
                    || filename.equals(MyApplication.SYNC_PROFILES_FILENAME)
                    || filename.equals(MyApplication.OFFLINE_USER_ACTIONS_FILENAME)
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
        View view = inflater.inflate(R.layout.fragment_move_app_data, container, false);

        TextView textView = (TextView) view.findViewById(R.id.textView_operation);
        String string = (moveToExternal) ? "external" : "internal";
        string = "Moving data to " + string + " memory";
        textView.setText(string);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                //SystemClock.sleep(2000);
                //internal dir
                File internalDir = getActivity().getFilesDir();
                //secondary external dir
                File[] externalDirs = ContextCompat.getExternalFilesDirs(getActivity(), null);
                File activeExternalDir = (externalDirs.length > 1) ? externalDirs[1] : externalDirs[0];
                //Pictures folder inside secondary external directory
                File externalPicsDir = new File(activeExternalDir.getAbsolutePath() + "/Pictures");
                if(!externalPicsDir.exists()) {
                    externalPicsDir.mkdir();
                }

                //app folder inside public pictures directory (primary external directory)
                File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/AlienCompanion");
                if(!picturesDir.exists()) {
                    picturesDir.mkdir();
                }
                if(moveToExternal) {
                    //first move all synced data (excluding pictures)
                    File[] syncedData = internalDir.listFiles(filter);
                    for(File file : syncedData) {
                        StorageUtils.moveFileBetweenDisks(file, activeExternalDir.getAbsolutePath());
                    }

                    //find any pictures in primary external directory and move them to secondary (SD card)
                    for(File file : picturesDir.listFiles()) {
                        if(file.isDirectory()) {
                            File subredditDir = new File(externalPicsDir + "/" + file.getName());
                            if(!subredditDir.exists()) {
                                subredditDir.mkdir();
                            }
                            File[] pics = file.listFiles();
                            for(File pic : pics) {
                                StorageUtils.moveFileBetweenDisks(getActivity(), pic, subredditDir.getAbsolutePath());
                            }
                        }
                    }
                }
                else {
                    File[] syncedData = activeExternalDir.listFiles(filter);
                    for(File file : syncedData) {
                        StorageUtils.moveFileBetweenDisks(file, internalDir.getAbsolutePath());
                    }

                    for(File file : externalPicsDir.listFiles()) {
                        if(file.isDirectory()) {
                            File subredditDir = new File(picturesDir + "/" + file.getName());
                            if(!subredditDir.exists()) {
                                subredditDir.mkdir();
                            }
                            File[] pics = file.listFiles();
                            for(File pic : pics) {
                                StorageUtils.moveFileBetweenDisks(getActivity(), pic, subredditDir.getAbsolutePath());
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismiss();
            }
        }.execute();

        setCancelable(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
