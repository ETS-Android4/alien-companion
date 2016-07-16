package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by George on 7/16/2016.
 */
public class NavDrawerOtherItem implements NavDrawerItem {

    private String name;

    public NavDrawerOtherItem(String name) {
        this.name = name;
    }

    @Override
    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_OTHER_ITEM;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
