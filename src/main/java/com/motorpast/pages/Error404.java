package com.motorpast.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SetupRender;

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
    @InjectPage
    private ConfirmationPage confirmationPage;

    @InjectPage
    private ResultPage resultPage;


    public String getPageName() {
        return Error404.class.getSimpleName();
    }


    @SetupRender
    void init() {
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }
}
