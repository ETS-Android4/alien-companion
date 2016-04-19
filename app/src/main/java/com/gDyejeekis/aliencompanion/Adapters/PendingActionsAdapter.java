package com.gDyejeekis.aliencompanion.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return pendingActions.size();
    }

    public static class PendingActionViewHolder extends RecyclerView.ViewHolder {

        public PendingActionViewHolder(View itemView) {
            super(itemView);
        }
    }
}
