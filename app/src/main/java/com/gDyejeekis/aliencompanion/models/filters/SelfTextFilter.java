package com.gDyejeekis.aliencompanion.models.filters;

import java.io.Serializable;

/**
 * Created by George on 6/21/2017.
 */

public class SelfTextFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291405L;

    public static final String HEADER = "Self-text filter";

    public SelfTextFilter(String filterText) {
        super(filterText);
    }

    @Override
    public boolean match(String text) {
        return false;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

}
