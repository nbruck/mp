package com.motorpast.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import com.motorpast.additional.MotorPages;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.security.SecurityService;

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
    private Response response;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Inject
    private SecurityService securityService;

    @InjectPage
    private ResultPage resultPage;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @Property
    private String token;

    @Property
    private long date;

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
    void onActivate(EventContext eventContext) {
        if(eventContext != null && eventContext.getCount()  > 0) {
            try {
                response.sendError(404, "not found");
            } catch (IOException e) {
                logger.error("ioException while try sending error 404");
            }
        }
    }

    @SetupRender
    void initIndexPage() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            sessionObj.testForCreation();
        }
        // reset stuff
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }

    void beginRender() {
        final String token = securityService.generateToken(httpServletRequest);
        this.token = token;
        sessionObj.setUniqueToken(token);

        final long timestamp = securityService.generateTimestamp();
        this.date = timestamp;
        sessionObj.setTimestamp(timestamp);
    }

    public String getPageDescription() {
        return messages.format("page.description-task", messages.get("page.description.searchlink"));
    }
}
