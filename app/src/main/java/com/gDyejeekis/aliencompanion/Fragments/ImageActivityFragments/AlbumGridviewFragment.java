package com.gDyejeekis.aliencompanion.Fragments.ImageActivityFragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gDyejeekis.aliencompanion.Activities.ImageActivity;
import com.gDyejeekis.aliencompanion.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sound on 3/8/2016.
 */
public class AlbumGridviewFragment extends Fragment {

    private ImageActivity activity;

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

        activity = (ImageActivity) getActivity();
        urls = getArguments().getStringArrayList("urls");
    }

    //@Override
    //public void onResume() {
    //    super.onResume();
    //    activity.setMainProgressBarVisible(false);
    //}

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

    //static class ImageAdapter extends BaseAdapter {
//
    //    private Context mContext;
//
    //    private ArrayList<String> urls;
//
    //    public ImageAdapter(Context c, ArrayList<String> urls) {
    //        mContext = c;
    //        this.urls = urls;
    //    }
//
    //    public int getCount() {
    //        return urls.size();
    //    }
//
    //    public Object getItem(int position) {
    //        return null;
    //    }
//
    //    public long getItemId(int position) {
    //        return 0;
    //    }
//
    //    // create a new ImageView for each item referenced by the Adapter
    //    public View getView(int position, View convertView, ViewGroup parent) {
    //        ImageView imageView;
    //        if (convertView == null) {
    //            // if it's not recycled, initialize some attributes
    //            imageView = new ImageView(mContext);
    //            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    //            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    //            //imageView.setPadding(8, 8, 8, 8);
    //        } else {
    //            imageView = (ImageView) convertView;
    //        }
//
    //        //imageView.setImageResource(mThumbIds[position]);
    //        Picasso.with(mContext).load(urls.get(position)).placeholder(R.color.darker_gray).into(imageView);
    //        return imageView;
    //    }
    //}

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
