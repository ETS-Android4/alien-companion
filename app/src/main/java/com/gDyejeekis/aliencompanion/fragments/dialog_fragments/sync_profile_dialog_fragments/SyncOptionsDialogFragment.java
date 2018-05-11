package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.ProfilesActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.util.Arrays;

/**
 * Created by sound on 3/20/2016.
 */
public class SyncOptionsDialogFragment extends ScalableDialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SyncProfileOptions syncOptions;
    private SyncProfile profile;
    private boolean customSync;

    // TODO: 4/23/2017 should probably asssign from resource arrays
    private static final String[] postCountOptions = {"10", "25", "50", "75", "100", "150", "200"};
    private static final String[] commentCountOptions = {"50", "100", "200", "400", "600"};
    private static final String[] commentDepthOptions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private static final String[] albumLimitOptions = {"1", "2", "5", "10", "25", "50"};
    private static final String[] commentSortOptions = {CommentSort.TOP.value().toUpperCase(), CommentSort.BEST.value().toUpperCase(), CommentSort.NEW.value().toUpperCase(),
            CommentSort.OLD.value().toUpperCase(), CommentSort.CONTROVERSIAL.value().toUpperCase()};
    private static final String[] syncLinksInTextOptions = {"0", "5", "10", "25", "50"};

    private int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.spinner_dropdown_item_dark : R.layout.spinner_dropdown_item_light;

    private Switch useGlobalSwitch;
    private Spinner syncPostCountSpinner;
    private Spinner syncCommentCountSpinner;
    private Spinner syncCommentDepthSpinner;
    private Spinner syncCommentSortSpinner;
    private Spinner albumImageLimitSpinner;
    private Spinner selfTextLinkCountSpinner;
    private Spinner commentLinkCountSpinner;
    private CheckBox syncThumbsCheckbox;
    private CheckBox syncImagesCheckbox;
    private CheckBox syncVideoCheckbox;
    private CheckBox syncArticlesCheckbox;
    private CheckBox syncWifiOnlyCheckbox;
    private CheckBox syncNewPostsOnlyCheckbox;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments()!=null) {
            customSync = getArguments().getBoolean("customSync", false);
            profile = (SyncProfile) getArguments().getSerializable("profile");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_options, container, false);

        useGlobalSwitch = view.findViewById(R.id.switch_global_options);
        LinearLayout layoutButtons = view.findViewById(R.id.layout_buttons);
        boolean optionsEnabled;

        if (customSync) {
            layoutButtons.setVisibility(View.VISIBLE);
            Button btnSync = view.findViewById(R.id.button_sync);
            Button btnCancel = view.findViewById(R.id.button_cancel);
            btnSync.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
        } else {
            layoutButtons.setVisibility(View.GONE);
        }

        if (profile == null) {
            optionsEnabled = true;
            useGlobalSwitch.setVisibility(View.GONE);
            syncOptions = new SyncProfileOptions();
        } else {
            optionsEnabled = !profile.isUseGlobalSyncOptions();
            useGlobalSwitch.setVisibility(View.VISIBLE);
            useGlobalSwitch.setChecked(!optionsEnabled);
            useGlobalSwitch.setOnCheckedChangeListener(this);
            syncOptions = (profile.getSyncOptions()==null) ? new SyncProfileOptions()
                    : new SyncProfileOptions(profile.getSyncOptions());
        }

        syncPostCountSpinner = view.findViewById(R.id.spinner_syncPostCount);
        syncPostCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, postCountOptions));
        syncPostCountSpinner.setSelection(Arrays.asList(postCountOptions).indexOf(String.valueOf(syncOptions.getSyncPostCount())));
        syncPostCountSpinner.setEnabled(optionsEnabled);

        syncCommentCountSpinner = view.findViewById(R.id.spinner_syncCommentCount);
        syncCommentCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentCountOptions));
        syncCommentCountSpinner.setSelection(Arrays.asList(commentCountOptions).indexOf(String.valueOf(syncOptions.getSyncCommentCount())));
        syncCommentCountSpinner.setEnabled(optionsEnabled);

        syncCommentDepthSpinner = view.findViewById(R.id.spinner_syncCommentDepth);
        syncCommentDepthSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentDepthOptions));
        syncCommentDepthSpinner.setSelection(Arrays.asList(commentDepthOptions).indexOf(String.valueOf(syncOptions.getSyncCommentDepth())));
        syncCommentDepthSpinner.setEnabled(optionsEnabled);

        syncCommentSortSpinner = view.findViewById(R.id.spinner_syncCommentSort);
        syncCommentSortSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentSortOptions));
        syncCommentSortSpinner.setSelection(Arrays.asList(commentSortOptions).indexOf(syncOptions.getSyncCommentSort().value().toUpperCase()));
        syncCommentSortSpinner.setEnabled(optionsEnabled);

        albumImageLimitSpinner = view.findViewById(R.id.spinner_almumLimit);
        albumImageLimitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, albumLimitOptions));
        albumImageLimitSpinner.setSelection(Arrays.asList(albumLimitOptions).indexOf(String.valueOf(syncOptions.getAlbumSyncLimit())));
        albumImageLimitSpinner.setEnabled(optionsEnabled);

        selfTextLinkCountSpinner = view.findViewById(R.id.spinner_syncSelfTextLinks);
        selfTextLinkCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, syncLinksInTextOptions));
        selfTextLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(syncOptions.getSyncSelfTextLinkCount())));
        selfTextLinkCountSpinner.setEnabled(optionsEnabled);

        commentLinkCountSpinner = view.findViewById(R.id.spinner_syncCommentLinks);
        commentLinkCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, syncLinksInTextOptions));
        commentLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(syncOptions.getSyncCommentLinkCount())));
        commentLinkCountSpinner.setEnabled(optionsEnabled);

        syncThumbsCheckbox = view.findViewById(R.id.checkBox_syncThumbs);
        syncThumbsCheckbox.setChecked(syncOptions.isSyncThumbs());
        syncThumbsCheckbox.setEnabled(optionsEnabled);

        syncImagesCheckbox = view.findViewById(R.id.checkBox_syncImages);
        syncImagesCheckbox.setChecked(syncOptions.isSyncImages());
        syncImagesCheckbox.setEnabled(optionsEnabled);

        syncVideoCheckbox = view.findViewById(R.id.checkBox_syncVideo);
        syncVideoCheckbox.setChecked(syncOptions.isSyncVideo());
        syncVideoCheckbox.setEnabled(optionsEnabled);

        syncNewPostsOnlyCheckbox = view.findViewById(R.id.checkBox_sync_new_only);
        syncNewPostsOnlyCheckbox.setChecked(syncOptions.isSyncNewPostsOnly());
        syncNewPostsOnlyCheckbox.setEnabled(optionsEnabled);

        syncWifiOnlyCheckbox = view.findViewById(R.id.checkBox_syncWifiOnly);
        syncWifiOnlyCheckbox.setChecked(syncOptions.isSyncOverWifiOnly());
        syncWifiOnlyCheckbox.setEnabled(optionsEnabled);

        syncArticlesCheckbox = view.findViewById(R.id.checkBox_syncArticles);
        syncArticlesCheckbox.setChecked(syncOptions.isSyncWebpages());
        syncArticlesCheckbox.setEnabled(optionsEnabled);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!customSync && profile!=null) {
            if (useGlobalSwitch.isChecked()) {
                syncOptions = null;
            } else {
                updateSyncOptions();
            }
            profile.setUseGlobalSyncOptions(useGlobalSwitch.isChecked());
            profile.setSyncOptions(syncOptions);
        }
    }

    private void updateSyncOptions() {
        syncOptions.setSyncPostCount(Integer.valueOf((String) syncPostCountSpinner.getSelectedItem()));
        syncOptions.setSyncCommentCount(Integer.valueOf((String) syncCommentCountSpinner.getSelectedItem()));
        syncOptions.setSyncCommentDepth(Integer.valueOf((String) syncCommentDepthSpinner.getSelectedItem()));
        syncOptions.setSyncCommentSort(CommentSort.valueOf((String) syncCommentSortSpinner.getSelectedItem()));
        syncOptions.setAlbumSyncLimit(Integer.valueOf((String) albumImageLimitSpinner.getSelectedItem()));
        syncOptions.setSyncSelfTextLinkCount(Integer.valueOf((String) selfTextLinkCountSpinner.getSelectedItem()));
        syncOptions.setSyncCommentLinkCount(Integer.valueOf((String) commentLinkCountSpinner.getSelectedItem()));
        syncOptions.setSyncThumbs(syncThumbsCheckbox.isChecked());
        syncOptions.setSyncImages(syncImagesCheckbox.isChecked());
        syncOptions.setSyncVideo(syncVideoCheckbox.isChecked());
        syncOptions.setSyncWebpages(syncArticlesCheckbox.isChecked());
        syncOptions.setSyncNewPostsOnly(syncNewPostsOnlyCheckbox.isChecked());
        syncOptions.setSyncOverWifiOnly(syncWifiOnlyCheckbox.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            syncPostCountSpinner.setSelection(Arrays.asList(postCountOptions).indexOf(String.valueOf(MyApplication.syncPostCount)));
            syncCommentCountSpinner.setSelection(Arrays.asList(commentCountOptions).indexOf(String.valueOf(MyApplication.syncCommentCount)));
            syncCommentDepthSpinner.setSelection(Arrays.asList(commentDepthOptions).indexOf(String.valueOf(MyApplication.syncCommentDepth)));
            syncCommentSortSpinner.setSelection(Arrays.asList(commentSortOptions).indexOf(MyApplication.syncCommentSort.value().toUpperCase()));
            albumImageLimitSpinner.setSelection(Arrays.asList(commentDepthOptions).indexOf(String.valueOf(MyApplication.syncAlbumImgCount)));
            selfTextLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(MyApplication.syncSelfTextLinkCount)));
            commentLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(MyApplication.syncCommentLinkCount)));
            syncThumbsCheckbox.setChecked(MyApplication.syncThumbnails);
            syncImagesCheckbox.setChecked(MyApplication.syncImages);
            syncVideoCheckbox.setChecked(MyApplication.syncVideo);
            syncArticlesCheckbox.setChecked(MyApplication.syncWebpages);
            syncNewPostsOnlyCheckbox.setChecked(MyApplication.syncNewPostsOnly);
            syncWifiOnlyCheckbox.setChecked(MyApplication.syncOverWifiOnly);
        }
        syncPostCountSpinner.setEnabled(!b);
        syncCommentCountSpinner.setEnabled(!b);
        syncCommentDepthSpinner.setEnabled(!b);
        syncCommentSortSpinner.setEnabled(!b);
        albumImageLimitSpinner.setEnabled(!b);
        selfTextLinkCountSpinner.setEnabled(!b);
        commentLinkCountSpinner.setEnabled(!b);
        syncThumbsCheckbox.setEnabled(!b);
        syncImagesCheckbox.setEnabled(!b);
        syncVideoCheckbox.setEnabled(!b);
        syncArticlesCheckbox.setEnabled(!b);
        syncNewPostsOnlyCheckbox.setEnabled(!b);
        syncWifiOnlyCheckbox.setEnabled(!b);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sync:
                dismiss();
                updateSyncOptions();
                // TODO: 3/24/2018 abstraction here
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getListFragment().addToSyncQueue(syncOptions);
                } else if (getActivity() instanceof SubredditActivity) {
                    ((SubredditActivity) getActivity()).getListFragment().addToSyncQueue(syncOptions);
                } else if (getActivity() instanceof UserActivity) {
                    ((UserActivity) getActivity()).getListFragment().addSavedToSyncQueue(syncOptions);
                } else if (getActivity() instanceof ProfilesActivity) {
                    addSyncProfileToQueue();
                }
                break;
            case R.id.button_cancel:
                dismiss();
                break;
        }
    }

    private void addSyncProfileToQueue() {
        Context context = getContext();
        if (context!=null && profile!=null) {
            String toastMessage;
            if (GeneralUtils.isNetworkAvailable(context)) {
                boolean syncOverWifiOnly = (profile.isUseGlobalSyncOptions()) ? MyApplication.syncOverWifiOnly
                        : syncOptions.isSyncOverWifiOnly();
                if (syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(context)) {
                    toastMessage = "Syncing over mobile data connection is disabled";
                } else {
                    toastMessage = profile.getName() + " added to sync queue";
                    Intent intent = new Intent(context, DownloaderService.class);
                    intent.putExtra("profileId", profile.getProfileId());
                    intent.putExtra("syncOptions", syncOptions);
                    context.startService(intent);
                }
            } else {
                toastMessage = "Network connection unavailable";
            }
            ToastUtils.showToast(context, toastMessage);
        }
    }

}
