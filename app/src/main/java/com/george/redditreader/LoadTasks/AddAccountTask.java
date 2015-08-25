package com.george.redditreader.LoadTasks;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Models.SavedAccount;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.exception.ActionFailedException;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;

import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 8/18/2015.
 */
public class AddAccountTask extends AsyncTask<Void, Void, SavedAccount> {

    private DialogFragment dialogFragment;
    private Context context;
    private HttpClient httpClient;
    private String username;
    private String password;
    private Exception exception;

    private static final String DEBUG_USER = "user account info";

    public AddAccountTask(DialogFragment dialogFragment, String username, String password) {
        this.dialogFragment = dialogFragment;
        context = dialogFragment.getActivity();
        httpClient = new RedditHttpClient();
        this.username =  username;
        this.password = password;
    }

    private List<SavedAccount> readFromFile() {
        try {
            FileInputStream fis = context.openFileInput(MainActivity.SAVED_ACCOUNTS_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SavedAccount> savedAccounts = (List<SavedAccount>) is.readObject();
            is.close();
            fis.close();
            return savedAccounts;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveToFile(List<SavedAccount> updatedAccounts) {
        try {
            FileOutputStream fos = context.openFileOutput(MainActivity.SAVED_ACCOUNTS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(updatedAccounts);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected SavedAccount doInBackground(Void... unused) {
        //SystemClock.sleep(5000);
        try {
            //User user;
            //try {
            //    user = new User(httpClient, URLEncoder.encode(username, "utf-8"), URLEncoder.encode(password, "utf-8"));
            //} catch (UnsupportedEncodingException e) {
            //    e.printStackTrace();
            //    user = new User(httpClient, username, password);
            //}
            User user = new User(httpClient, username, password);
            MainActivity.currentUser = user;
            user.connect();

            SavedAccount newAccount = new SavedAccount(username, password);

            List<SavedAccount> accounts = readFromFile();
            if(accounts == null) accounts = new ArrayList<>();
            accounts.add(newAccount);
            saveToFile(accounts);

            SharedPreferences.Editor editor = MainActivity.prefs.edit();
            editor.putInt("currentAccountIndex", accounts.size() + 1);
            editor.apply();

            return newAccount;
            //user.getSubscribed(0);
            //ProfileActions profileActions = new ProfileActions(httpClient, user);
            //profileActions.getUserInformation();
        } catch (RetrievalFailedException | ActionFailedException | IOException | ParseException | NullPointerException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(SavedAccount account) {
        dialogFragment.dismiss();
        if(exception != null) {
            ToastUtils.displayShortToast(context, "Failed to verify account");
            //dialogFragment.dismiss();
        }
        else {
            ToastUtils.displayShortToast(context, "Logged in as " + username);

            MainActivity mainActivity = (MainActivity) context;
            mainActivity.changeCurrentUser(account);
            //Intent mStartActivity = new Intent(context, MainActivity.class);
            //int mPendingIntentId = 123456;
            //PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            //AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            //mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            //System.exit(0);
        }
    }
}
