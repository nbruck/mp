package com.motorpast.base;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;


/**
 * only used for global message-properties providing
 * @author dgerhardt
 */
public class BaseRenderable
{
    @Inject
    private ComponentResources componentResources;

    @Inject
    private Messages messages;

    public String getPageTitle() {
        return messages.format("global.meta.title." + componentResources.getPageName().toLowerCase(), messages.get("global.application-name"));
    }
}
