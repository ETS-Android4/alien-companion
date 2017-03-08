package com.gDyejeekis.aliencompanion.enums;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 3/5/2017.
 */

public enum CommentNavSetting {

    threads("Threads"),
    ama("AMA mode"),
    op("Original poster"),
    searchText("Search text"),
    time("Time"),
    gilded("Gilded");


    private final String value;

    CommentNavSetting(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public int getIconResource() {
        switch (this) {
            case threads:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.drawable.ic_forum_grey_600_24dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.drawable.ic_forum_light_grey_24dp;
                }
                else {
                    return R.drawable.ic_forum_white_24dp;
                }
            case ama:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.drawable.ic_people_grey_600_24dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.drawable.ic_people_light_grey_24dp;
                }
                else {
                    return R.drawable.ic_people_white_24dp;
                }
            case op:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.mipmap.ic_person_grey_48dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.mipmap.ic_person_light_grey_48dp;
                }
                else {
                    return R.mipmap.ic_person_white_48dp;
                }
            case searchText:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.drawable.ic_search_grey_600_24dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.drawable.ic_search_light_grey_24dp;
                }
                else {
                    return R.drawable.ic_search_white_24dp;
                }
            case time:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.drawable.ic_access_time_grey_600_24dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.drawable.ic_access_time_light_grey_24dp;
                }
                else {
                    return R.drawable.ic_access_time_white_24dp;
                }
            case gilded:
                if(MyApplication.currentBaseTheme == MyApplication.LIGHT_THEME) {
                    return R.drawable.ic_star_grey_600_24dp;
                }
                else if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                    return R.drawable.ic_star_light_grey_24dp;
                }
                else {
                    return R.drawable.ic_star_white_24dp;
                }
            default:
                throw new RuntimeException("No icon resource found for enum value");
        }
    }

    public int getIconResourceWhite() {
        switch (this) {
            case threads:
                return R.drawable.ic_forum_white_24dp;
            case ama:
                return R.drawable.ic_people_white_24dp;
            case op:
                return R.mipmap.ic_person_white_48dp;
            case searchText:
                return R.drawable.ic_search_white_24dp;
            case time:
                return R.drawable.ic_access_time_white_24dp;
            case gilded:
                return R.drawable.ic_star_white_24dp;
            default:
                throw new RuntimeException("No icon resource found for enum value");
        }
    }

}
