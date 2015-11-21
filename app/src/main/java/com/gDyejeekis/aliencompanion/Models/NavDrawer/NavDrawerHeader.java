package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

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
