package com.gDyejeekis.aliencompanion.Activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.gDyejeekis.aliencompanion.Fragments.ComposeMessageFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitFragments.SubmitCommentFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitFragments.SubmitImageFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitFragments.SubmitLinkFragment;
import com.gDyejeekis.aliencompanion.Fragments.SubmitFragments.SubmitTextFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

public class SubmitActivity extends BackNavActivity implements DialogInterface.OnClickListener {

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        if(MyApplication.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFragment();
    }

    private void setupFragment() {
        if(getFragmentManager().findFragmentByTag("submitFragment") == null) {
            SubmitType submitType = (SubmitType) getIntent().getSerializableExtra("submitType");
            //Fragment fragment = null;
            switch (submitType) {
                case link:
                    getSupportActionBar().setTitle("Submit link");
                    fragment = new SubmitLinkFragment();
                    break;
                case self:
                    getSupportActionBar().setTitle("Submit text");
                    fragment = new SubmitTextFragment();
                    break;
                case image:
                    getSupportActionBar().setTitle("Submit image");
                    fragment = new SubmitImageFragment();
                    break;
                case comment:
                    getSupportActionBar().setTitle("Submit reply");
                    fragment = new SubmitCommentFragment();
                    break;
                case message:
                    getSupportActionBar().setTitle("Compose message");
                    fragment = new ComposeMessageFragment();
                    break;
            }
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, fragment, "submitFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit_post, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        String message = "";
        boolean showDialog = false;
        if(fragment instanceof SubmitCommentFragment) {
            message = "Discard comment?";
            showDialog = ((SubmitCommentFragment) fragment).displayConfirmDialog();
        }
        else if(fragment instanceof SubmitLinkFragment) {
            message = "Discard post?";
            showDialog = ((SubmitLinkFragment) fragment).displayConfirmDialog();
        }
        else if(fragment instanceof SubmitTextFragment) {
            message = "Discard post?";
            showDialog = ((SubmitTextFragment) fragment).displayConfirmDialog();
        }

        if(showDialog) {
            new AlertDialog.Builder(this).setMessage(message).setPositiveButton("Yes", this).setNegativeButton("No", null).show();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        super.onBackPressed();
    }

}
