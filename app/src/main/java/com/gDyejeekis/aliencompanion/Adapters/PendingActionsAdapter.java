package com.gDyejeekis.aliencompanion.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingActionsAdapter extends RecyclerView.Adapter {

    //public static final int VIEW_TYPE_PENDING_ACTION = 0;

    private PendingUserActionsActivity activity;

    private List<OfflineUserAction> pendingActions;

    public PendingActionsAdapter(PendingUserActionsActivity activity) {
        this.activity = activity;
        try {
            pendingActions = (List<OfflineUserAction>) GeneralUtils.readObjectFromFile(new File(activity.getFilesDir(), MyApplication.OFFLINE_USER_ACTIONS_FILENAME));
        } catch (Exception e) {
            pendingActions = new ArrayList<>();
        }
    }

    public boolean executeAction(int position) {
        // TODO: 4/24/2016
        return false;
    }

    public boolean executeAction(OfflineUserAction action) {
        return executeAction(pendingActions.indexOf(action));
    }

    public void editAction(int position) {
        // TODO: 4/24/2016
    }

    public void editAction(OfflineUserAction action) {
        editAction(pendingActions.indexOf(action));
    }

    public void cancelAction(int position) {
        // TODO: 4/24/2016
    }

    public void cancelAction(OfflineUserAction action) {
        cancelAction(pendingActions.indexOf(action));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_action_item, parent, false);

        return new PendingActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        OfflineUserAction pendingAction = pendingActions.get(position);

        ((PendingActionViewHolder) viewHolder).bindModel(activity, pendingAction);
    }

    @Override
    public int getItemCount() {
        return pendingActions.size();
    }

    public static class PendingActionViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView status;
        ImageView options;

        public PendingActionViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.textView_action_descr);
            status = (TextView) itemView.findViewById(R.id.textView_action_status);
            options = (ImageView) itemView.findViewById(R.id.imageView_action_options);
        }

        public void bindModel(final Context context, final OfflineUserAction action) {
            options.setImageResource((MyApplication.nightThemeEnabled) ? R.mipmap.ic_more_vert_white_24dp : R.mipmap.ic_more_vert_black_24dp);

            description.setText(action.getActionId());

            if(action.isActionFailed()) {
                status.setTextColor(Color.RED);
                status.setText("FAILED");
            }
            else {
                status.setTextColor(Color.GREEN);
                status.setText("PENDING");
            }

            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPendingActionOptions(context, view, action);
                }
            });
        }

        private void showPendingActionOptions(Context context, View view, final OfflineUserAction action) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.pending_action_options);
            final PendingActionsAdapter adapter = ((PendingUserActionsActivity) context).getAdapter();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_execute_offline_action:
                            adapter.executeAction(action);
                            return true;
                        case R.id.action_edit_offline_action:
                            adapter.editAction(action);
                            return true;
                        case R.id.action_cancel_offline_action:
                            adapter.cancelAction(action);
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
