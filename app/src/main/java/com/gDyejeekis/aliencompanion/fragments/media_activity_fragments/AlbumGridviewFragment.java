package com.gDyejeekis.aliencompanion.fragments.media_activity_fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gDyejeekis.aliencompanion.activities.MediaActivity;
import com.gDyejeekis.aliencompanion.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sound on 3/8/2016.
 */
public class AlbumGridviewFragment extends Fragment {

    private MediaActivity activity;

    private ArrayList<String> urls;

    public static AlbumGridviewFragment newInstance(ArrayList<String> urls) {
        AlbumGridviewFragment fragment = new AlbumGridviewFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("urls", urls);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = (MediaActivity) getActivity();
        urls = getArguments().getStringArrayList("urls");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(activity, R.layout.album_gridview_item, urls));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                activity.setViewPagerPosition(position);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, activity.getOriginalUrl());
            intent.setType("text/plain");
            activity.startActivity(Intent.createChooser(intent, "Share album to.."));
        }
        return super.onOptionsItemSelected(item);
    }

    static class ImageAdapter extends ArrayAdapter<String> {

        private Context mContext;

        private int layoutResourceId;

        private ArrayList<String> urls;

        public ImageAdapter(Context c, int resource, ArrayList<String> urls) {
            super(c, resource, urls);
            mContext = c;
            this.layoutResourceId = resource;
            this.urls = urls;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            Picasso.with(mContext).load(urls.get(position)).into(holder.imageView);
            return row;
        }

        static class ViewHolder {
            ImageView imageView;
        }

    }

}
