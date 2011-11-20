package com.motorpast.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;

import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;

@UrlRewrite(
    mappings={
        "/de/errorpage", "/fehler"
        /*"/en/errorpage", "/error"*/
    },
    putInSitemap = false
)
public class ErrorPage extends BasePage
{
    @SessionState
    private UserSessionObj sessionObj;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @InjectPage
    private ResultPage resultPage;

    @Property(write = false)
    @PageActivationContext
    private String reason;


    public String getPageName() {
        return ErrorPage.class.getSimpleName();
    }


    @SetupRender
    void initErrorpage() {
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);

        if(sessionObj != null) {
            sessionObj.setMileageResultCaptcha(null);
            sessionObj.setTrustLevel(null);
            sessionObj.setUniqueToken(null);
            sessionObj.setTimestamp(0L);
        }
    }
}
