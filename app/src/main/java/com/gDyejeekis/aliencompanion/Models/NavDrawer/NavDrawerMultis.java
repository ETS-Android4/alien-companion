package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by sound on 1/23/2016.
 */
public class NavDrawerMultis implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_MULTIS;
    }

    public NavDrawerMultis() {

    }
}
