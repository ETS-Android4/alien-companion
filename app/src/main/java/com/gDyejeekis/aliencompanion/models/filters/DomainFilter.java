package com.gDyejeekis.aliencompanion.models.filters;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.Serializable;

/**
 * Created by George on 6/21/2017.
 */

public class DomainFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291401L;

    public static final String HEADER = "Domain filter";

    public DomainFilter(String filterText) {
        super(filterText.replaceAll("\\s",""));
    }

    @Override
    public boolean isValid() {
        return GeneralUtils.isValidDomain(filterText);
    }

    @Override
    public String getTextRequirements() {
        return "Text must be a valid domain";
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

}
