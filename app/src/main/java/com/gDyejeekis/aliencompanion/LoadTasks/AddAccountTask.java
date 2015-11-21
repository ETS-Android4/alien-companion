package com.gDyejeekis.aliencompanion.LoadTasks;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditOAuth;

import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 8/18/2015.
 */
public class AddAccountTask extends AsyncTask<Void, Void, SavedAccount> {

    private DialogFragment dialogFragment;
    private Context context;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private String username;
    private String password;
    private String oauthCode;
    private Exception exception;

    private String currentAccountName;

    private static final String DEBUG_USER = "user account info";

    public AddAccountTask(DialogFragment dialogFragment, String username, String password) {
        this.dialogFragment = dialogFragment;
        context = dialogFragment.getActivity();
        //httpClient = new PoliteRedditHttpClient();
        this.username =  username;
        this.password = password;
    }

    public AddAccountTask(DialogFragment dialogFragment, String oauthCode) {
        this.dialogFragment = dialogFragment;
        context = dialogFragment.getActivity();
        //httpClient = new PoliteRedditHttpClient();
        this.oauthCode = oauthCode;
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
        try {
            if(RedditOAuth.useOAuth2) {
                RedditOAuth.getOAuthToken(httpClient, oauthCode);
            }
            else {
                User user = new User(httpClient, username, password);
                user.connect();
                List<String> subredditNames = new ArrayList<>();

                subredditNames.add("All");
                try {
                    List<Subreddit> subreddits = user.getSubscribed(0);
                    for (Subreddit subreddit : subreddits) {
                        subredditNames.add(subreddit.getDisplayName());
                    }
                } catch (RetrievalFailedException | NullPointerException | RedditError e) {
                    e.printStackTrace();
                    //Collections.addAll(subredditNames, RedditConstants.defaultSubscribed);
                }

                SavedAccount newAccount = new SavedAccount(username, user.getModhash(), user.getCookie(), subredditNames);

                List<SavedAccount> accounts = readFromFile();
                //if(accounts == null) accounts = new ArrayList<>();
                assert accounts != null;
                accounts.add(newAccount);
                writeToFile(accounts);

                currentAccountName = newAccount.getUsername();
                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                editor.putString("currentAccountName", currentAccountName);
                editor.apply();

                return newAccount;
            }
        } catch (ActionFailedException | IOException | ParseException | NullPointerException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(SavedAccount account) {
        dialogFragment.dismiss();
        if(exception != null || account == null) {
            ToastUtils.displayShortToast(context, "Failed to verify account");
        }
        else {
            //ToastUtils.displayShortToast(context, "Logged in as " + username);
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.getNavDrawerAdapter().accountAdded(new NavDrawerAccount(account), currentAccountName);
            mainActivity.changeCurrentUser(account);
        }
    }
}
