package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by sound on 4/10/2016.
 */
public class NavDrawerSeparator implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_SEPARATOR;
    }
}
