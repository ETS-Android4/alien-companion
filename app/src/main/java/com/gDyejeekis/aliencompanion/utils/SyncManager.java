package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.services.DownloaderService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by George on 2/10/2017.
 */

public class SyncManager {

    private static final String NETWORK_UNAVAILABLE_MESSAGE = "Network connection unavailable";

    private static final String DATA_SYNC_DISABLED_MESSAGE = "Syncing over mobile data connection is disabled";

    private static final String ALREADY_IN_QUEUE_MESSAGE = "Item already in sync queue";

    private static final String ADD_TO_QUEUE_FAILED_MESSAGE = "Failed to add item to sync queue";

    public static class SyncQueueItem {
        private String name;
        private Intent intent;
        private String toastMessage;

        public SyncQueueItem(Context context, String subreddit, boolean isMulti, SubmissionSort sort, TimeSpan time) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("subreddit", subreddit);
            intent.putExtra("isMulti", isMulti);
            intent.putExtra("sort", sort);
            intent.putExtra("time", time);
            if(subreddit == null) {
                name = "frontpage";
            }
            else {
                name = isMulti ? subreddit + " (multi)" : subreddit;
            }
            toastMessage = name + " added to sync queue";
        }

        public SyncQueueItem(Context context, Submission post) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("post", post);
            toastMessage = "Post added to sync queue";
            name = post.getFullName();
        }

        public SyncQueueItem(Context context, SyncProfile syncProfile, boolean reschedule) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("profile", syncProfile);
            intent.putExtra("reschedule", reschedule);
            toastMessage = syncProfile.getName() + " added to sync queue";
            name = syncProfile.getProfileId();
        }

        public SyncQueueItem(Context context, String syncProfileName, String syncProfileId, boolean reschedule) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("profileId", syncProfileId);
            intent.putExtra("reschedule", reschedule);
            toastMessage = syncProfileName + " added to sync queue";
            name = syncProfileId;
        }

        String getName() {
            return name;
        }

        Intent getSyncIntent() {
            return intent;
        }

        String getToastMessage() {
            return toastMessage;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof SyncQueueItem) {
                if(((SyncQueueItem) o).getName().equalsIgnoreCase(this.name)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static Queue<SyncQueueItem> queue = new LinkedList<>();

    public static void addToSyncQueue(Context context, SyncQueueItem item) {
        String toastMessage;
        if(queue.contains(item)) {
            toastMessage = ALREADY_IN_QUEUE_MESSAGE;
        }
        else {
            if (GeneralUtils.isNetworkAvailable(context)) {
                if (MyApplication.syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(context)) {
                    toastMessage = DATA_SYNC_DISABLED_MESSAGE;
                }
                else {
                    if (queue.add(item)) {
                        toastMessage = item.getToastMessage();
                        context.startService(item.getSyncIntent());
                    } else {
                        toastMessage = ADD_TO_QUEUE_FAILED_MESSAGE;
                    }
                }
            } else {
                toastMessage = NETWORK_UNAVAILABLE_MESSAGE;
            }
        }
        ToastUtils.showToast(context, toastMessage);
    }

    public static void pollSyncQueue() {
        queue.poll();
    }
}
