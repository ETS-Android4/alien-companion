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

    public abstract String getHeader();

    public String getFilterText() {
        return filterText;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getClass().isInstance(obj) && ((Filter) obj).getFilterText().equals(this.filterText);
    }

}
