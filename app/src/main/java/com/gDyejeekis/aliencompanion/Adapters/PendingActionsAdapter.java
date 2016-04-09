package com.gDyejeekis.aliencompanion.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;

import java.util.List;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingActionsAdapter extends RecyclerView.Adapter {

    private List<OfflineUserAction> pendingActions;

    public PendingActionsAdapter() {

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
