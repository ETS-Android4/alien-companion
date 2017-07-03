package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.EditFilterProfileActivity;
import com.gDyejeekis.aliencompanion.activities.EditSyncProfileActivity;
import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.models.filters.FilterProfile;
import com.gDyejeekis.aliencompanion.models.Profile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by George on 6/20/2017.
 */

public class ProfileViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView moreButton;
    public Button state;

    public static int moreButtonResource;

    public ProfileViewHolder(View itemView) {
        super(itemView);
        moreButtonResource = (MyApplication.nightThemeEnabled) ? R.drawable.ic_more_vert_white_24dp : R.drawable.ic_more_vert_black_24dp;
        name = (TextView) itemView.findViewById(R.id.textView_profile_name);
        moreButton = (ImageView) itemView.findViewById(R.id.imageView_profile_more);
        state = (Button) itemView.findViewById(R.id.button_state);
    }

    public void bindModel(final ProfilesActivity activity, final Profile profile, final int position) {
        name.setText(profile.getName());
        if(profile instanceof SyncProfile) {
            final SyncProfile syncProfile = (SyncProfile) profile;
            name.setTextColor((syncProfile.isActive()) ? MyApplication.textPrimaryColor : MyApplication.textSecondaryColor);
            moreButton.setImageResource(moreButtonResource);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSyncPopupMenu(activity, view, syncProfile);
                }
            });
            String stateText = (syncProfile.isActive()) ? "ACTIVE" : "INACTIVE";
            state.setText(stateText);
            state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (syncProfile.hasSchedule()) {
                        syncProfile.setActive(!syncProfile.isActive());
                        activity.notifyProfileChanged(position);
                        syncProfile.save(activity, false);
                    } else {
                        ToastUtils.showSnackbarOverToast(activity, syncProfile.getName() + " has no schedule. Add a schedule to activate this profile.");
                    }
                }
            });
        }
        else if(profile instanceof FilterProfile) {
            final FilterProfile filterProfile = (FilterProfile) profile;
            name.setTextColor((filterProfile.isActive()) ? MyApplication.textPrimaryColor : MyApplication.textSecondaryColor);
            moreButton.setImageResource(moreButtonResource);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFilterPopupMenu(activity, view, filterProfile);
                }
            });
            String stateText = (filterProfile.isActive()) ? "ACTIVE" : "INACTIVE";
            state.setText(stateText);
            state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(filterProfile.hasFilters()) {
                        filterProfile.setActive(!filterProfile.isActive());
                        activity.notifyProfileChanged(position);
                        filterProfile.save(activity, false);
                    }
                    else {
                        ToastUtils.showSnackbarOverToast(activity, filterProfile.getName() + " has no filters. Add a filter to activate this profile.");
                    }
                }
            });
        }
    }

    private void showSyncPopupMenu(final ProfilesActivity activity, View view, final SyncProfile profile) {
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

    private void showFilterPopupMenu(final ProfilesActivity activity, View view, final FilterProfile profile) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.inflate(R.menu.menu_filter_profile_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit_profile:
                        Intent intent = new Intent(activity, EditFilterProfileActivity.class);
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
                }
                return false;
            }
        });
        popupMenu.show();
    }

}
