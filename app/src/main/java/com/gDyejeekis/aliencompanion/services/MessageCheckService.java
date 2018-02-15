package com.gDyejeekis.aliencompanion.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.api.retrieval.Messages;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

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
            try {
                HttpClient httpClient = new PoliteRedditHttpClient();
                MyApplication.checkAccountInit(this, httpClient);
                if (MyApplication.currentAccount.loggedIn) {
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
        Intent intent = new Intent(this, MessageActivity.class);
        intent.setClass(getApplicationContext(), MessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("viewNew", true);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String title = "Alien Companion - " + count + " new message";
        if(count > 1) title = title.concat("s");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_mail_white_48dp)
                .setContentTitle(title)
                .setContentText("Last message received " + ConvertUtils.getSubmissionAge(lastMessageUtc))
                .setContentIntent(pIntent);

        Notification notif = builder.build();
        notif.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(SERVICE_NOTIF_ID, notif);
    }
}
