package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by George on 7/19/2016.
 */
public class StorageUtils {

    private static final String TAG = "StorageUtils";

    public static class StorageInfo {

        public final String path;
        public final boolean readonly;
        public final boolean removable;
        public final int number;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;
            this.number = number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append("Internal SD card");
            } else if (number > 1) {
                res.append("SD card " + number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }

    public static List<StorageInfo> getStorageList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_removable = Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<String>();
        int cur_removable_number = 1;

        if (def_path_available) {
            paths.add(def_path);
            list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
        }

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        return list;
    }



    public static void listFilesInDir(File dir) {
        File[] files = dir.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                listFilesInDir(file);
            }
            else {
                Log.d(TAG, file.getName() + " " + file.length());
            }
        }
    }

    public static boolean isExternalStorageAvailable(Context context) {

        File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
        File externalDir = (externalDirs.length > 1) ? externalDirs[1] : externalDirs[0];
        return externalDir != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        //String state = Environment.getExternalStorageState();
        //boolean mExternalStorageAvailable = false;
        //boolean mExternalStorageWriteable = false;
//
        //if (Environment.MEDIA_MOUNTED.equals(state)) {
        //    // We can read and write the media
        //    mExternalStorageAvailable = mExternalStorageWriteable = true;
        //} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        //    // We can only read the media
        //    mExternalStorageAvailable = true;
        //    mExternalStorageWriteable = false;
        //} else {
        //    // Something else is wrong. It may be one of many other states, but
        //    // all we need
        //    // to know is we can neither read nor write
        //    mExternalStorageAvailable = mExternalStorageWriteable = false;
        //}
//
        //if (mExternalStorageAvailable == true
        //        && mExternalStorageWriteable == true) {
        //    return true;
        //} else {
        //    return false;
        //}
    }

    public static List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            }
            else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }

    public static boolean copyFileToTarget(File fileSource, File fileTarget) {
        try {
            FileInputStream inputStream = new FileInputStream(fileSource);
            FileOutputStream outputStream = new FileOutputStream(fileTarget);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean moveFileInsideDisk(File file, String targetDir) {
        try {
            String oldPath = file.getAbsolutePath();
            if(!targetDir.endsWith("/")) {
                targetDir = targetDir.concat("/");
            }
            if(file.renameTo(new File(targetDir + file.getName()))) {
                Log.d(TAG, oldPath + " moved to " + file.getAbsolutePath());
                return true;
            }
            else {
                Log.e(TAG, "Move operation failed for " + oldPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean moveFileBetweenDisks(File file, String targetDir) {
        try{
            File afile = file;
            File bfile = new File(targetDir, afile.getName());

            FileInputStream inStream = new FileInputStream(afile);
            FileOutputStream outStream = new FileOutputStream(bfile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            //delete the original file
            afile.delete();

            Log.d(TAG, "Successfully copied " + afile.getAbsolutePath() + " to " + bfile.getAbsolutePath());
            return true;

        } catch(IOException e){
            e.printStackTrace();
        }
        Log.d(TAG, "Failed to copy file from " + file.getAbsolutePath() + " to " + targetDir);
        return false;
    }

    public static boolean moveFileBetweenDisks(Context context, File file, String targetDir) {
        try{
            File afile = file;
            File bfile = new File(targetDir, afile.getName());

            FileInputStream inStream = new FileInputStream(afile);
            FileOutputStream outStream = new FileOutputStream(bfile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            //delete the original file
            afile.delete();

            GeneralUtils.deleteFileFromMediaStore(context.getContentResolver(), afile);
            GeneralUtils.addFileToMediaStore(context, bfile);

            Log.d(TAG, "Successfully copied " + afile.getAbsolutePath() + " to " + bfile.getAbsolutePath());
            return true;

        } catch(IOException e){
            e.printStackTrace();
        }
        Log.d(TAG, "Failed to copy file from " + file.getAbsolutePath() + " to " + targetDir);
        return false;
    }

}
