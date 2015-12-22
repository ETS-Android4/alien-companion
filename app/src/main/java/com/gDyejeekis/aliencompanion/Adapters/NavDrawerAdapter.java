package com.gDyejeekis.aliencompanion.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.AccountListener;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.HeaderListener;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.MenuItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.NavDrawerListener;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.SubredditItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners.SubredditsListener;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.enums.MenuType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by George on 6/25/2015.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_HEADER = 0;

    public static final int VIEW_TYPE_MENU_ITEM = 1;

    public static final int VIEW_TYPE_SUBREDDITS = 2;

    public static final int VIEW_TYPE_SUBREDDIT_ITEM = 3;

    public static final int VIEW_TYPE_ACCOUNT = 4;

    //public static final int VIEW_TYPE_SEPARATOR = 5;

    private final MainActivity activity;

    private List<NavDrawerItem> items;

    private boolean accountItemsVisible;
    private boolean subredditItemsVisible;

    private List<NavDrawerItem> subredditItems;
    public List<NavDrawerAccount> accountItems;

    public static String currentAccountName;

    private NavDrawerMenuItem profile;
    private NavDrawerMenuItem messages;
    private boolean userMenuItemsVisible;

    public NavDrawerAdapter(MainActivity activity) {
        items = new ArrayList<>();
        this.activity = activity;
        subredditItemsVisible = true;
        accountItemsVisible = false;
        currentAccountName = "Logged out";
        profile = new NavDrawerMenuItem(MenuType.profile);
        messages = new NavDrawerMenuItem(MenuType.messages);
        //importAccounts();
        Log.d("current account name", "at init : " + currentAccountName);
    }

    public void setCurrentAccountName(String name) {
        currentAccountName = name;
        toggleAccountItems();
        Log.d("current account name", "current : " + currentAccountName);
    }

    public void showUserMenuItems() {
        if(!userMenuItemsVisible) {
            add(1, profile);
            add(2, messages);
            userMenuItemsVisible = true;
        }
    }

    public void hideUserMenuItems() {
        items.remove(profile);
        items.remove(messages);
        userMenuItemsVisible = false;
    }

    public void updateSubredditItems(List<String> subredditNames) {
        subredditItems = new ArrayList<>();
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) subredditItems.add(item);
        }
        items.removeAll(subredditItems);
        subredditItems.clear();
        subredditItems.add(new NavDrawerSubredditItem());
        for(String name : subredditNames) {
            subredditItems.add(new NavDrawerSubredditItem(name));
        }
        items.addAll(subredditItems);
        notifyDataSetChanged();
    }

    public void importAccounts() {
        currentAccountName = MyApplication.prefs.getString("currentAccountName", "Logged out");
        List<SavedAccount> savedAccounts = readAccounts();

        SavedAccount currentAccount = null;
        accountItems = new ArrayList<>();
        //accountItems.add(new NavDrawerAccount(1));
        if(savedAccounts != null) {
            for(SavedAccount account : savedAccounts) {
                if(!account.loggedIn) accountItems.add(new NavDrawerAccount(account, true));
                else accountItems.add(new NavDrawerAccount(account));
                if(account.getUsername().equals(currentAccountName)) currentAccount = account;
            }
        }
        else {
            List<String> subreddits = new ArrayList<>();
            Collections.addAll(subreddits, MyApplication.defaultSubredditStrings);
            SavedAccount loggedOut = new SavedAccount(subreddits);
            accountItems.add(new NavDrawerAccount(loggedOut, true));
            currentAccount = loggedOut;
            List<SavedAccount> accounts = new ArrayList<>();
            accounts.add(loggedOut);
            saveAccounts(accounts);
        }
        accountItems.add(new NavDrawerAccount(0));

        activity.changeCurrentUser(currentAccount);
    }

    public void accountAdded(NavDrawerAccount accountItem, String name) {
        items.add(accountItems.size(), accountItem);
        accountItems.add(accountItems.size()-1, accountItem);
        setCurrentAccountName(name);
        notifyDataSetChanged();
        //Log.d("current account name", "after add : " + currentAccountName);
    }

    public List<SavedAccount> readAccounts() {
        try {
            FileInputStream fis = activity.openFileInput(MyApplication.SAVED_ACCOUNTS_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SavedAccount> savedAccounts = (List<SavedAccount>) is.readObject();
            is.close();
            fis.close();
            return savedAccounts;
        } catch (IOException | ClassNotFoundException e) {
            if(e instanceof ClassNotFoundException) {
                Log.d("geotest", "account data deprecated or corrupt, clearing account data...");
                GeneralUtils.deleteAccountData(activity);
            }
            e.printStackTrace();
        }
        return null;
    }

    public void saveAccounts(List<SavedAccount> updatedAccounts) {
        try {
            FileOutputStream fos = activity.openFileOutput(MyApplication.SAVED_ACCOUNTS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(updatedAccounts);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteAccount(String accountToDelete) {
        Log.d("current account name", "before delete : " + currentAccountName);
        List<SavedAccount> accounts = readAccounts();
        for(SavedAccount account : accounts) {
            if(account.getUsername().equals(accountToDelete)) {
                accounts.remove(account);
                break;
            }
        }
        saveAccounts(accounts);
        boolean justRemove = true;
        int i =0;
        for(NavDrawerAccount account : accountItems) {
            if(account.getName().equals(accountToDelete)) {
                accountItems.remove(account);
                items.remove(account);
                if(currentAccountName.equals(accountToDelete)) {
                    currentAccountName = "Logged out";
                    justRemove = false;
                }
                break;
            }
            i++;
        }
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putString("currentAccountName", currentAccountName);
        editor.apply();
        if(justRemove) notifyItemRemoved(i+1);
        else {
            activity.getDrawerLayout().closeDrawers();
            activity.changeCurrentUser(accountItems.get(0).savedAccount);
        }
        //Log.d("current account name", "after delete : " + currentAccountName);
    }

    public void add(NavDrawerItem item) {
        items.add(item);
    }

    public void add(int location, NavDrawerItem item) {
        items.add(location, item);
    }

    public void addAll(List<NavDrawerItem> items) {
        this.items.addAll(items);
    }

    public NavDrawerItem getItemAt(int position) {
        return items.get(position);
    }

    public void toggleAccountItems() {
        if(accountItemsVisible) collapseAccountItems();
        else expandAccountItems();
    }

    private void collapseAccountItems() {
        items.removeAll(accountItems);
        accountItemsVisible = false;
        //notifyDataSetChanged();
        notifyItemChanged(0);
        notifyItemRangeRemoved(1, accountItems.size());
    }

    private void expandAccountItems() {
        int i = 1;
        for(NavDrawerAccount accountItem : accountItems) {
            items.add(i, accountItem);
            i++;
        }
        accountItemsVisible = true;
        //notifyDataSetChanged();
        notifyItemChanged(0);
        notifyItemRangeInserted(1, accountItems.size());
    }

    public void toggleSubredditItems() {
        if(subredditItemsVisible) collapseSubredditItems();
        else expandSubredditItems();
    }

    private void collapseSubredditItems() {
        subredditItems = new ArrayList<>();
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) subredditItems.add(item);
        }
        items.removeAll(subredditItems);
        subredditItemsVisible = false;
        //notifyDataSetChanged();
        notifyItemChanged(items.size()-1);
        notifyItemRangeRemoved(items.size(), subredditItems.size());
    }

    private void expandSubredditItems() {
        int position = items.size();
        items.addAll(subredditItems);
        subredditItemsVisible = true;
        //notifyDataSetChanged();
        notifyItemChanged(position-1);
        notifyItemRangeInserted(position, subredditItems.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                int resource = R.layout.drawer_header;
                NavDrawerListener listener = new HeaderListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new HeaderListener(activity));
                viewHolder = new HeaderViewHolder(v);
                break;
            case VIEW_TYPE_MENU_ITEM:
                resource = R.layout.drawer_menu_row;
                listener = new MenuItemListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new MenuItemListener(activity));
                viewHolder = new MenuRowViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDITS:
                resource = R.layout.drawer_subreddits;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new SubredditsViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                resource = R.layout.drawer_subreddit_row;
                listener = new SubredditItemListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                //v.setOnClickListener(new SubredditItemListener(activity));
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_ACCOUNT:
                resource = R.layout.drawer_account_row;
                listener = new AccountListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                v.setOnLongClickListener(listener);
                //v.setOnClickListener(new AccountListener(activity));
                viewHolder = new SubredditRowViewHolder(v);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
                headerViewHolder.headerLayout.setBackgroundColor(MyApplication.currentColor);
                headerViewHolder.currentAccount.setText(currentAccountName);
                headerViewHolder.themeSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = (MyApplication.nightThemeEnabled) ? "Switch to light mode?" : "Switch to dark mode?";
                        text += " (App will restart)";
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MyApplication.nightThemeEnabled = !MyApplication.nightThemeEnabled;
                                SharedPreferences.Editor editor = MyApplication.prefs.edit();
                                editor.putBoolean("nightTheme", MyApplication.nightThemeEnabled);
                                editor.apply();
                                restartApp();
                            }
                        };
                        new AlertDialog.Builder(activity).setMessage(text).setPositiveButton("Yes", listener)
                                .setNegativeButton("No", null).show();
                        //ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(text, listener);
                        //dialog.show(activity.getFragmentManager(), "dialog");
                    }
                });
                //headerViewHolder.themeSwitch.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //        MainActivity.nightThemeEnabled = !MainActivity.nightThemeEnabled;
                //        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                //        editor.putBoolean("nightTheme", MainActivity.nightThemeEnabled);
                //        editor.apply();
                //        restartApp();
                //    }
                //});
                headerViewHolder.offlineSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = (MyApplication.offlineModeEnabled) ? "Switch to online mode?" : "Switch to offline mode?";
                        text += " (App will restart)";
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MyApplication.offlineModeEnabled = !MyApplication.offlineModeEnabled;
                                SharedPreferences.Editor editor = MyApplication.prefs.edit();
                                editor.putBoolean("offlineMode", MyApplication.offlineModeEnabled);
                                editor.apply();
                                restartApp();
                            }
                        };
                        new AlertDialog.Builder(activity).setMessage(text).setPositiveButton("Yes", listener)
                                .setNegativeButton("No", null).show();
                        //ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(text, listener);
                        //dialog.show(activity.getFragmentManager(), "dialog");
                    }
                });
                //headerViewHolder.offlineSwitch.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //        MainActivity.offlineModeEnabled = !MainActivity.offlineModeEnabled;
                //        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                //        editor.putBoolean("offlineMode", MainActivity.offlineModeEnabled);
                //        editor.apply();
                //        restartApp();
                //    }
                //});
                //if(MainActivity.nightThemeEnabled) {
                    headerViewHolder.themeSwitch.setImageResource(R.drawable.ic_action_theme_switch);
                    if(MyApplication.offlineModeEnabled) headerViewHolder.offlineSwitch.setImageResource(R.drawable.ic_action_offline);
                    else headerViewHolder.offlineSwitch.setImageResource(R.drawable.ic_action_online);
                //}
                //else {
                //    headerViewHolder.themeSwitch.setImageResource(R.mipmap.ic_action_night_switch_grey);
                //    if(MainActivity.offlineModeEnabled) headerViewHolder.offlineSwitch.setImageResource(R.mipmap.ic_action_offline_mode_grey);
                //    else headerViewHolder.offlineSwitch.setImageResource(R.mipmap.ic_action_online_mode_grey);
                //}
                if(accountItemsVisible) headerViewHolder.toggle.setImageResource(R.mipmap.ic_action_collapse_white);
                else headerViewHolder.toggle.setImageResource(R.mipmap.ic_action_expand_white);
                break;
            case VIEW_TYPE_MENU_ITEM:
                MenuRowViewHolder menuRowViewHolder= (MenuRowViewHolder) viewHolder;
                NavDrawerMenuItem menuItem = (NavDrawerMenuItem) getItemAt(position);
                menuRowViewHolder.name.setText(menuItem.getMenuType().value());
                switch (menuItem.getMenuType()) {
                    case profile:
                        if(MyApplication.nightThemeEnabled) menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_profile_white);
                        else menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_profile_grey);
                        break;
                    case messages:
                        if(MyApplication.nightThemeEnabled) menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_messages_white);
                        else menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_messages_grey);
                        break;
                    case user:
                        if(MyApplication.nightThemeEnabled) menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_user_white);
                        else menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_user_grey);
                        break;
                    case subreddit:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_subreddit);
                        break;
                    case settings:
                        if(MyApplication.nightThemeEnabled) menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_settings_white);
                        else menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_settings_grey);
                        break;
                    case cached:
                        if(MyApplication.nightThemeEnabled) menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_profile_grey);
                        else menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_cached_grey);
                        break;
                }
                break;
            case VIEW_TYPE_SUBREDDITS:
                SubredditsViewHolder subredditsViewHolder = (SubredditsViewHolder) viewHolder;
                //NavDrawerSubreddits subreddits = (NavDrawerSubreddits) getItemAt(position);
                if(subredditItemsVisible) subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_action_collapse_grey);
                else subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_action_expand_grey);
                SubredditsListener listener = new SubredditsListener(activity);
                subredditsViewHolder.layoutToggle.setOnClickListener(listener);
                subredditsViewHolder.layoutEdit.setOnClickListener(listener);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                SubredditRowViewHolder subredditRowViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerSubredditItem subredditItem = (NavDrawerSubredditItem) getItemAt(position);
                String subreddit = (subredditItem.getName() != null) ? subredditItem.getName() : "Frontpage";
                subredditRowViewHolder.name.setText(subreddit);
                //Highlight current subreddit
                String currentSubreddit = activity.getListFragment().subreddit;
                if(subreddit.toLowerCase().equals(currentSubreddit) ||
                        (subredditItem.getName() == null && currentSubreddit == null)) {
                    if(MyApplication.nightThemeEnabled) subredditRowViewHolder.name.setTextColor(Color.WHITE);
                    else subredditRowViewHolder.name.setTextColor(MyApplication.colorPrimary);
                    if(MyApplication.nightThemeEnabled) subredditRowViewHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.darker_gray));
                    else subredditRowViewHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.light_gray));
                }
                else {
                    subredditRowViewHolder.name.setTextColor(MyApplication.textColor);
                    //subredditRowViewHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector));
                    if(MyApplication.nightThemeEnabled) subredditRowViewHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector_drawer_dark_theme));
                    else subredditRowViewHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector));
                }
                break;
            case VIEW_TYPE_ACCOUNT:
                SubredditRowViewHolder accountViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerAccount account = (NavDrawerAccount) getItemAt(position);
                accountViewHolder.name.setText(account.getName());
                //if(account == accountItems.get(currentAccountName)) accountViewHolder.name.setTextColor(Color.parseColor("#C2C2FF"));
                //else accountViewHolder.name.setTextColor(Color.WHITE);
                if(currentAccountName.equals(account.getName())) accountViewHolder.name.setTextColor(Color.parseColor("#C2C2FF"));
                else accountViewHolder.name.setTextColor(Color.WHITE);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    private void restartApp() {
        Intent i = activity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("subreddit", activity.getListFragment().subreddit);
        i.putExtra("sort", activity.getListFragment().submissionSort);
        i.putExtra("time", activity.getListFragment().timeSpan);
        activity.finish();
        activity.startActivity(i);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAt(position).getType();
    }

    public static class MenuRowViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;

        public MenuRowViewHolder(View row) {
            super(row);
            image = (ImageView) row.findViewById(R.id.imgView_option);
            name = (TextView) row.findViewById(R.id.txtView_option);
        }
    }

    public static class SubredditRowViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public LinearLayout layout;

        public SubredditRowViewHolder(View row) {
            super(row);
            name = (TextView) row.findViewById(R.id.txtView_subreddit);
            layout = (LinearLayout) row.findViewById(R.id.subredditRowLayout);
        }
    }

    public static class SubredditsViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layoutToggle;
        public LinearLayout layoutEdit;
        public ImageView imgToggle;

        public SubredditsViewHolder(View row) {
            super(row);
            layoutToggle = (LinearLayout) row.findViewById(R.id.layoutToggle);
            layoutEdit = (LinearLayout) row.findViewById(R.id.layoutEdit);
            imgToggle = (ImageView) row.findViewById(R.id.imgView_toggle);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout headerLayout;
        public TextView currentAccount;
        public LinearLayout accountLayout;
        public ImageView toggle;
        public CircleImageView themeSwitch;
        public CircleImageView offlineSwitch;

        public HeaderViewHolder(View row) {
            super(row);
            headerLayout = (RelativeLayout) row.findViewById(R.id.layout_header);
            currentAccount = (TextView) row.findViewById(R.id.txtView_currentAccount);
            accountLayout = (LinearLayout) row.findViewById(R.id.layout_account);
            toggle = (ImageView) row.findViewById(R.id.imgView_toggle);
            themeSwitch = (CircleImageView) row.findViewById(R.id.themeSwitch);
            offlineSwitch = (CircleImageView) row.findViewById(R.id.offlineSwitch);
        }
    }
}
