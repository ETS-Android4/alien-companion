package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

/**
 * Created by George on 6/26/2015.
 */
public class NavDrawerHeader implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_HEADER;
    }

    public NavDrawerHeader() {

    }
}
