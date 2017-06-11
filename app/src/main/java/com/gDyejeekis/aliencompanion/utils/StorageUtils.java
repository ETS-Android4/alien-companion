package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    /**
     * Search file a file in a directory. Please comment more here, your method is not that standard.
     * @param aFile the file / folder where to look our file for.
     * @param sDir a directory that must be in the path of the file to find
     * @param toFind the name of file we are looking for.
     * @return the file we were looking for. Null if no such file could be found.
     */
    public static File findFile( File aFile, String sDir, String toFind ){
        if( aFile.isFile() &&
                aFile.getAbsolutePath().contains( sDir ) &&
                aFile.getName().contains( toFind ) ) {
            return aFile;
        } else if( aFile.isDirectory() ) {
            for( File child : aFile.listFiles() ){
                File found = findFile( child, sDir, toFind );
                if( found != null ) {
                    return found;
                }//if
            }//for
        }//else
        return null;
    }//met

    public static void deleteRecursive(File file) {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(File file1 : files) {
                deleteRecursive(file1);
            }
        }
        else {
            file.delete();
        }
    }

    public static void listFilesRecursive(File dir, FileFilter filter, List<File> files) {
        File[] fList = dir.listFiles(filter);
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFilesRecursive(file, filter, files);
            }
        }
    }

    public static File oldestFileInDir(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long firstMod = Long.MAX_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() < firstMod) {
                choice = file;
                firstMod = file.lastModified();
            }
        }
        return choice;
    }

    /**
     * Recursively logs all file names and their size in a directory
     * @param dir
     */
    public static void logFilesInDir(File dir) {
        File[] files = dir.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                logFilesInDir(file);
            }
            else {
                Log.d(TAG, file.getName() + " " + file.length());
            }
        }
    }

    /**
     * Recursively deletes a directory and all its files
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
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

    /**
     *
     * @param dir
     * @param recursive whether to add size of subdirectories to total
     * @return size of directory
     */
    public static long dirSize(File dir, boolean recursive) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if(recursive && fileList[i].isDirectory()) {
                    result += dirSize(fileList [i], true);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    /**
     *
     * @param parentDir
     * @return a list of all files in a parent directory
     */
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

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean safeCopy(File src, File dst) {
        try {
            copy(src, dst);
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

    public static boolean moveFileBetweenDisks(File src, String targetDir) {
        try{
            File afile = src;
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

            // delete the original file
            boolean success = afile.delete();
            if(success) {
                Log.d(TAG, "Successfully moved " + afile.getAbsolutePath() + " to " + bfile.getAbsolutePath());
            }
            else {
                Log.d(TAG, "Copied " + afile.getAbsolutePath() + " to " + bfile.getAbsolutePath());
                Log.d(TAG, "Failed to delete original file");
            }
            return true;

        } catch(IOException e){
            e.printStackTrace();
        }
        Log.e(TAG, "Failed to move file from " + src.getAbsolutePath() + " to " + targetDir);
        return false;
    }

    public static boolean moveFileBetweenDisksRecursive(File src, String targetDir) {
        try {
            if(src.isDirectory()) {
                File aDir = src;
                File bDir = new File(targetDir, aDir.getName());
                if(!bDir.exists()) {
                    boolean success = bDir.mkdir();
                    if(!success) {
                        Log.e(TAG, "Failed to create directory " + bDir.getAbsolutePath());
                        return false;
                    }
                }

                File[] files = aDir.listFiles();
                for(File file : files) {
                    moveFileBetweenDisksRecursive(file, bDir.getAbsolutePath());
                }

                // delete the original dir
                boolean success = aDir.delete();
                if(success) {
                    Log.d(TAG, "Successfully moved " + aDir.getAbsolutePath() + " to " + bDir.getAbsolutePath());
                }
                else {
                    Log.d(TAG, "Copied " + aDir.getAbsolutePath() + " to " + bDir.getAbsolutePath());
                    Log.d(TAG, "Failed to delete original directory");
                }
                return true;
            }
            else {
                return moveFileBetweenDisks(src, targetDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Failed to move file from " + src.getAbsolutePath() + " to " + targetDir);
        return false;
    }

}
