package com.motorpast.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;

public class ToolTipComponent
{
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String helpText;

    @Parameter(required = false)
    private Object[] parameter;


    public String getToolTipText() {
        if(parameter != null && parameter.length > 0) {
            return String.format(helpText, parameter);
        } else {
            return helpText;
        }
    }
}
