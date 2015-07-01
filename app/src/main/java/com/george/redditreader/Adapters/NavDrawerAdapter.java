package com.george.redditreader.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.ClickListeners.NavDrawerListeners.AccountListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.HeaderListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.MenuItemListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.SubredditItemListener;
import com.george.redditreader.ClickListeners.NavDrawerListeners.SubredditsListener;
import com.george.redditreader.Models.NavDrawer.NavDrawerAccount;
import com.george.redditreader.Models.NavDrawer.NavDrawerHeader;
import com.george.redditreader.Models.NavDrawer.NavDrawerItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerMenuItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubredditItem;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubreddits;
import com.george.redditreader.R;

import java.util.ArrayList;
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

    public static final int VIEW_TYPE_SEPARATOR = 5;

    private final MainActivity activity;

    //private View.OnClickListener listener;

    private List<NavDrawerItem> items;

    public NavDrawerAdapter(MainActivity activity) {
        items = new ArrayList<>();
        this.activity = activity;
        //this.listener = listener;
    }

    public void add(NavDrawerItem item) {
        items.add(item);
    }

    public void addAll(List<NavDrawerItem> items) {
        this.items.addAll(items);
    }

    public NavDrawerItem getItemAt(int position) {
        return items.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                int resource = R.layout.drawer_header;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(new HeaderListener(activity));
                viewHolder = new HeaderViewHolder(v);
                break;
            case VIEW_TYPE_MENU_ITEM:
                resource = R.layout.drawer_menu_row;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(new MenuItemListener(activity));
                viewHolder = new MenuRowViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDITS:
                resource = R.layout.drawer_subreddits;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(new SubredditsListener(activity));
                viewHolder = new SubredditsViewHolder(v);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                resource = R.layout.drawer_subreddit_row;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(new SubredditItemListener(activity));
                viewHolder = new SubredditRowViewHolder(v);
                break;
            case VIEW_TYPE_ACCOUNT:
                resource = R.layout.drawer_subreddit_row;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                v.setOnClickListener(new AccountListener(activity));
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
                NavDrawerHeader header = (NavDrawerHeader) getItemAt(position);
                break;
            case VIEW_TYPE_MENU_ITEM:
                MenuRowViewHolder menuRowViewHolder= (MenuRowViewHolder) viewHolder;
                NavDrawerMenuItem menuItem = (NavDrawerMenuItem) getItemAt(position);
                menuRowViewHolder.name.setText(menuItem.getMenuType().value());
                break;
            case VIEW_TYPE_SUBREDDITS:
                SubredditsViewHolder subredditsViewHolder = (SubredditsViewHolder) viewHolder;
                NavDrawerSubreddits subreddits = (NavDrawerSubreddits) getItemAt(position);
                break;
            case VIEW_TYPE_SUBREDDIT_ITEM:
                SubredditRowViewHolder subredditRowViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerSubredditItem subredditItem = (NavDrawerSubredditItem) getItemAt(position);
                String subreddit = (subredditItem.getName() != null) ? subredditItem.getName() : "Frontpage";
                subredditRowViewHolder.name.setText(subreddit);
                break;
            case VIEW_TYPE_ACCOUNT:
                SubredditRowViewHolder accountViewHolder = (SubredditRowViewHolder) viewHolder;
                NavDrawerAccount account = (NavDrawerAccount) getItemAt(position);
                accountViewHolder.name.setText(account.getName());
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
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

            public SubredditRowViewHolder(View row) {
                super(row);
                name = (TextView) row.findViewById(R.id.txtView_subreddit);
            }
    }

    public static class SubredditsViewHolder extends RecyclerView.ViewHolder {
        public TextView subreddits;
        public TextView edit;

        public SubredditsViewHolder(View row) {
            super(row);
            subreddits = (TextView) row.findViewById(R.id.txtView_subreddits);
            edit = (TextView) row.findViewById(R.id.txtView_edit);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView currentAccount;
        public CircleImageView circleImageView;
        public LinearLayout accountLayout;

        public HeaderViewHolder(View row) {
            super(row);
            currentAccount = (TextView) row.findViewById(R.id.txtView_currentAccount);
            circleImageView = (CircleImageView) row.findViewById(R.id.circleView);
            accountLayout = (LinearLayout) row.findViewById(R.id.layout_account);
        }
    }
}
