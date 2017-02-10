package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.services.DownloaderService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by George on 2/10/2017.
 */

public class SyncManager {

    public static final String NETWORK_UNAVAIBLE_MESSAGE = "Network connection unavailable";

    public static final String DATA_SYNC_DISABLED_MESSAGE = "Syncing over mobile data connection is disabled";

    public static final String ADD_TO_QUEUE_FAILED_MESSAGE = "Failed to add item to sync queue";

    public static class SyncQueueItem {
        private Intent intent;
        private String successMessage;

        public SyncQueueItem(Context context, String subreddit, boolean isMulti, SubmissionSort sort, TimeSpan time) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("subreddit", subreddit);
            intent.putExtra("isMulti", isMulti);
            intent.putExtra("sort", sort);
            intent.putExtra("time", time);
            String name;
            if(subreddit == null) {
                name = "frontpage";
            }
            else {
                name = isMulti ? subreddit + " (multi)" : "/r/" + subreddit;
            }
            successMessage = name + " added to sync queue";
        }

        public SyncQueueItem(Context context, Submission post) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("post", post);
            successMessage = "Post added to sync queue";
        }

        public SyncQueueItem(Context context, SyncProfile syncProfile, boolean reschedule) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("profile", syncProfile);
            intent.putExtra("reschedule", reschedule);
            successMessage = syncProfile.getName() + " added to sync queue";
        }

        public SyncQueueItem(Context context, String syncProfileName, int syncProfileId, boolean reschedule) {
            intent = new Intent(context, DownloaderService.class);
            intent.putExtra("profileId", syncProfileId);
            intent.putExtra("reschedule", reschedule);
            successMessage = syncProfileName + " added to sync queue";
        }

        public Intent getSyncIntent() {
            return intent;
        }

        public String getSuccessMessage() {
            return successMessage;
        }
    }

    public static Queue<SyncQueueItem> queue = new LinkedList<>();

    public static void addToSyncQueue(Context context, SyncQueueItem item) {
        String toastMessage;
        if(GeneralUtils.isNetworkAvailable(context)) {
            if(MyApplication.syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(context)) {
                toastMessage = DATA_SYNC_DISABLED_MESSAGE;
            }
            else {
                if(queue.add(item)) {
                    toastMessage = item.getSuccessMessage();
                    context.startService(item.getSyncIntent());
                }
                else {
                    toastMessage = ADD_TO_QUEUE_FAILED_MESSAGE;
                }
            }
        }
        else {
            toastMessage = NETWORK_UNAVAIBLE_MESSAGE;
        }
        ToastUtils.displayShortToast(context, toastMessage);
    }

    public static void pollSyncQueue(Context context) {
        // TODO: 2/10/2017
    }
}
