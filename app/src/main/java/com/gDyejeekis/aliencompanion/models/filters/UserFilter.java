package com.gDyejeekis.aliencompanion.models.filters;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.Serializable;

/**
 * Created by George on 6/21/2017.
 */

public class UserFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291402L;

    public static final String HEADER = "User filter";

    public UserFilter(String filterText) {
        super(filterText.replaceAll("\\s",""));
    }

    @Override
    public boolean isValid() {
        return GeneralUtils.isAlphaNumeric(filterText);
    }

    @Override
    public String getTextRequirements() {
        return "User must contain only alphanumeric characters";
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public boolean match(String text) {
        return text.equalsIgnoreCase(filterText);
    }

}
