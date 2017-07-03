package com.gDyejeekis.aliencompanion.models.filters;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by George on 6/21/2017.
 */

public abstract class Filter implements Serializable {

    protected final String filterText;

    public Filter(String filterText) {
        this.filterText = filterText;
    }

    public abstract boolean match(String text);

    public abstract boolean isValid();

    public abstract String getTextRequirements();

    public abstract String getHeader();

    public String getFilterText() {
        return filterText;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getClass().isInstance(obj) && ((Filter) obj).getFilterText().equals(this.filterText);
    }

    public static Filter newInstance(Class<? extends Filter> cls, String filterText) {
        Filter filter = null;
        if(cls == DomainFilter.class) {
            filter = new DomainFilter(filterText);
        }
        else if(cls == TitleFilter.class) {
            filter = new TitleFilter(filterText);
        }
        else if(cls == FlairFilter.class) {
            filter = new FlairFilter(filterText);
        }
        else if(cls == SelfTextFilter.class) {
            filter = new SelfTextFilter(filterText);
        }
        else if(cls == SubredditFilter.class) {
            filter = new SubredditFilter(filterText);
        }
        else if(cls == UserFilter.class) {
            filter = new UserFilter(filterText);
        }
        return filter;
    }

}
