package com.gDyejeekis.aliencompanion.models.nav_drawer;

import android.graphics.drawable.Drawable;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.enums.MenuType;

/**
 * Created by George on 6/26/2015.
 */
public class NavDrawerMenuItem implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_MENU_ITEM;
    }

    private Drawable image;

    private MenuType type;

    public NavDrawerMenuItem(MenuType type) {
        this.type = type;
    }

    public NavDrawerMenuItem(Drawable drawable, MenuType type) {
        this.image = drawable;
        this.type = type;
    }

    public MenuType getMenuType() {
        return type;
    }

    public void setMenuType(MenuType type) {
        this.type = type;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

}
