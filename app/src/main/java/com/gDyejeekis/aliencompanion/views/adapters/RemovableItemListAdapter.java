package com.gDyejeekis.aliencompanion.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.EditFilterProfileActivity;
import com.gDyejeekis.aliencompanion.activities.EditSyncProfileActivity;

import java.util.List;

/**
 * Created by George on 6/30/2017.
 */

public class RemovableItemListAdapter extends ArrayAdapter {

    public static final int LAYOUT_RESOURCE = R.layout.simple_list_item_profile;

    public static final int SUBREDDITS = 0;
    public static final int MULTIREDDITS = 1;
    public static final int SYNC_SCHEDULES = 2;
    public static final int FILTERS = 3;
    public static final int SUBREDDIT_RESTRICTIONS = 4;
    public static final int MULTIREDDIT_RESTRCTIONS = 5;

    private int itemType;

    public RemovableItemListAdapter(@NonNull Context context, @NonNull List<String> items, int itemType) {
        super(context, LAYOUT_RESOURCE, items);
        this.itemType = itemType;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Context context = getContext();
        View view = convertView;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_profile, null);
        }

        TextView tv = (TextView) view.findViewById(R.id.textView_item_text);
        tv.setText((String) getItem(position));

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_item_remove);
        View.OnClickListener listener = null;
        if(context instanceof EditSyncProfileActivity) {
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (itemType) {
                        case SUBREDDITS:
                            ((EditSyncProfileActivity) context).removeSubreddit(position);
                            break;
                        case MULTIREDDITS:
                            ((EditSyncProfileActivity) context).removeMultireddit(position);
                            break;
                        case SYNC_SCHEDULES:
                            ((EditSyncProfileActivity) context).removeSchedule(position);
                            break;
                    }
                }
            };
        }
        else if(context instanceof EditFilterProfileActivity) {
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (itemType) {
                        case FILTERS:
                            ((EditFilterProfileActivity) context).removeFilter(position);
                            break;
                        case SUBREDDIT_RESTRICTIONS:
                            ((EditFilterProfileActivity) context).removeSubRestriction(position);
                            break;
                        case MULTIREDDIT_RESTRCTIONS:
                            ((EditFilterProfileActivity) context).removeMultiRestriction(position);
                            break;
                    }
                }
            };
        }
        imageView.setOnClickListener(listener);

        return view;
    }

}
