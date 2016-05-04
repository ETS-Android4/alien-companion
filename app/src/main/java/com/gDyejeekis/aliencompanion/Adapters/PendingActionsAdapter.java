package com.gDyejeekis.aliencompanion.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.CommentAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.DownvoteAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.HideAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.ReportAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.SaveAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.SubmitLinkAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.SubmitTextAction;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Services.PendingActionsService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingActionsAdapter extends RecyclerView.Adapter {

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

    public void remove(int position) {
        pendingActions.remove(position);
        notifyItemRemoved(position);
    }

    public void notifyActionResult(String actionId, boolean success) {
        OfflineUserAction currentAction = null;
        for(OfflineUserAction action : pendingActions) {
            if(action.getActionId().equals(actionId)) {
                currentAction = action;
                break;
            }
        }
        if(currentAction != null) {
            int index = pendingActions.indexOf(currentAction);
            if (success) {
                pendingActions.remove(index);
                notifyItemRemoved(index);
            }
            else {
                currentAction.setActionFailed(true);
                pendingActions.set(index, currentAction);
                notifyItemChanged(index);
            }
        }
    }

    public void markActionFailed(int position, boolean flag) {
        pendingActions.get(position).setActionFailed(flag);
        notifyItemChanged(position);
    }

    public void executeAllActions() {
        if(GeneralUtils.isNetworkAvailable(activity)) {
            ToastUtils.displayShortToast(activity, "Executing remaining actions..");
            Intent intent = new Intent(activity, PendingActionsService.class);
            activity.startService(intent);
        }
        else {
            ToastUtils.displayShortToast(activity, "Network connection unavailable");
        }
    }

    public void executeAction(int position) {
        if(GeneralUtils.isNetworkAvailable(activity)) {
            ToastUtils.displayShortToast(activity, "Executing action..");
            ExecuteSingleActionTask task = new ExecuteSingleActionTask(activity, position);
            task.execute(pendingActions.get(position));
        }
        else {
            ToastUtils.displayShortToast(activity, "Network connection unavailable");
        }
    }

    public void executeAction(OfflineUserAction action) {
        executeAction(pendingActions.indexOf(action));
    }

    public void actionEdited(int position, OfflineUserAction newAction) {
        pendingActions.set(position, newAction);
        saveChanges();
        ToastUtils.displayShortToast(activity, "Offline action edit successful");
    }

    public void editAction(int position) {
        OfflineUserAction action = pendingActions.get(position);
        switch (action.getActionType()) {
            case CommentAction.ACTION_TYPE:
                Intent intent = new Intent(activity, SubmitActivity.class);
                intent.putExtra("submitType", SubmitType.comment);
                intent.putExtra("edit", true);
                intent.putExtra("offline", action);
                intent.putExtra("index", position);
                activity.startActivity(intent);
                break;
            case SubmitTextAction.ACTION_TYPE:
                break;
            case SubmitLinkAction.ACTION_TYPE:
                break;
            case ReportAction.ACTION_TYPE:
                break;
            case UpvoteAction.ACTION_TYPE:
                break;
            case SaveAction.ACTION_TYPE:
                break;
            case HideAction.ACTION_TYPE:
                break;
        }
    }

    public void editAction(OfflineUserAction action) {
        editAction(pendingActions.indexOf(action));
    }

    public void cancelAllActions() {
        pendingActions.clear();
        notifyDataSetChanged();
        saveChanges();
    }

    public void cancelAction(int position) {
        pendingActions.remove(position);
        notifyDataSetChanged();
        saveChanges();
    }

    public void cancelAction(OfflineUserAction action) {
        cancelAction(pendingActions.indexOf(action));
    }

    public void saveChanges() {
        try {
            GeneralUtils.writeObjectToFile(pendingActions, new File(activity.getFilesDir(), MyApplication.OFFLINE_USER_ACTIONS_FILENAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static class ExecuteSingleActionTask extends AsyncTask<OfflineUserAction, Void, Boolean> {

        PendingUserActionsActivity activity;
        int position;

        public ExecuteSingleActionTask(PendingUserActionsActivity activity, int position) {
            this.activity = activity;
            this.position = position;
        }

        @Override
        public Boolean doInBackground(OfflineUserAction... actions) {
            OfflineUserAction action = actions[0];
            action.executeAction(activity);
            return action.isActionCompleted();
        }

        @Override
        public void onPostExecute(Boolean success) {
            if(success) {
                activity.getAdapter().remove(position);
            }
            else {
                activity.getAdapter().markActionFailed(position, true);
                ToastUtils.displayShortToast(activity, "Error completing user action");
            }
            activity.getAdapter().saveChanges();
        }

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

            description.setText(action.getActionName() + " - " + action.getActionPreview());

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

        private void showPendingActionOptions(final Context context, View view, final OfflineUserAction action) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.pending_action_options);
            Menu menu = popupMenu.getMenu();
            menu.findItem(R.id.action_edit_offline_action).setVisible(action.getActionType() == CommentAction.ACTION_TYPE);
            final PendingActionsAdapter adapter = ((PendingUserActionsActivity) context).getAdapter();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_execute_offline_action:
                            if(PendingUserActionsActivity.checkServiceRunning(context)) {
                                return true;
                            }
                            adapter.executeAction(action);
                            return true;
                        case R.id.action_edit_offline_action:
                            if(PendingUserActionsActivity.checkServiceRunning(context)) {
                                return true;
                            }
                            adapter.editAction(action);
                            return true;
                        case R.id.action_cancel_offline_action:
                            if(PendingUserActionsActivity.checkServiceRunning(context)) {
                                return true;
                            }
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
