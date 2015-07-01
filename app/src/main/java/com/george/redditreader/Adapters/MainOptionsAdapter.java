package com.george.redditreader.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.george.redditreader.Models.OptionItem;
import com.george.redditreader.R;

import java.util.List;

/**
 * Created by George on 6/5/2015.
 */
public class MainOptionsAdapter extends ArrayAdapter<OptionItem> {

    private List<OptionItem> optionItems;
    private LayoutInflater inflater;

    public MainOptionsAdapter(Activity activity, List<OptionItem> optionItems) {
        super(activity, R.layout.options_main, optionItems);
        this.optionItems = optionItems;
        inflater = activity.getWindow().getLayoutInflater();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.options_main, parent, false);

        //Set Option Title
        TextView textView = (TextView) view.findViewById(R.id.txtView_title);
        textView.setText(optionItems.get(position).getTitle());
        //Set Option Subtitle
        textView = (TextView) view.findViewById(R.id.txtView_subtitle);
        textView.setText(optionItems.get(position).getSubtitle());

        return view;
    }
}
