package com.gDyejeekis.aliencompanion.Models;

import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

import java.io.Serializable;

/**
 * Created by sound on 3/20/2016.
 */
public class SyncProfileOptions implements Serializable {

    public int getSyncPostCount() {
        return syncPostCount;
    }

    public void setSyncPostCount(int syncPostCount) {
        this.syncPostCount = syncPostCount;
    }

    private int syncPostCount;

    public int getSyncCommentCount() {
        return syncCommentCount;
    }

    public void setSyncCommentCount(int syncCommentCount) {
        this.syncCommentCount = syncCommentCount;
    }

    private int syncCommentCount;

    public int getSyncCommentDepth() {
        return syncCommentDepth;
    }

    public void setSyncCommentDepth(int syncCommentDepth) {
        this.syncCommentDepth = syncCommentDepth;
    }

    private int syncCommentDepth;

    public CommentSort getSyncCommentSort() {
        return syncCommentSort;
    }

    public void setSyncCommentSort(CommentSort syncCommentSort) {
        this.syncCommentSort = syncCommentSort;
    }

    private CommentSort syncCommentSort;

    public boolean isSyncThumbs() {
        return syncThumbs;
    }

    public void setSyncThumbs(boolean syncThumbs) {
        this.syncThumbs = syncThumbs;
    }

    private boolean syncThumbs;

    public boolean isSyncImages() {
        return syncImages;
    }

    public void setSyncImages(boolean syncImages) {
        this.syncImages = syncImages;
    }

    private boolean syncImages;

    public int getAlbumSyncLimit() {
        return albumSyncLimit;
    }

    public void setAlbumSyncLimit(int albumSyncLimit) {
        this.albumSyncLimit = albumSyncLimit;
    }

    private int albumSyncLimit;

    public boolean isSyncGif() {
        return syncGif;
    }

    public void setSyncGif(boolean syncGif) {
        this.syncGif = syncGif;
    }

    private boolean syncGif;

    public int getSyncImagesInCommentsCount() {
        return syncImagesInCommentsCount;
    }

    public void setSyncImagesInCommentsCount(int syncImagesInCommentsCount) {
        this.syncImagesInCommentsCount = syncImagesInCommentsCount;
    }

    private int syncImagesInCommentsCount;

    public boolean isSyncOverWifiOnly() {
        return syncOverWifiOnly;
    }

    public void setSyncOverWifiOnly(boolean syncOverWifiOnly) {
        this.syncOverWifiOnly = syncOverWifiOnly;
    }

    private boolean syncOverWifiOnly;

    public boolean isSyncWebpages() {
        return syncWebpages;
    }

    public void setSyncWebpages(boolean syncWebpages) {
        this.syncWebpages = syncWebpages;
    }

    private boolean syncWebpages;

    public SyncProfileOptions() {
        syncPostCount = 25;
        syncCommentCount = 100;
        syncCommentDepth = 5;
        syncCommentSort = CommentSort.TOP;
        syncThumbs = false;
        syncImages = false;
        albumSyncLimit = 1;
        syncOverWifiOnly = true;
        syncWebpages = false;
        syncGif = false;
        syncImagesInCommentsCount = 0;
    }



}
