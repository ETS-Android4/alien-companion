package com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SyncProfileDialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 3/20/2016.
 */
public class SyncProfileOptionsDialogFragment extends ScalableDialogFragment {

    private SyncProfile profile;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        profile = (SyncProfile) getArguments().getSerializable("profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_profile_options, container, false);

        return view;
    }
}
