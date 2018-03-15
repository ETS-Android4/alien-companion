package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;

/**
 * Created by George on 6/26/2015.
 */
public class HeaderListener extends NavDrawerListener {

    public HeaderListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_theme_switch:
                getAdapter().showThemesDialog();
                break;
            case R.id.button_offline_switch:
                getAdapter().showOfflineSwitchDialog();
                break;
            default:
                getAdapter().toggleAccountItems();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.button_offline_switch:
                getAdapter().switchModeGracefully();
                return true;
        }
        return false;
    }
}
