package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

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
