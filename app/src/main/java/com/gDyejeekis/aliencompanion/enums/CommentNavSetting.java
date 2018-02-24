package com.gDyejeekis.aliencompanion.enums;

import com.gDyejeekis.aliencompanion.AppConstants;
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

    public static float getIconOpacity() {
        switch (MyApplication.currentBaseTheme) {
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                return 0.6f;
            case AppConstants.LIGHT_THEME:
                return 0.54f;
            default:
                return 1f;
        }
    }

    public int getIconResource() {
        switch (this) {
            case threads:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_forum_white_24dp : R.drawable.ic_forum_black_24dp;
            case ama:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_people_white_24dp : R.drawable.ic_people_black_24dp;
            case op:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_person_white_48dp : R.drawable.ic_person_black_48dp;
            case searchText:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_search_white_24dp : R.drawable.ic_search_black_24dp;
            case time:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_access_time_white_24dp : R.drawable.ic_access_time_black_24dp;
            case gilded:
                return MyApplication.nightThemeEnabled ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_black_24dp;
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
                return R.drawable.ic_person_white_48dp;
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
