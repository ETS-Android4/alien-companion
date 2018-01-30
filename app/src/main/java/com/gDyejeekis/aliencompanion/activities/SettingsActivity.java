package com.gDyejeekis.aliencompanion.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.settings_fragments.AppearanceSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.CommentsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.HeadersSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.LinkHandlingSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.NavigationSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.OtherSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.PostsCommentsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.PostsSettingsFragment;
import com.gDyejeekis.aliencompanion.fragments.settings_fragments.SyncSettingsFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SettingsMenuType;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

/**
 * Created by George on 8/4/2015.
 */
public class SettingsActivity extends ToolbarActivity {

    private SettingsMenuType menuType;

    private boolean dualPaneActive;

    private boolean dualPaneInLandScape;
    private boolean dualPaneEverywhere;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    public void onCreate(Bundle bundle) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(bundle);
        setContentView(R.layout.settings_dual_pane);

        if(MyApplication.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);

        initToolbar();

        menuType = (SettingsMenuType) getIntent().getSerializableExtra("menuType");
        try {
            getSupportActionBar().setTitle(menuType.value());
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle("Settings");
        }

        PreferenceFragment fragment;
        switch (menuType) {
            case headers:
                dualPaneEverywhere = GeneralUtils.isVeryLargeScreen(this);
                dualPaneInLandScape = GeneralUtils.isLargeScreen(this);
                fragment = new HeadersSettingsFragment();
                break;
            case appearance:
                fragment = new AppearanceSettingsFragment();
                break;
            case navigation:
                fragment = new NavigationSettingsFragment();
                break;
            case posts:
                fragment = new PostsSettingsFragment();
                break;
            case comments:
                fragment = new CommentsSettingsFragment();
                break;
            case postsAndComments:
                fragment = new PostsCommentsSettingsFragment();
                break;
            case sync:
                fragment = new SyncSettingsFragment();
                break;
            case linkHandling:
                fragment = new LinkHandlingSettingsFragment();
                break;
            case other:
                fragment = new OtherSettingsFragment();
                break;
            default:
                throw new RuntimeException("No corresponding fragment for this menu type");
        }

        if(dualPaneEverywhere) {
            dualPaneActive = true;
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && dualPaneInLandScape) {
            dualPaneActive = true;
        }
        else {
            dualPaneActive = false;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.headers_holder, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(menuType == SettingsMenuType.headers) {
            if(dualPaneEverywhere) {
                dualPaneActive = true;
            }
            else if(dualPaneInLandScape && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                dualPaneActive = true;
            }
            else {
                dualPaneActive = false;
                hideOptionsFragment();
            }
        }
        else {
            dualPaneActive = false;
        }
    }

    public void setupOptionsFragment(SettingsMenuType menuType) {
        if(menuType != SettingsMenuType.headers) {
            getSupportActionBar().setTitle(menuType.value());
            getFragmentManager().beginTransaction().replace(R.id.options_holder, SettingsMenuType.getSettingsFragment(menuType), "optionsFragment")
                    .commitAllowingStateLoss();
            findViewById(R.id.options_holder).setVisibility(View.VISIBLE);
        }
    }

    private void hideOptionsFragment() {
        getSupportActionBar().setTitle("Settings");
        findViewById(R.id.options_holder).setVisibility(View.GONE);
    }

    //private void showOptionsFragment() {
    //    if(getFragmentManager().findFragmentByTag("optionsFragment") != null) {
    //        findViewById(R.id.options_holder).setVisibility(View.VISIBLE);
    //    }
    //}

    @Override
    public void onBackPressed() {
        if(menuType == SettingsMenuType.headers) {
            MyApplication.getCurrentSettings();
        }
        super.onBackPressed();
    }

    public boolean isDualPaneActive() {
        return dualPaneActive;
    }

}
