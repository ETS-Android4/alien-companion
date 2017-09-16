package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;

import java.util.Arrays;

/**
 * Created by sound on 3/20/2016.
 */
public class SyncProfileOptionsDialogFragment extends ScalableDialogFragment implements CompoundButton.OnCheckedChangeListener {

    private SyncProfile profile;
    private SyncProfileOptions syncOptions;

    // TODO: 4/23/2017 should probably asssign from resource arrays
    private static final String[] postCountOptions = {"10", "25", "50", "75", "100"};
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
        profile = (SyncProfile) getArguments().getSerializable("profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_profile_options, container, false);

        if(profile.getSyncOptions() == null) {
            syncOptions = new SyncProfileOptions();
        }
        else {
            syncOptions = new SyncProfileOptions(profile.getSyncOptions());
        }

        boolean useGlobal = profile.isUseGlobalSyncOptions();

        syncPostCountSpinner = (Spinner) view.findViewById(R.id.spinner_syncPostCount);
        syncPostCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, postCountOptions));
        syncPostCountSpinner.setSelection(Arrays.asList(postCountOptions).indexOf(String.valueOf(syncOptions.getSyncPostCount())));
        syncPostCountSpinner.setEnabled(!useGlobal);

        syncCommentCountSpinner = (Spinner) view.findViewById(R.id.spinner_syncCommentCount);
        syncCommentCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentCountOptions));
        syncCommentCountSpinner.setSelection(Arrays.asList(commentCountOptions).indexOf(String.valueOf(syncOptions.getSyncCommentCount())));
        syncCommentCountSpinner.setEnabled(!useGlobal);

        syncCommentDepthSpinner = (Spinner) view.findViewById(R.id.spinner_syncCommentDepth);
        syncCommentDepthSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentDepthOptions));
        syncCommentDepthSpinner.setSelection(Arrays.asList(commentDepthOptions).indexOf(String.valueOf(syncOptions.getSyncCommentDepth())));
        syncCommentDepthSpinner.setEnabled(!useGlobal);

        syncCommentSortSpinner = (Spinner) view.findViewById(R.id.spinner_syncCommentSort);
        syncCommentSortSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, commentSortOptions));
        syncCommentSortSpinner.setSelection(Arrays.asList(commentSortOptions).indexOf(syncOptions.getSyncCommentSort().value().toUpperCase()));
        syncCommentSortSpinner.setEnabled(!useGlobal);

        albumImageLimitSpinner = (Spinner) view.findViewById(R.id.spinner_almumLimit);
        albumImageLimitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, albumLimitOptions));
        albumImageLimitSpinner.setSelection(Arrays.asList(albumLimitOptions).indexOf(String.valueOf(syncOptions.getAlbumSyncLimit())));
        albumImageLimitSpinner.setEnabled(!useGlobal);

        selfTextLinkCountSpinner = (Spinner) view.findViewById(R.id.spinner_syncSelfTextLinks);
        selfTextLinkCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, syncLinksInTextOptions));
        selfTextLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(syncOptions.getSyncSelfTextLinkCount())));
        selfTextLinkCountSpinner.setEnabled(!useGlobal);

        commentLinkCountSpinner = (Spinner) view.findViewById(R.id.spinner_syncCommentLinks);
        commentLinkCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), dropdownResource, syncLinksInTextOptions));
        commentLinkCountSpinner.setSelection(Arrays.asList(syncLinksInTextOptions).indexOf(String.valueOf(syncOptions.getSyncCommentLinkCount())));
        commentLinkCountSpinner.setEnabled(!useGlobal);

        syncThumbsCheckbox = (CheckBox) view.findViewById(R.id.checkBox_syncThumbs);
        syncThumbsCheckbox.setChecked(syncOptions.isSyncThumbs());
        syncThumbsCheckbox.setEnabled(!useGlobal);

        syncImagesCheckbox = (CheckBox) view.findViewById(R.id.checkBox_syncImages);
        syncImagesCheckbox.setChecked(syncOptions.isSyncImages());
        syncImagesCheckbox.setEnabled(!useGlobal);

        syncVideoCheckbox = (CheckBox) view.findViewById(R.id.checkBox_syncVideo);
        syncVideoCheckbox.setChecked(syncOptions.isSyncVideo());
        syncVideoCheckbox.setEnabled(!useGlobal);

        syncNewPostsOnlyCheckbox = (CheckBox) view.findViewById(R.id.checkBox_sync_new_only);
        syncNewPostsOnlyCheckbox.setChecked(syncOptions.isSyncNewPostsOnly());
        syncNewPostsOnlyCheckbox.setEnabled(!useGlobal);

        syncWifiOnlyCheckbox = (CheckBox) view.findViewById(R.id.checkBox_syncWifiOnly);
        syncWifiOnlyCheckbox.setChecked(syncOptions.isSyncOverWifiOnly());
        syncWifiOnlyCheckbox.setEnabled(!useGlobal);

        syncArticlesCheckbox = (CheckBox) view.findViewById(R.id.checkBox_syncArticles);
        syncArticlesCheckbox.setChecked(syncOptions.isSyncWebpages());
        syncArticlesCheckbox.setEnabled(!useGlobal);

        useGlobalSwitch = (Switch) view.findViewById(R.id.switch_global_options);
        useGlobalSwitch.setChecked(useGlobal);
        useGlobalSwitch.setOnCheckedChangeListener(this);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(useGlobalSwitch.isChecked()) {
            syncOptions = null;
        }
        else {
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

        profile.setUseGlobalSyncOptions(useGlobalSwitch.isChecked());
        profile.setSyncOptions(syncOptions);
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

}
