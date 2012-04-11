package com.motorpast.base;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.motorpast.additional.MotorApplicationConstants;


/**
 * only used for global message-properties providing
 * @author dgerhardt
 */
public class BaseRenderable
{
    @Inject @Symbol(MotorApplicationConstants.AppBrandName)
    private String AppBrandName;

    @Inject
    private ComponentResources componentResources;

    @Inject
    private Messages messages;

    public String getPageTitle() {
        return messages.format("global.meta.title." + componentResources.getPageName().toLowerCase(), AppBrandName);
    }

    public final String getAppBrandName() {
        return AppBrandName;
    }
}
