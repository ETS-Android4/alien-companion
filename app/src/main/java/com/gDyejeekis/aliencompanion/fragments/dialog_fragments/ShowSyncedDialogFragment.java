package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sound on 4/12/2016.
 */
public class ShowSyncedDialogFragment extends ScalableDialogFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    static class SyncedGroup {
        String name;
        long lastModified;
        int syncedCount; // not used for now
        String syncedAge;

        SyncedGroup(String name, long lastModified, int syncedCount) {
            this.name = name;
            this.lastModified = lastModified;
            this.syncedCount = syncedCount;
            this.syncedAge = ConvertUtils.getSubmissionAge((double) lastModified / 1000);
        }

        String getTitleText() {
            if (name.startsWith(AppConstants.MULTIREDDIT_FILE_PREFIX))
                return name.replace(AppConstants.MULTIREDDIT_FILE_PREFIX, "").concat(" (multi)");
            return name;
        }

        String getSubtitleText() {
            //return syncedCount + " posts synced " + syncedAge;
            return "synced " + syncedAge;
        }
    }

    private List<SyncedGroup> syncedGroups;
    private ListView syncedListView;
    private ArrayAdapter adapter;
    private ProgressBar progressBar;
    private TextView notSyncedTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_show_synced, container, false);

        syncedListView = view.findViewById(R.id.listView_synced_list);
        progressBar = view.findViewById(R.id.progressBar_synced_list);
        progressBar.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        notSyncedTextView = view.findViewById(R.id.textView_synced_list);
        syncedListView.setOnItemClickListener(this);
        syncedListView.setOnItemLongClickListener(this);

        FindSyncedTask task = new FindSyncedTask(this);
        task.execute();

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    public void updateSyncedList(List<SyncedGroup> syncedGroups) {
        progressBar.setVisibility(View.GONE);
        notSyncedTextView.setVisibility(View.GONE);
        syncedListView.setVisibility(View.VISIBLE);
        this.syncedGroups = syncedGroups;
        adapter = new SyncedGroupsAdapter(getActivity(), this.syncedGroups);
        syncedListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        dismiss();
        final String groupName = ((SyncedGroup) adapterView.getItemAtPosition(i)).name;
        String subreddit = (groupName.equals("frontpage")) ? null : groupName;
        boolean isMulti = groupName.startsWith("multi=");
        boolean isOther = groupName.equals(AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME);
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getListFragment().changeSubreddit(subreddit, isMulti, isOther);
            ((MainActivity) getActivity()).getNavDrawerAdapter().notifyDataSetChanged();
        }
        else {
            ((SubredditActivity) getActivity()).getListFragment().changeSubreddit(subreddit, isMulti, isOther);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final SyncedGroup group = (SyncedGroup) adapterView.getItemAtPosition(i);
        String message = "Delete all synced posts, comments, images and articles for '" + group.getTitleText() + "'?";
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyAlertDialogStyle)).setMessage(message).setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                Bundle args = new Bundle();
                args.putString("message", "Clearing synced data for '" + group.getTitleText() + "'");
                dialogFragment.setArguments(args);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        CleaningUtils.clearAllSyncedData(getActivity(), group.name);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        dialogFragment.dismiss();
                        syncedGroups.remove(group);
                        adapter.notifyDataSetChanged();
                        ToastUtils.showToast(getActivity(), "Synced data for '" + group.getTitleText() + "' cleared");
                    }
                }.execute();
            }
        }).show();
        return true;
    }

    public void noSyncedRedditsFound() {
        syncedListView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        notSyncedTextView.setVisibility(View.VISIBLE);
    }

    static class SyncedGroupsAdapter extends ArrayAdapter<SyncedGroup> {

        public SyncedGroupsAdapter(@NonNull Context context, @NonNull List<SyncedGroup> objects) {
            super(context, R.layout.simple_list_item_2line, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView==null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.simple_list_item_2line, parent, false);
            }
            SyncedGroup group = getItem(position);
            ((TextView) convertView.findViewById(R.id.text1)).setText(group.getTitleText());
            ((TextView) convertView.findViewById(R.id.text2)).setText(group.getSubtitleText());
            return convertView;
        }
    }

    static class FindSyncedTask extends AsyncTask<Void, Void, List<SyncedGroup>> {

        private ShowSyncedDialogFragment dialog;

        FindSyncedTask(ShowSyncedDialogFragment dialog) {
            this.dialog = dialog;
        }

        @Override
        public List<SyncedGroup> doInBackground(Void... unused) {
            try {
                List<SyncedGroup> syncedGroups = new ArrayList<>();
                File syncedDir = GeneralUtils.checkSyncedRedditDataDir(dialog.getActivity());
                File[] children = syncedDir.listFiles();
                for (File file : children) {
                    if (file.isDirectory()) {
                        File postsFile = new File(file.getAbsolutePath(), file.getName()+AppConstants.SYNCED_POST_LIST_SUFFIX);
                        if (postsFile.exists()) {
                            syncedGroups.add(new SyncedGroup(file.getName(), postsFile.lastModified(), -1));
                        }
                    }
                }
                Collections.sort(syncedGroups, new Comparator<SyncedGroup>() {
                    @Override
                    public int compare(SyncedGroup g1, SyncedGroup g2) {
                        return Long.valueOf(g2.lastModified).compareTo(g1.lastModified);
                    }
                });
                return syncedGroups;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(List<SyncedGroup> syncedGroups) {
            if (syncedGroups == null || syncedGroups.isEmpty())
                dialog.noSyncedRedditsFound();
            else dialog.updateSyncedList(syncedGroups);
        }
    }

}
