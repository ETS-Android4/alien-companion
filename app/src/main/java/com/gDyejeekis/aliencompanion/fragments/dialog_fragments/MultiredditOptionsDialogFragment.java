package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 1/29/2016.
 */
public class MultiredditOptionsDialogFragment extends ScalableDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multireddit_options, container, false);

        return view;
    }
}
