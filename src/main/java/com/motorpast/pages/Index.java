package com.motorpast.pages;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.motorpast.additional.MotorPages;
import com.motorpast.additional.TextStreamResponseWithStatus;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.mixins.CheckRequestTime;
import com.motorpast.mixins.UniqueToken;
import com.motorpast.services.business.MotorpastBusinessException;

@UrlRewrite(
    mappings={
        "/de/", "/"/*,"/en/", "/en"*/
    },
    sitemap={
        "changefreq", "monthly",
        "priority", "1.0"
})
public class Index extends BasePage
{
    @SessionState(create = true)
    private UserSessionObj sessionObj;

    @Inject
    private Messages messages;

    @InjectPage
    private ResultPage resultPage;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @SessionAttribute(value = UniqueToken.UniqueSessionToken)
    private String sessionToken;

    @SessionAttribute(value = CheckRequestTime.TimeStampSessionToken)
    private String timeStamp;

    private boolean sessionObjExists;


    public String getPageName() {
        return Index.class.getSimpleName();
    }

    @Override
    public Class<?>[] getNavigation() {
        return MotorPages.getPagesForNavigation();
    }


    /**
     * catching all senseless urls for our application and sending 404
     */
    Object onActivate(EventContext eventContext) {
        if(eventContext != null && eventContext.getCount()  > 0) {
            return new TextStreamResponseWithStatus("text/html", messages.get("render.404"), HttpServletResponse.SC_NOT_FOUND);
        }

        return true;
    }

    @SetupRender
    void initIndexPage() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            sessionObj.testForCreation();
        }
        // reset stuff
        sessionToken = null;
        timeStamp = null;
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }

    public String getPageDescription() {
        return messages.format("page.description-task", messages.get("page.description.searchlink"));
    }
}
