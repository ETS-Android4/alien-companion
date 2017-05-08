package com.gDyejeekis.aliencompanion.views.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.AccountListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.HeaderListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.MenuItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.MultiredditItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.MultisListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.NavDrawerListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.OtherItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.SubredditItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners.SubredditsListener;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.BaseThemesDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerEmptySpace;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMultis;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMutliredditItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerOther;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerOtherItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSubreddits;
import com.gDyejeekis.aliencompanion.models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.enums.MenuType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by George on 6/25/2015.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_HEADER = 0;

    public static final int VIEW_TYPE_MENU_ITEM = 1;

    public static final int VIEW_TYPE_SUBREDDITS = 2;

    public static final int VIEW_TYPE_SUBREDDIT_ITEM = 3;

    public static final int VIEW_TYPE_ACCOUNT = 4;

    public static final int VIEW_TYPE_MULTIS = 5;

    public static final int VIEW_TYPE_MULTIREDDIT_ITEM = 6;

    public static final int VIEW_TYPE_EMPTY_SPACE = 7;

    public static final int VIEW_TYPE_SEPARATOR = 8;

    public static final int VIEW_TYPE_OTHER = 9;

    public static final int VIEW_TYPE_OTHER_ITEM = 10;

    private final MainActivity activity;

    private List<NavDrawerItem> items;

    private boolean accountItemsVisible;
    private boolean subredditItemsVisible;
    private boolean multiredditItemsVisible;

    private List<NavDrawerItem> subredditItems;
    private List<NavDrawerItem> multiredditItems;
    public List<NavDrawerAccount> accountItems;

    public static String currentAccountName;

    private NavDrawerMenuItem profile;
    private NavDrawerMenuItem messages;
    private NavDrawerOtherItem saved;
    private boolean userMenuItemsVisible;

    public NavDrawerAdapter(MainActivity activity) {
        items = new ArrayList<>();
        this.activity = activity;
        subredditItemsVisible = true;
        multiredditItemsVisible = true;
        subredditItems = new ArrayList<>();
        multiredditItems = new ArrayList<>();
        accountItemsVisible = false;
        currentAccountName = "Logged out";
        profile = new NavDrawerMenuItem(MenuType.profile);
        messages = new NavDrawerMenuItem(MenuType.messages);
        saved = new NavDrawerOtherItem("Saved");
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
            int pos = -1;
            int otherIndex = -1;
            for(NavDrawerItem item : items) {
                if(pos == -1 && item instanceof NavDrawerEmptySpace) {
                    pos = items.indexOf(item);
                }
                else if(item instanceof NavDrawerOther) {
                    otherIndex = items.indexOf(item);
                    break;
                }
            }
            add(pos+1, profile);
            add(pos+2, messages);
            add(otherIndex + 3, saved);
            userMenuItemsVisible = true;
        }
    }

    public void hideUserMenuItems() {
        items.remove(profile);
        items.remove(messages);
        items.remove(saved);
        userMenuItemsVisible = false;
    }

    public void updateSubredditItems(List<String> subredditNames) {
        subredditItemsVisible = true;
        subredditItems.clear();
        int index = -1;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) {
                subredditItems.add(item);
            }
            else if(item instanceof NavDrawerSubreddits) {
                index = items.indexOf(item);
            }
        }
        items.removeAll(subredditItems);
        subredditItems.clear();

        subredditItems.add(new NavDrawerSubredditItem());
        for(String name : subredditNames) {
            subredditItems.add(new NavDrawerSubredditItem(name));
        }
        addAll(index + 1, subredditItems);
        notifyDataSetChanged();
    }

    public void updateMultiredditItems(List<String> multiNames) {
        multiredditItemsVisible = true;
        multiredditItems.clear();
        int index = -1;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerMutliredditItem) {
                multiredditItems.add(item);
            }
            else if(item instanceof NavDrawerMultis) {
                index = items.indexOf(item);
            }
        }
        items.removeAll(multiredditItems);
        multiredditItems.clear();

        for(String name : multiNames) {
            multiredditItems.add(new NavDrawerMutliredditItem(name));
        }
        addAll(index + 1, multiredditItems);
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
                CleaningUtils.clearAccountData(activity);
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
        this.items.add(item);
    }

    public void add(int location, NavDrawerItem item) {
        this.items.add(location, item);
    }

    public void addAll(List<NavDrawerItem> items) {
        this.items.addAll(items);
    }

    public void addAll(int position, List<NavDrawerItem> items) {
        this.items.addAll(position, items);
    }

    public void remove(NavDrawerItem item) {
        this.items.remove(item);
    }

    public void removeAll(List<NavDrawerItem> items) {
        this.items.removeAll(items);
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

        notifyItemChanged(0);
        notifyItemRangeInserted(1, accountItems.size());
    }

    public void toggleSubredditItems() {
        if(subredditItemsVisible) collapseSubredditItems();
        else expandSubredditItems();
    }

    private void collapseSubredditItems() {
        subredditItems.clear();
        int i = 0;
        int subredditsIndex = -1;
        int start = -1;
        int end = -1;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubredditItem) {
                if(start == -1) start = i;
                subredditItems.add(item);
                end = i;
            }
            else if(item instanceof NavDrawerSubreddits) subredditsIndex = i;
            i++;
        }
        removeAll(subredditItems);
        subredditItemsVisible = false;

        notifyItemRangeRemoved(start, end);
        notifyItemChanged(subredditsIndex);
    }

    private void expandSubredditItems() {
        int position = -1;
        int i = 0;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerSubreddits) {
                position = i+1;
                break;
            }
            i++;
        }
        addAll(position, subredditItems);
        subredditItemsVisible = true;

        notifyItemRangeInserted(position, subredditItems.size());
        notifyItemChanged(position - 1);
    }

    public void toggleMultiredditItems() {
        if(multiredditItemsVisible) collapseMultiredditItems();
        else expandMultiredditItems();
    }

    private void collapseMultiredditItems() {
        multiredditItems.clear();
        int i = 0;
        int multisIndex = -1;
        int start = -1;
        int end = -1;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerMutliredditItem) {
                if(start == -1) start = i;
                multiredditItems.add(item);
                end = i;
            }
            else if(item instanceof NavDrawerMultis) multisIndex = i;
            i++;
        }
        removeAll(multiredditItems);
        multiredditItemsVisible = false;

        notifyItemRangeRemoved(start, end);
        notifyItemChanged(multisIndex);
    }

    private void expandMultiredditItems() {
        int position = -1;
        int i = 0;
        for(NavDrawerItem item : items) {
            if(item instanceof NavDrawerMultis) {
                position = i+1;
                break;
            }
            i++;
        }
        addAll(position, multiredditItems);
        multiredditItemsVisible = true;

        notifyItemRangeInserted(position, multiredditItems.size());
        notifyItemChanged(position - 1);
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
                viewHolder = new HeaderViewHolder(v);
                break;
            case VIEW_TYPE_MENU_ITEM:
                resource = R.layout.drawer_menu_row;
                listener = new MenuItemListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
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
                v.setOnLongClickListener(listener);
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_ACCOUNT:
                resource = R.layout.drawer_account_row;
                listener = new AccountListener(activity);
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(listener);
                v.setOnLongClickListener(listener);
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_MULTIS:
                resource = R.layout.drawer_multis;
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                viewHolder = new SubredditsViewHolder(v);
                break;
            case VIEW_TYPE_MULTIREDDIT_ITEM:
                resource = R.layout.drawer_subreddit_row;
                listener = new MultiredditItemListener(activity);
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                v.setOnClickListener(listener);
                v.setOnLongClickListener(listener);
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_OTHER:
                viewHolder = new OtherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_other, parent, false));
                break;
            case VIEW_TYPE_OTHER_ITEM:
                resource = R.layout.drawer_subreddit_row;
                listener = new OtherItemListener(activity);
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                v.setOnClickListener(listener);
                v.setOnLongClickListener(listener);
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_EMPTY_SPACE:
                viewHolder = new EmptySpaceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_empty_space, parent, false));
                break;
            case VIEW_TYPE_SEPARATOR:
                viewHolder = new SeparatorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_separator, parent, false));
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

                //String themeButtonText = (MyApplication.nightThemeEnabled) ? "Light theme" : "Dark theme";
                //headerViewHolder.themeButton.setText(themeButtonText);
                //headerViewHolder.themeButton.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View view) {
                //        showThemeSwitchDialog();
                //    }
                //});

                headerViewHolder.themeButton.setText("Base theme");
                headerViewHolder.themeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showThemesDialog();
                    }
                });

                String offlineButtonText = (MyApplication.offlineModeEnabled) ? "Go online" : "Go offline";
                headerViewHolder.offlineButton.setText(offlineButtonText);
                headerViewHolder.offlineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOfflineSwitchDialog();
                    }
                });
                headerViewHolder.offlineButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(MyApplication.longTapSwitchMode) {
                            switchModeGracefully();
                            return true;
                        }
                        return false;
                    }
                });

                if(accountItemsVisible) headerViewHolder.toggle.setImageResource(R.mipmap.ic_arrow_drop_up_white_24dp);
                else headerViewHolder.toggle.setImageResource(R.mipmap.ic_arrow_drop_down_white_24dp);
                break;
            case VIEW_TYPE_MENU_ITEM:
                MenuRowViewHolder menuRowViewHolder= (MenuRowViewHolder) viewHolder;
                NavDrawerMenuItem menuItem = (NavDrawerMenuItem) getItemAt(position);
                menuRowViewHolder.name.setText(menuItem.getMenuType().value());
                switch (menuItem.getMenuType()) {
                    case profile:
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            menuRowViewHolder.image.setImageResource(R.mipmap.ic_account_circle_light_grey_48dp);
                        }
                        else {
                            if (MyApplication.nightThemeEnabled)
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_account_circle_white_48dp);
                            else
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_account_circle_grey_48dp);
                        }
                        break;
                    case messages:
                        if(MyApplication.newMessages) {
                            menuRowViewHolder.image.setImageResource(R.mipmap.ic_mail_orange_48dp);
                        }
                        else {
                            if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_mail_light_grey_48dp);
                            }
                            else {
                                if (MyApplication.nightThemeEnabled)
                                    menuRowViewHolder.image.setImageResource(R.mipmap.ic_mail_white_48dp);
                                else
                                    menuRowViewHolder.image.setImageResource(R.mipmap.ic_mail_grey_48dp);
                            }
                        }
                        break;
                    case user:
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            menuRowViewHolder.image.setImageResource(R.mipmap.ic_person_light_grey_48dp);
                        }
                        else {
                            if (MyApplication.nightThemeEnabled)
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_person_white_48dp);
                            else
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_person_grey_48dp);
                        }
                        break;
                    case subreddit:
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            menuRowViewHolder.image.setImageResource(R.mipmap.ic_subreddit_light_grey_48dp);
                        }
                        else {
                            if (MyApplication.nightThemeEnabled)
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_subreddit_white_48dp);
                            else
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_subreddit_grey_48dp);
                        }
                        break;
                    case settings:
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            menuRowViewHolder.image.setImageResource(R.mipmap.ic_settings_light_grey_48dp);
                        }
                        else {
                            if (MyApplication.nightThemeEnabled)
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_settings_white_48dp);
                            else
                                menuRowViewHolder.image.setImageResource(R.mipmap.ic_settings_grey_48dp);
                        }
                        break;
                    case cached:
                        menuRowViewHolder.image.setImageResource(R.mipmap.ic_action_cached_grey);
                        break;
                }
                break;
            case VIEW_TYPE_SUBREDDITS:
                SubredditsViewHolder subredditsViewHolder = (SubredditsViewHolder) viewHolder;
                if(subredditItemsVisible) subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_expand_less_grey_24dp);
                else subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_expand_more_grey_24dp);
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
                boolean isMulti = activity.getListFragment().isMulti;
                boolean isOther = activity.getListFragment().isOther;
                if ((subreddit.toLowerCase().equals(currentSubreddit) && !isMulti && !isOther) ||
                        (subredditItem.getName() == null && currentSubreddit == null)) {
                    highlightSelectedItem(subredditRowViewHolder);
                }
                else {
                    subredditRowViewHolder.name.setTextColor(MyApplication.textPrimaryColor);
                    setBackgroundSelector(subredditRowViewHolder);
                }
                break;
            case VIEW_TYPE_ACCOUNT:
                SubredditRowViewHolder accountViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerAccount account = (NavDrawerAccount) getItemAt(position);
                accountViewHolder.name.setText(account.getName());
                if(currentAccountName.equals(account.getName())) accountViewHolder.name.setTextColor(Color.parseColor("#C2C2FF"));
                else accountViewHolder.name.setTextColor(Color.WHITE);
                break;
            case VIEW_TYPE_MULTIS:
                subredditsViewHolder = (SubredditsViewHolder) viewHolder;
                if(multiredditItemsVisible) subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_expand_less_grey_24dp);
                else subredditsViewHolder.imgToggle.setImageResource(R.mipmap.ic_expand_more_grey_24dp);
                MultisListener multisListener = new MultisListener(activity);
                subredditsViewHolder.layoutToggle.setOnClickListener(multisListener);
                subredditsViewHolder.layoutEdit.setOnClickListener(multisListener);
                break;
            case VIEW_TYPE_MULTIREDDIT_ITEM:
                subredditRowViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerMutliredditItem multireddit = (NavDrawerMutliredditItem) getItemAt(position);
                subredditRowViewHolder.name.setText(multireddit.getName());

                currentSubreddit = activity.getListFragment().subreddit;
                isMulti = activity.getListFragment().isMulti;
                if (multireddit.getName().toLowerCase().equals(currentSubreddit) && isMulti) {
                    highlightSelectedItem(subredditRowViewHolder);
                }
                else {
                    subredditRowViewHolder.name.setTextColor(MyApplication.textPrimaryColor);
                    setBackgroundSelector(subredditRowViewHolder);
                }
                break;
            case VIEW_TYPE_OTHER_ITEM:
                subredditRowViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerOtherItem otherItem = (NavDrawerOtherItem) getItemAt(position);
                subredditRowViewHolder.name.setText(otherItem.getName());

                currentSubreddit = activity.getListFragment().subreddit;
                isOther = activity.getListFragment().isOther;
                if (otherItem.getName().toLowerCase().equals(currentSubreddit) && isOther) {
                    highlightSelectedItem(subredditRowViewHolder);
                }
                else {
                    subredditRowViewHolder.name.setTextColor(MyApplication.textPrimaryColor);
                    setBackgroundSelector(subredditRowViewHolder);
                }
                break;
            case VIEW_TYPE_OTHER:
            case VIEW_TYPE_EMPTY_SPACE:
            case VIEW_TYPE_SEPARATOR:
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    private void setBackgroundSelector(SubredditRowViewHolder vHolder) {
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                vHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector));
                break;
            case MyApplication.MATERIAL_BLUE_THEME:
                vHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector_material_blue));
                break;
            case MyApplication.MATERIAL_GREY_THEME:
                vHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector_material_grey));
                break;
            case MyApplication.DARK_THEME:
                vHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector_drawer_dark_theme));
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                vHolder.layout.setBackground(activity.getResources().getDrawable(R.drawable.touch_selector_drawer_dark_theme));
                break;
        }
    }

    private void highlightSelectedItem(SubredditRowViewHolder vHolder) {
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                vHolder.name.setTextColor(MyApplication.colorPrimary);
                vHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.lightDrawerItemSelected));
                break;
            case MyApplication.MATERIAL_BLUE_THEME:
                vHolder.name.setTextColor(MyApplication.colorPrimary);
                vHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.materialBlueDrawerItemSelected));
                break;
            case MyApplication.MATERIAL_GREY_THEME:
                vHolder.name.setTextColor(MyApplication.colorPrimary);
                vHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.materialGreyDrawerItemSelected));
                break;
            case MyApplication.DARK_THEME:
                vHolder.name.setTextColor(MyApplication.colorInDarkThemes ? MyApplication.colorPrimary : MyApplication.textPrimaryColor);
                vHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.darkDrawerItemSelected));
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                vHolder.name.setTextColor(MyApplication.colorInDarkThemes ? MyApplication.colorPrimary : MyApplication.textPrimaryColor);
                vHolder.layout.setBackgroundColor(activity.getResources().getColor(R.color.darkDrawerItemSelected));
                break;
        }
    }

    public void restartApp(String subreddit, boolean isMulti, boolean isOther, SubmissionSort sort, TimeSpan timeSpan) {
        //Intent i = activity.getBaseContext().getPackageManager()
        //        .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(!MyApplication.offlineModeEnabled && isOther && subreddit!=null && subreddit.equals("synced")) {
            subreddit = null;
            isOther = false;
        }
        Intent i = activity.getIntent();
        i.putExtra("subreddit", subreddit);
        i.putExtra("isMulti", isMulti);
        i.putExtra("isOther", isOther);
        i.putExtra("sort", sort);
        i.putExtra("time", timeSpan);
        activity.finish();
        activity.startActivity(i);
    }

    public void restartApp() {
        PostListFragment fragment = activity.getListFragment();
        restartApp(fragment.subreddit, fragment.isMulti, fragment.isOther, fragment.submissionSort, fragment.timeSpan);
    }

    private void showThemesDialog() {
        BaseThemesDialogFragment dialogFragment = new BaseThemesDialogFragment();
        dialogFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void showOfflineSwitchDialog() {
        String text = (MyApplication.offlineModeEnabled) ? "Switch to online mode?" : "Switch to offline mode?";
        //text += " (App will restart)";
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchMode();
            }
        };
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyAlertDialogStyle)).setMessage(text).setPositiveButton("Yes", listener)
                .setNegativeButton("No", null).show();
    }

    private void switchMode() {
        PostListFragment fragment = activity.getListFragment();
        switchMode(fragment.subreddit, fragment.isMulti, fragment.isOther, fragment.submissionSort, fragment.timeSpan);
    }

    public void switchMode(String subreddit, boolean isMulti, boolean isOther, SubmissionSort sort, TimeSpan timeSpan) {
        MyApplication.offlineModeEnabled = !MyApplication.offlineModeEnabled;
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putBoolean("offlineMode", MyApplication.offlineModeEnabled);
        editor.apply();
        restartApp(subreddit, isMulti, isOther, sort, timeSpan);
    }

    public void switchModeGracefully() {
        activity.getDrawerLayout().closeDrawers();
        ToastUtils.showToast(activity, "Switching to " + ((MyApplication.offlineModeEnabled) ? "online" : "offline") + " mode");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchMode();
            }
        }, MyApplication.NAV_DRAWER_CLOSE_TIME);
    }

    public void switchModeGracefully(final String reddit, final boolean isMulti) {
        activity.getDrawerLayout().closeDrawers();
        ToastUtils.showToast(activity, "Switching to " + ((MyApplication.offlineModeEnabled) ? "online" : "offline") + " mode");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchMode(reddit, isMulti, false, SubmissionSort.HOT, null);
            }
        }, MyApplication.NAV_DRAWER_CLOSE_TIME);
    }

    public void showOfflineSwitchDialog(final String subreddit, final boolean isMulti, final boolean isOther, final SubmissionSort sort, final TimeSpan timeSpan) {
        String text = (MyApplication.offlineModeEnabled) ? "Switch to online mode?" : "Switch to offline mode?";
        //text += " (App will restart)";
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchMode(subreddit, isMulti, isOther, sort, timeSpan);
            }
        };
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyAlertDialogStyle)).setMessage(text).setPositiveButton("Yes", listener)
                .setNegativeButton("No", null).show();
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
        public LinearLayout headerLayout;
        public TextView currentAccount;
        public LinearLayout accountLayout;
        public ImageView toggle;
        public Button themeButton;
        public Button offlineButton;

        public HeaderViewHolder(View row) {
            super(row);
            headerLayout = (LinearLayout) row.findViewById(R.id.layout_header);
            currentAccount = (TextView) row.findViewById(R.id.txtView_currentAccount);
            accountLayout = (LinearLayout) row.findViewById(R.id.layout_account);
            toggle = (ImageView) row.findViewById(R.id.imgView_toggle);
            themeButton = (Button) row.findViewById(R.id.button_theme_switch);
            offlineButton = (Button) row.findViewById(R.id.button_offline_switch);
        }
    }

    public static class OtherViewHolder extends RecyclerView.ViewHolder {
        public OtherViewHolder(View view) {
            super(view);
        }
    }

    public static class EmptySpaceViewHolder extends RecyclerView.ViewHolder {
        public EmptySpaceViewHolder(View convertView) {
            super(convertView);
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        public SeparatorViewHolder(View convertView) {
            super(convertView);
        }
    }
}
