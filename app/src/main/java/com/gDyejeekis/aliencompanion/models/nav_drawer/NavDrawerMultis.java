package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

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
