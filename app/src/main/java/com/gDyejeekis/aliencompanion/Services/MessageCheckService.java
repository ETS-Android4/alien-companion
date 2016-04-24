package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.MessageActivity;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.retrieval.Messages;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

import java.util.List;

/**
 * Created by sound on 4/10/2016.
 */
public class MessageCheckService extends IntentService {

    public static final String TAG = "MessageCheckService";

    public static final int SERVICE_ID = 5311;

    public static final int SERVICE_NOTIF_ID = 6311;

    public MessageCheckService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent i) {
        if(!MessageActivity.isActive) {
            Log.d(TAG, "Checking for new messages..");
            if (MyApplication.currentAccount == null) {
                MyApplication.currentAccount = MyApplication.getCurrentAccount(this);
            }

            try {
                if (MyApplication.currentAccount.loggedIn) {
                    HttpClient httpClient = new PoliteRedditHttpClient();
                    MyApplication.currentUser = new User(httpClient, MyApplication.currentAccount.getUsername(), MyApplication.currentAccount.getToken());
                    MyApplication.currentAccessToken = MyApplication.currentAccount.getToken().accessToken;

                    Messages msgRetrieval = new Messages(httpClient, MyApplication.currentUser);
                    List<RedditItem> messages = msgRetrieval.ofUser(MessageCategory.INBOX, MessageCategorySort.UNREAD, -1, 1000, null, null, true);
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putBoolean("newMessages", messages.size() > 0);
                    editor.commit();

                    if (messages.size() > 0) {
                        Log.d(TAG, messages.size() + " new messages");
                        MainActivity.notifyDrawerChanged = true;
                        Message lastMessage = (Message) messages.get(0);
                        showNewMessagesNotif(messages.size(), lastMessage.createdUTC);
                    } else {
                        Log.d(TAG, "No new messages");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showNewMessagesNotif(int count, double lastMessageUtc) {
        String title = "Alien Companion - " + count + " new message";
        if(count > 1) title = title.concat("s");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_mail_white_48dp)
                .setContentTitle(title)
                .setContentText("Last message received " + ConvertUtils.getSubmissionAge(lastMessageUtc));

        Notification notif = builder.build();
        notif.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(SERVICE_NOTIF_ID, notif);
    }
}
