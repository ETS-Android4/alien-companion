package com.gDyejeekis.aliencompanion.api.utils;

public class ParamFormatter {
    
    /**
     * Add a parameter to the parameter list that is appended to a URL.
     * Precondition: current list of parameters is not null.
     * Postcondition: if the value is not a default value (null, empty or -1) it is added to the parameter list.
     * 
     * @param params 	Current parameter list
     * @param name 		Parameter name
     * @param value 	Parameter value
     * 
     * @return 			New parameter list
     */
    public static String addParameter(String params, String name, String value) {
    	assert(params != null);
    	
    	if (value != null && !value.equals("") && !value.equals("-1")) {
    		return params.concat("&" + name + "=" + value);
    	} else {
    		return params;
    	}
    }

	/**
	 * Get the value of a parameter from a parameters string
	 *
	 * @param params parameters string
	 * @param name parameter name
	 * @return parameter value, null if parameter isn't found
	 */
	public static String getParameterValue(String params, String name) {
		assert params != null;

		try {
			int nameStart = params.indexOf(name);
			if (nameStart == -1)
				return null;
			String substring = params.substring(nameStart + name.length() + 1);
			int valueEnd = substring.indexOf('&');
			if (valueEnd == -1) valueEnd = substring.length();
			return substring.substring(0, valueEnd);
		} catch (Exception e) {}
		return null;
	}
    
}
