package com.motorpast.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;

@UrlRewrite(
    mappings={
        "/de/error404", "/seite-nicht-gefunden"
    },
    putInSitemap = false
)
public class Error404 extends BasePage
{
    @Inject
    private PageRenderLinkSource pageRenderLinkSource;


    public String getMotorPageLink() {
        return pageRenderLinkSource.createPageRenderLink(Index.class).toURI();
    }


    public String getPageName() {
        return Error404.class.getSimpleName();
    }
}
