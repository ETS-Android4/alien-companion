package com.gDyejeekis.aliencompanion.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.models.Profile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.views.viewholders.ProfileViewHolder;

import java.io.File;
import java.util.List;

/**
 * Created by sound on 2/5/2016.
 */
public class ProfileListAdapter extends RecyclerView.Adapter {

    private ProfilesActivity activity;

    private List<Profile> profiles;

    public static final int VIEW_TYPE_PROFILE_ITEM = 0;

    public static final int PROFILE_LAYOUT_RESOURCE = R.layout.profile_list_item;

    public ProfileListAdapter(ProfilesActivity activity, List<Profile> profiles) {
        this.activity = activity;
        this.profiles = profiles;
    }

    public void removeProfile(Profile profile) {
        int index = profiles.indexOf(profile);
        profiles.remove(profile);
        notifyItemRemoved(index);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_PROFILE_ITEM:
                v = LayoutInflater.from(parent.getContext()).inflate(PROFILE_LAYOUT_RESOURCE, parent, false);
                viewHolder = new ProfileViewHolder(v);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemAt(position).getViewType()) {
            case VIEW_TYPE_PROFILE_ITEM:
                ProfileViewHolder spv = (ProfileViewHolder) viewHolder;
                Profile profile = getItemAt(position);
                spv.bindModel(activity, profile, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(profiles!=null) {
            return profiles.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAt(position).getViewType();
    }

    public Profile getItemAt(int position) {
        if(profiles!=null && !profiles.isEmpty()) {
            return profiles.get(position);
        }
        return null;
    }

}
