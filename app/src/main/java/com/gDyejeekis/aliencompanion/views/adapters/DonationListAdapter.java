package com.gDyejeekis.aliencompanion.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.models.Donation;

import java.util.List;

/**
 * Created by George on 1/27/2018.
 */

public class DonationListAdapter extends ArrayAdapter {

    public static final int LAYOUT_RESOURCE = R.layout.donation_list_item;

    public DonationListAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, LAYOUT_RESOURCE, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Context context = getContext();
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(LAYOUT_RESOURCE, null);
        }
        TextView name = view.findViewById(R.id.textView_donation_name);
        TextView amount = view.findViewById(R.id.textView_donation_amount);
        TextView message = view.findViewById(R.id.textView_donation_message);
        final Donation donation = (Donation) getItem(position);
        name.setText(donation.getName());
        amount.setText("$" + String.valueOf(donation.getAmount()));
        if (donation.hasMessage()) {
            message.setVisibility(View.VISIBLE);
            message.setText(donation.getMessage());
        } else {
            message.setVisibility(View.GONE);
        }

        return view;
    }
}
