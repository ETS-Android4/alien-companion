package com.gDyejeekis.aliencompanion.models.filters;

import java.io.Serializable;

/**
 * Created by George on 6/21/2017.
 */

public class DomainFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291401L;

    public static final String HEADER = "Domain filter";

    public DomainFilter(String filterText) {
        super(filterText);
    }

    @Override
    public boolean match(String text) {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getTextRequirements() {
        return null;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

}
