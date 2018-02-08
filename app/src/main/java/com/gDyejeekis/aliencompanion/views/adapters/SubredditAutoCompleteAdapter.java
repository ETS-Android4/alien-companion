package com.gDyejeekis.aliencompanion.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.api.retrieval.Subreddits;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by George on 2/8/2018.
 */

public class SubredditAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 15;
    private Context mContext;
    private Subreddits subredditRetrieval;
    private List<Subreddit> resultList = new ArrayList<>();

    public SubredditAutoCompleteAdapter(Context context) {
        mContext = context;
        subredditRetrieval = new Subreddits(new RedditHttpClient(), MyApplication.currentUser);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Subreddit getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        Subreddit subreddit = getItem(position);
        ((TextView) convertView.findViewById(R.id.text1)).setText(subreddit.getDisplayName());
        ((TextView) convertView.findViewById(R.id.text2)).setText(String.valueOf(subreddit.getSubscribers()) + " subscribers");
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Subreddit> subreddits = getResultList(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = subreddits;
                    filterResults.count = subreddits.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Subreddit>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private List<Subreddit> getResultList(String query) {
        List<Subreddit> subreddits = subredditRetrieval.autocomplete(MyApplication.showNSFWsuggestions, false, query);
        // remove any leftover nsfw subreddits if nsfw suggestions are disabled
        if (!MyApplication.showNSFWsuggestions) {
            for (Subreddit subreddit : subreddits) {
                if (subreddit.getDisplayName().toLowerCase().contains("nsfw"))
                    subreddits.remove(subreddit);
            }
        }
        // sort list by subs
        Collections.sort(subreddits, new Comparator<Subreddit>() {
            @Override
            public int compare(Subreddit s1, Subreddit s2) {
                if (s1.getSubscribers() == s2.getSubscribers())
                    return 0;
                return s1.getSubscribers() > s2.getSubscribers() ? -1 : 1;
            }
        });
        return subreddits;
    }

}
