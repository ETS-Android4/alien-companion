package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

/**
 * Created by George on 7/16/2016.
 */
public class NavDrawerOther implements NavDrawerItem {
    @Override
    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_OTHER;
    }
}
