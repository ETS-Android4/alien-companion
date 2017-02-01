package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

/**
 * Created by sound on 4/10/2016.
 */
public class NavDrawerSeparator implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_SEPARATOR;
    }
}
