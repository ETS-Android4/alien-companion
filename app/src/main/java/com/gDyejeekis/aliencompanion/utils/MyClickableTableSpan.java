package com.gDyejeekis.aliencompanion.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ViewTableDialogFragment;

/**
 * Created by George on 5/28/2017.
 */

public class MyClickableTableSpan extends ClickableTableSpan {
    @Override
    public ClickableTableSpan newInstance() {
        return new MyClickableTableSpan();
    }

    @Override
    public void onClick(View widget) {
        ViewTableDialogFragment dialogFragment = new ViewTableDialogFragment();
        Bundle args = new Bundle();
        args.putString("tableHtml", getTableHtml());
        dialogFragment.setArguments(args);
        AppCompatActivity compatActivity = (AppCompatActivity) GeneralUtils.scanForActivity(widget.getContext());
        FragmentManager fm = compatActivity.getSupportFragmentManager();
        dialogFragment.show(fm, "dialog");
    }
}
