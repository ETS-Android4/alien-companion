package com.gDyejeekis.aliencompanion.models.sync_profile;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

import java.io.Serializable;

/**
 * Created by sound on 3/20/2016.
 */
public class SyncOptions implements Serializable {

    private static final long serialVersionUID = 1234543L;

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

    public boolean isSyncVideo() {
        return syncVideo;
    }

    public void setSyncVideo(boolean syncVideo) {
        this.syncVideo = syncVideo;
    }

    private boolean syncVideo;

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

    public int getSyncSelfTextLinkCount() {
        return syncSelfTextLinkCount;
    }

    public void setSyncSelfTextLinkCount(int syncSelfTextLinkCount) {
        this.syncSelfTextLinkCount = syncSelfTextLinkCount;
    }

    private int syncSelfTextLinkCount;

    public int getSyncCommentLinkCount() {
        return syncCommentLinkCount;
    }

    public void setSyncCommentLinkCount(int syncCommentLinkCount) {
        this.syncCommentLinkCount = syncCommentLinkCount;
    }

    private int syncCommentLinkCount;

    public boolean isSyncNewPostsOnly() {
        return syncNewPostsOnly;
    }

    public void setSyncNewPostsOnly(boolean syncNewPostsOnly) {
        this.syncNewPostsOnly = syncNewPostsOnly;
    }

    private boolean syncNewPostsOnly;

    @Override
    public boolean equals(Object obj) {
        if (obj!=null && obj instanceof SyncOptions) {
            SyncOptions other = (SyncOptions) obj;
            if (syncPostCount != other.getSyncPostCount()) return false;
            if (syncCommentCount != other.getSyncCommentCount()) return false;
            if (syncCommentDepth != other.getSyncCommentDepth()) return false;
            if (syncCommentSort != other.getSyncCommentSort()) return false;
            if (syncThumbs != other.isSyncThumbs()) return false;
            if (syncImages != other.isSyncImages()) return false;
            if (syncVideo != other.isSyncVideo()) return false;
            if (albumSyncLimit != other.getAlbumSyncLimit()) return false;
            if (syncOverWifiOnly != other.isSyncOverWifiOnly()) return false;
            if (syncSelfTextLinkCount != other.getSyncSelfTextLinkCount()) return false;
            if (syncCommentLinkCount != other.getSyncCommentLinkCount()) return false;
            if (syncNewPostsOnly != other.isSyncNewPostsOnly()) return false;
            return true;
        }
        return false;
    }

    public SyncOptions(SyncOptions other) {
        if (other!=null) {
            this.syncPostCount = other.getSyncPostCount();
            this.syncCommentCount = other.getSyncCommentCount();
            this.syncCommentDepth = other.getSyncCommentDepth();
            this.syncCommentSort = other.getSyncCommentSort();
            this.syncThumbs = other.isSyncThumbs();
            this.syncImages = other.isSyncImages();
            this.syncVideo = other.isSyncVideo();
            this.albumSyncLimit = other.getAlbumSyncLimit();
            this.syncOverWifiOnly = other.isSyncOverWifiOnly();
            this.syncWebpages = other.isSyncWebpages();
            this.syncGif = other.isSyncGif();
            this.syncSelfTextLinkCount = other.getSyncSelfTextLinkCount();
            this.syncCommentLinkCount = other.getSyncCommentLinkCount();
            this.syncNewPostsOnly = other.isSyncNewPostsOnly();
        }
    }

    public SyncOptions() {
        syncPostCount = MyApplication.syncPostCount;
        syncCommentCount = MyApplication.syncCommentCount;
        syncCommentDepth = MyApplication.syncCommentDepth;
        syncCommentSort = MyApplication.syncCommentSort;
        syncThumbs = MyApplication.syncThumbnails;
        syncImages = MyApplication.syncImages;
        syncVideo = MyApplication.syncVideo;
        albumSyncLimit = MyApplication.syncAlbumImgCount;
        syncOverWifiOnly = MyApplication.syncOverWifiOnly;
        syncWebpages = MyApplication.syncWebpages;
        syncGif = MyApplication.syncGif;
        syncSelfTextLinkCount = MyApplication.syncSelfTextLinkCount;
        syncCommentLinkCount = MyApplication.syncCommentLinkCount;
        syncNewPostsOnly = MyApplication.syncNewPostsOnly;
    }



}
