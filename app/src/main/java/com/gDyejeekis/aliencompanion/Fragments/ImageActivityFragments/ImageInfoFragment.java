package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.MediaActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 9/20/2016.
 */

public class ImageInfoFragment extends Fragment {

    public static final String TAG = "ImageInfoFragment";

    public static ImageInfoFragment newInstance(String title, String description) {
        ImageInfoFragment fragment = new ImageInfoFragment();

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("descr", description);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_info, container, false);

        if(MyApplication.dismissInfoOnTap) {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_image_info);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MediaActivity) getActivity()).removeInfoFragment();
                }
            });
        }

        TextView titleTextView = (TextView) view.findViewById(R.id.textView_image_title);
        String title = getArguments().getString("title");
        if(title==null || title.isEmpty()) {
            titleTextView.setVisibility(View.GONE);
        }
        else {
            titleTextView.setText(title);
        }
        TextView descrTextView = (TextView) view.findViewById(R.id.textView_image_descr);
        String descr = getArguments().getString("descr");
        if(descr==null || descr.isEmpty()) {
            descrTextView.setVisibility(View.GONE);
        }
        else {
            descrTextView.setText(descr);
        }

        return view;
    }
}
