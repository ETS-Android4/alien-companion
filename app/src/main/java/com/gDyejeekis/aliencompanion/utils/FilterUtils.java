package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.filters.DomainFilter;
import com.gDyejeekis.aliencompanion.models.filters.Filter;
import com.gDyejeekis.aliencompanion.models.filters.FilterProfile;
import com.gDyejeekis.aliencompanion.models.filters.FlairFilter;
import com.gDyejeekis.aliencompanion.models.filters.SelfTextFilter;
import com.gDyejeekis.aliencompanion.models.filters.SubredditFilter;
import com.gDyejeekis.aliencompanion.models.filters.TitleFilter;
import com.gDyejeekis.aliencompanion.models.filters.UserFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 6/20/2017.
 */

public class FilterUtils {

    public static final String TAG = "FilterUtils";

    public static List<RedditItem> checkProfiles(Context context, List<RedditItem> items, String currentReddit, boolean isMulti) {
        List<FilterProfile> profiles = getFilterProfiles(context);
        if(profiles!=null) {
            for (FilterProfile profile : profiles) {
                if(profile.isActive()) {
                    if (checkProfileRestrictions(profile, currentReddit, isMulti)) {
                        items = checkFilters(profile, items);
                    }
                }
            }
        }
        return items;
    }

    // return true if current reddit is valid for filtering according to restrctions
    private static boolean checkProfileRestrictions(FilterProfile profile, String currentReddit, boolean isMulti) {
        if(isMulti) {
            if(profile.getMultiredditRestrictions()!=null && !profile.getMultiredditRestrictions().isEmpty()) {
                return profile.getMultiredditRestrictions().contains(currentReddit);
            }
        }
        else {
            if(profile.getSubredditRestrictions()!=null && !profile.getSubredditRestrictions().isEmpty()) {
                return profile.getSubredditRestrictions().contains(currentReddit);
            }
        }
        return true;
    }

    public static List<RedditItem> checkFilters(FilterProfile profile, List<RedditItem> items) {
        List<RedditItem> filteredItems = new ArrayList<>();
        for(RedditItem item : items) {
            boolean match = false;
            if(item instanceof Submission) {
                Submission post = (Submission) item;
                for (Filter filter : profile.getFilters()) {
                    if (filter instanceof DomainFilter) {
                        match = filter.match(post.getDomain());
                    } else if (filter instanceof TitleFilter) {
                        match = filter.match(post.getTitle());
                    } else if (filter instanceof FlairFilter) {
                        match = filter.match(post.getLinkFlairText());
                    } else if (filter instanceof SelfTextFilter) {
                        match = filter.match(post.getSelftext());
                    } else if (filter instanceof SubredditFilter) {
                        match = filter.match(post.getSubreddit());
                    } else if (filter instanceof UserFilter) {
                        match = filter.match(post.getAuthor());
                    }
                    if(match) break;
                }
            }
            if(!match) filteredItems.add(item);
        }
        return filteredItems;
    }

    public static List<FilterProfile> getFilterProfiles(Context context) {
        try {
            return (List<FilterProfile>) GeneralUtils.readObjectFromFile(new File(context.getFilesDir(), AppConstants.FILTER_PROFILES_FILENAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
