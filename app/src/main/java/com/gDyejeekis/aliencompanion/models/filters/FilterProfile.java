package com.gDyejeekis.aliencompanion.models.filters;

import com.gDyejeekis.aliencompanion.models.Profile;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 6/18/2017.
 */

public class FilterProfile extends Profile implements Serializable {

    private static final long serialVersionUID = 47322143L;

    private boolean isActive;
    private List<Filter> filters;
    private List<String> subredditRestrictions;
    private List<String> multiredditRestrictions;

    public FilterProfile() {
        super("");
    }

    public FilterProfile(String name) {
        super(name);
    }

    public FilterProfile(String name, List<Filter> filters) {
        super(name);
        this.filters = filters;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean hasFilters() {
        return filters!=null && !filters.isEmpty();
    }

    public boolean addFilter(Filter filter) {
        if(filters == null) {
            filters = new ArrayList<>();
        }
        return filter.isValid() && filters.add(filter);
    }

    public boolean removeFilter(Filter filter) {
        return filters!=null && filters.remove(filter);
    }

    public boolean addSubredditRestriction(String subreddit) {
        if(subredditRestrictions == null) {
            subredditRestrictions = new ArrayList<>();
        }
        return subredditRestrictions.add(subreddit);
    }

    public boolean removeSubredditRestrction(int index) {
        if(subredditRestrictions!=null && subredditRestrictions.size()>index) {
            subredditRestrictions.remove(index);
            return true;
        }
        return false;
    }

    public boolean addMultiredditRestriction(String multireddit) {
        if(multiredditRestrictions == null) {
            multiredditRestrictions = new ArrayList<>();
        }
        return multiredditRestrictions.add(multireddit);
    }

    public boolean removeMultiredditRestrction(int index) {
        if(multiredditRestrictions!=null && multiredditRestrictions.size()>index) {
            multiredditRestrictions.remove(index);
            return true;
        }
        return false;
    }

    public List<String> getSubredditRestrictions() {
        return subredditRestrictions;
    }

    public List<String> getMultiredditRestrictions() {
        return multiredditRestrictions;
    }

    public List<Filter> getFilters() {
        return this.filters;
    }

    public List<Filter> getFilters(Class<? extends Filter> c) {
        if(this.filters == null) {
            return null;
        }

        List<Filter> filters = new ArrayList<>();
        for(Filter filter : this.filters) {
            if(c.isInstance(filter)) {
                filters.add(filter);
            }
        }
        return filters;
    }

    public List<String> getFilterStrings(Class<? extends Filter> c) {
        if(this.filters == null) {
            return null;
        }

        List<String> filters = new ArrayList<>();
        for(Filter filter : this.filters) {
            if(c.isInstance(filter)) {
                filters.add(filter.getFilterText());
            }
        }
        return filters;
    }

    public boolean containsFilter(Filter filter) {
        return filters!=null && filters.contains(filter);
    }

    public boolean containsFilter(Class<? extends Filter> cls, String filterText) {
        List<String> filters = getFilterStrings(cls);
        return filters!=null && filters.contains(filterText);
    }

    public boolean containsSubredditRestriction(String subreddit) {
        return subredditRestrictions!=null && subredditRestrictions.contains(subreddit);
    }

    public boolean containsMultiredditRestrction(String multireddit) {
        return multiredditRestrictions!=null && multiredditRestrictions.contains(multireddit);
    }

}
