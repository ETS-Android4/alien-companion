package com.gDyejeekis.aliencompanion.models.filters;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.Serializable;

/**
 * Created by George on 6/21/2017.
 */

public class SubredditFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291403L;

    public static final String HEADER = "Subreddit filter";

    public SubredditFilter(String filterText) {
        super(filterText.replaceAll("\\s",""));
    }

    @Override
    public boolean isValid() {
        return GeneralUtils.isAlphaNumeric(filterText);
    }

    @Override
    public String getTextRequirements() {
        return "Subreddit must contain only alphanumeric characters";
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
