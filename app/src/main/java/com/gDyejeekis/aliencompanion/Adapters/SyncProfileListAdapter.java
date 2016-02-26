package com.gDyejeekis.aliencompanion.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SyncProfileScheduleDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SyncProfileSubredditsDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 2/5/2016.
 */
public class SyncProfileListAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {

    public class TempProfile extends SyncProfile {

        public TempProfile() {
            super();
        }

        public TempProfile(SyncProfile profile) {
            super(profile);
        }

        public int getViewType() {
            return VIEW_TYPE_TEMP_PROFILE;
        }
    }

    SyncProfilesActivity activity;

    List<SyncProfile> profiles;

    public static final int VIEW_TYPE_PROFILE_ITEM = 0;

    public static final int VIEW_TYPE_TEMP_PROFILE = 1;

    public static final int PROFILE_LAYOUT_RESOURCE = R.layout.sync_profile_list_item;

    public static final int TEMP_PROFILE_LAYOUT_RESOURCE = R.layout.sync_profile_temp_list_item;

    public boolean addingNewProfile = false;

    public int renamingProfilePosition = -1;

    public SyncProfileListAdapter(SyncProfilesActivity activity) {
        this.activity = activity;
        this.profiles = new ArrayList<>();
        loadSyncProfiles();
    }

    public SyncProfileListAdapter(SyncProfilesActivity activity, List<SyncProfile> profiles) {
        this.activity = activity;
        this.profiles = profiles;
    }

    private void loadSyncProfiles() {
        List<SyncProfile> savedProfiles = null;
        try {
            FileInputStream fis = activity.openFileInput(MyApplication.SYNC_PROFILES_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            savedProfiles = (List<SyncProfile>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(savedProfiles != null) {
            this.profiles = savedProfiles;
        }
    }

    public void saveSyncProfiles() {
        Log.d("geotest", "Saving sync profiles");
        try {
            FileOutputStream fos = activity.openFileOutput(MyApplication.SYNC_PROFILES_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            List<SyncProfile> toSave = new ArrayList<>();
            for(SyncProfile profile : profiles) {
                if(profile.getViewType() == VIEW_TYPE_PROFILE_ITEM) {
                    toSave.add(profile);
                    if(profile.isActive()) {
                        profile.scheduleSync(activity);
                    }
                    else {
                        profile.unscheduleSync(activity);
                    }
                }
            }
            os.writeObject(toSave);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewProfile(SyncProfile profile) {
        try {
            addingNewProfile = false;
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            profiles.remove(profiles.size() - 1);
            profiles.add(profile);
            notifyDataSetChanged();
            showSubredditsDialog(profiles.indexOf(profile), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newProfile() {
        if(!addingNewProfile) {
            addingNewProfile = true;
            profiles.add(new TempProfile());
            notifyDataSetChanged();
        }
    }

    public void removeTempProfile() {
        addingNewProfile = false;
        int index = profiles.size() -1;
        profiles.remove(index);
        notifyItemRemoved(index);
    }

    private void renameProfileAt(int position) {
        profiles.set(position, new TempProfile(getItemAt(position)));
        notifyItemChanged(position);
    }

    public void profileRenamedAt(int position, SyncProfile profile, boolean toggleKeyboard) {
        if(toggleKeyboard) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        profiles.set(position, profile);
        notifyItemChanged(position);
        renamingProfilePosition = -1;
    }

    private void deleteProfileAt(int position) {
        try {
            profiles.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void deleteProfile(SyncProfile profile) {
        try {
            int index = profiles.indexOf(profile);
            profiles.remove(index);
            notifyItemRemoved(index);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void showSubredditsDialog(int profilePosition, boolean showSchedule) {
        SyncProfileSubredditsDialogFragment dialog = new SyncProfileSubredditsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("profile", getItemAt(profilePosition));
        bundle.putBoolean("showSchedule", showSchedule);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "dialog");
    }

    private void showScheduleDialog(int profilePosition) {
        SyncProfileScheduleDialogFragment dialog = new SyncProfileScheduleDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("profile", getItemAt(profilePosition));
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "dialog");
    }

    public void showScheduleDialog(SyncProfile profile) {
        showScheduleDialog(profiles.indexOf(profile));
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
            case VIEW_TYPE_TEMP_PROFILE:
                v = LayoutInflater.from(parent.getContext()).inflate(TEMP_PROFILE_LAYOUT_RESOURCE, parent, false);
                viewHolder = new TempProfileViewHolder(v);
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
            case VIEW_TYPE_TEMP_PROFILE:
                TempProfileViewHolder tpv = (TempProfileViewHolder) viewHolder;
                tpv.bindModel(activity, getItemAt(position), position);
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
        return profiles.get(position);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
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
                    showPopupMenu(activity, view, profile, position);
                }
            });
            String stateText = (profile.isActive()) ? "ENABLED" : "DISABLED";
            state.setText(stateText);
            state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    profile.setActive(!profile.isActive());
                    activity.getAdapter().notifyItemChanged(position);
                }
            });
        }

        private void showPopupMenu(final SyncProfilesActivity activity, View view, final SyncProfile profile, final int position) {
            PopupMenu popupMenu = new PopupMenu(activity, view);
            popupMenu.inflate(R.menu.menu_sync_profile_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_edi_subreddits:
                            activity.getAdapter().showSubredditsDialog(position, false);
                            return true;
                        case R.id.edit_schedule:
                            activity.getAdapter().showScheduleDialog(position);
                            return true;
                        case R.id.action_rename_profile:
                            activity.getAdapter().renamingProfilePosition = position;
                            activity.getAdapter().renameProfileAt(position);
                            return true;
                        case R.id.action_delete_profile:
                            //activity.getAdapter().deleteProfileAt(position);
                            activity.getAdapter().deleteProfile(profile);
                            return true;
                        case R.id.action_sync_now:
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

    public static class TempProfileViewHolder extends RecyclerView.ViewHolder {

        public EditText nameField;

        public TempProfileViewHolder(View itemView) {
            super(itemView);
            nameField = (EditText) itemView.findViewById(R.id.editText_profile_name_temp);
        }

        public void bindModel(final SyncProfilesActivity activity, final SyncProfile profile, final int position) {
            nameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    String profileName = textView.getText().toString();
                    if(profile.getName().length()==0) {
                        activity.getAdapter().addNewProfile(new SyncProfile(profileName));
                    }
                    else {
                        profile.setName(profileName);
                        SyncProfile renamedProfile = new SyncProfile(profile);
                        activity.getAdapter().profileRenamedAt(position, renamedProfile, true);
                    }
                    return true;
                }
            });
            if(profile.getName().length()==0) {
                nameField.setText("Profile " + activity.getAdapter().getItemCount());
            }
            else {
                nameField.setText(profile.getName());
            }
            nameField.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

}
