package com.gDyejeekis.aliencompanion.models.filters;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by George on 6/21/2017.
 */

public class TitleFilter extends Filter implements Serializable {

    private static final long serialVersionUID = 924291404L;

    public static final String HEADER = "Title filter";

    public TitleFilter(String filterText) {
        super(filterText);
    }

    @Override
    public boolean isValid() {
        return GeneralUtils.containsAlphaNumeric(filterText);
    }

    @Override
    public String getTextRequirements() {
        return "Text must contain alphanumeric characters";
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

}
