package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.dyejeekis.aliencompanion.Models.SavedAccount;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.entity.Subreddit;
import com.dyejeekis.aliencompanion.api.entity.User;
import com.dyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    private String currentAccountName;

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

    private void writeToFile(List<SavedAccount> updatedAccounts) {
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
        User user = new User(httpClient, username, password);
        try {
            //MainActivity.currentUser = user;
            user.connect();
            List<String> subredditNames = new ArrayList<>();

            try {
                List<Subreddit> subreddits = user.getSubscribed(0);
                if(subreddits.size()==0) Collections.addAll(subredditNames, RedditConstants.defaultSubscribed);
                else {
                    for (Subreddit subreddit : subreddits) {
                        subredditNames.add(subreddit.getDisplayName());
                    }
                }
            } catch (RetrievalFailedException | NullPointerException | RedditError e) {
                e.printStackTrace();
                Collections.addAll(subredditNames, RedditConstants.defaultSubscribed);
            }

            //SavedAccount newAccount = new SavedAccount(username, password);
            SavedAccount newAccount = new SavedAccount(username, user.getModhash(), user.getCookie(), subredditNames);

            List<SavedAccount> accounts = readFromFile();
            if(accounts == null) accounts = new ArrayList<>();
            accounts.add(newAccount);
            writeToFile(accounts);

            currentAccountName = newAccount.getUsername();
            SharedPreferences.Editor editor = MainActivity.prefs.edit();
            editor.putString("currentAccountName", currentAccountName);
            editor.apply();

            return newAccount;
            //user.getSubscribed(0);
            //ProfileActions profileActions = new ProfileActions(httpClient, user);
            //profileActions.getUserInformation();
        } catch (ActionFailedException | IOException | ParseException | NullPointerException e) {
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
            //ToastUtils.displayShortToast(context, "Logged in as " + username);

            MainActivity mainActivity = (MainActivity) context;
            mainActivity.getNavDrawerAdapter().accountAdded(new NavDrawerAccount(account), currentAccountName);
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
