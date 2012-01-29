package com.motorpast.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.motorpast.base.BasePage;

//@Import(stylesheet = {"context:css/mainLayout.css"})
public class MainLayout
{
    @InjectContainer
    private BasePage basePage;

    @Inject
    private ComponentResources componentResources;

    @Inject
    private Messages messages;

    @Inject
    @Path(value="context:favicon.ico")
    private Asset favicon;

    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;

    @Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String description;

    @Property
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String keywords;

    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String robot;

    @Parameter(required = false)
    private Class<?>[] navigation;

    @Property
    @Parameter(required = false, defaultPrefix = BindingConstants.LITERAL)
    private Block headerBlock, leftBlock, rightBlock, footerBlock;

    @Property
    private String currentNavigationEntry;

    @Property
    private int navCounter;


    public String getOtherPageText() {
        return messages.get(componentResources.getPageName());
    }

    public String getFavicon() {
        return favicon.toClientURL();
    }

    public String[] getNavigation() {
        if(navigation == null) {
            return null;
        }

        String[] urls = new String [navigation.length];

        for(int i = 0; i < navigation.length; i++) {
            urls[i] = navigation[i].getSimpleName();
        }

        return urls;
    }

    public boolean isActivePage() {
        return basePage.getPageName().equals(currentNavigationEntry);
    }

    public String getCurrentNavigationLinkText() {
        return messages.get("page." + currentNavigationEntry);
    }

    public String getAdditionalStyleClass() {
        if(navCounter == navigation.length -1) {
            return "navlast";
        } else {
            return "";
        }
    }
}
