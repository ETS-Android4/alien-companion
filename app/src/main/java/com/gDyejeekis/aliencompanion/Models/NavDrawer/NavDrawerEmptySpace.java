package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by sound on 4/9/2016.
 */
public class NavDrawerEmptySpace implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_EMPTY_SPACE;
    }
}
