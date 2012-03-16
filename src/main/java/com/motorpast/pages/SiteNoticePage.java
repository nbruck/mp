package com.motorpast.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.motorpast.additional.MotorPages;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;

@UrlRewrite(
    mappings={
        "/de/sitenoticepage", "/impressum" /*"/en/sitenoticepage", "/site-notice"*/
    },
    sitemap={
        "changefreq", "monthly",
        "priority", "0.5"
    }
)
public class SiteNoticePage extends BasePage
{
    @Inject
    private Messages messages;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @InjectPage
    private ResultPage resultPage;


    public String getPageName() {
        return SiteNoticePage.class.getSimpleName();
    }

    @Override
    public Class<?>[] getNavigation() {
        return MotorPages.getPagesForNavigation();
    }

    @Override
    public String getRobotText() {
        return messages.get("page.meta.robot.no-index");
    }

    public String getGuarantee() {
        return messages.format("text.site.guarantee", messages.get("global.application-name"), messages.get("global.application-name"));
    }

    @SetupRender
    void init() {
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }
}
