package com.motorpast.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import com.motorpast.pages.Index;

@SuppressWarnings("unused")
public class BackToMainPage
{
    @Parameter(required = false, defaultPrefix = BindingConstants.LITERAL)
    @Property(write = false)
    private String message;

    @Parameter(required = false, defaultPrefix = BindingConstants.LITERAL)
    @Property(write = false)
    private String linktext;


    public String getMainPageName() {
        return Index.class.getSimpleName();
    }
}
