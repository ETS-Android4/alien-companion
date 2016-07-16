package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by George on 7/16/2016.
 */
public class NavDrawerOther implements NavDrawerItem {
    @Override
    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_OTHER;
    }
}
