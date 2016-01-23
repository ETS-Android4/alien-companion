package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;

/**
 * Created by sound on 1/23/2016.
 */
public class NavDrawerMutliredditItem implements NavDrawerItem {

    private String name;

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_MULTIREDDIT_ITEM;
    }

    public NavDrawerMutliredditItem(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
