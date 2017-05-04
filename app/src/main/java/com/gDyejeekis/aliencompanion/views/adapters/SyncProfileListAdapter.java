package com.gDyejeekis.aliencompanion.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.EditSyncProfileActivity;
import com.gDyejeekis.aliencompanion.activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sound on 2/5/2016.
 */
public class SyncProfileListAdapter extends RecyclerView.Adapter {

    private SyncProfilesActivity activity;

    private List<SyncProfile> profiles;

    public static final int VIEW_TYPE_PROFILE_ITEM = 0;

    public static final int PROFILE_LAYOUT_RESOURCE = R.layout.sync_profile_list_item;

    public SyncProfileListAdapter(SyncProfilesActivity activity) {
        this.activity = activity;
        this.profiles = new ArrayList<>();
        loadSyncProfilesFromDisk();
    }

    public SyncProfileListAdapter(SyncProfilesActivity activity, List<SyncProfile> profiles) {
        this.activity = activity;
        this.profiles = profiles;
    }

    private void loadSyncProfilesFromDisk() {
        List<SyncProfile> savedProfiles = null;
        try {
            savedProfiles = (List<SyncProfile>) GeneralUtils.readObjectFromFile(new File(activity.getFilesDir(), MyApplication.SYNC_PROFILES_FILENAME));
        } catch (Exception e) {}
        if(savedProfiles != null) {
            this.profiles = savedProfiles;
        }
    }

    public void removeProfile(SyncProfile profile) {
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
                viewHolder = new SyncProfileViewHolder(v);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        switch (getItemAt(position).getViewType()) {
            case VIEW_TYPE_PROFILE_ITEM:
                SyncProfileViewHolder spv = (SyncProfileViewHolder) viewHolder;
                final SyncProfile profile = getItemAt(position);
                spv.bindModel(activity, profile, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAt(position).getViewType();
    }

    public SyncProfile getItemAt(int position) {
        if(profiles!=null && !profiles.isEmpty()) {
            return profiles.get(position);
        }
        return null;
    }

    public static class SyncProfileViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageView moreButton;
        public Button state;

        public static int moreButtonResource;

        public SyncProfileViewHolder(View itemView) {
            super(itemView);
            moreButtonResource = (MyApplication.nightThemeEnabled) ? R.mipmap.ic_more_vert_white_24dp : R.mipmap.ic_more_vert_black_24dp;
            name = (TextView) itemView.findViewById(R.id.textView_profile_name);
            moreButton = (ImageView) itemView.findViewById(R.id.imageView_profile_more);
            state = (Button) itemView.findViewById(R.id.button_state);
        }

        public void bindModel(final SyncProfilesActivity activity, final SyncProfile profile, final int position) {
            name.setText(profile.getName());
            name.setTextColor((profile.isActive()) ? MyApplication.textColor : MyApplication.textHintColor);
            moreButton.setImageResource(moreButtonResource);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(activity, view, profile);
                }
            });
            String stateText = (profile.isActive()) ? "ACTIVE" : "INACTIVE";
            state.setText(stateText);
            state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(profile.hasSchedule()) {
                        profile.setActive(!profile.isActive());
                        activity.notifyProfileChanged(position);
                        profile.saveChanges(activity, false);
                    }
                    else {
                        ToastUtils.showSnackbarOverToast(activity, profile.getName() + " has no schedule. Add a schedule to activate this profile.");
                    }
                }
            });
        }

        private void showPopupMenu(final SyncProfilesActivity activity, View view, final SyncProfile profile) {
            PopupMenu popupMenu = new PopupMenu(activity, view);
            popupMenu.inflate(R.menu.menu_sync_profile_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_edit_profile:
                            Intent intent = new Intent(activity, EditSyncProfileActivity.class);
                            intent.putExtra("profile", profile);
                            activity.startActivity(intent);
                            return true;
                        case R.id.action_delete_profile:
                            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(profile.delete(activity)) {
                                        activity.removeProfile(profile);
                                    }
                                    else {
                                        ToastUtils.showSnackbarOverToast(activity, "Error deleting profile");
                                    }
                                }
                            };
                            new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyAlertDialogStyle)).setMessage("Delete " + profile.getName() + "?").setPositiveButton("Yes", listener)
                                    .setNegativeButton("No", null).show();
                            return true;
                        case R.id.action_sync_now:
                            ToastUtils.showToast(activity, profile.getName() + " added to sync queue");
                            profile.startSync(activity);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }
    }

}
