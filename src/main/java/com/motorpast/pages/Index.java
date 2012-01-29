package com.motorpast.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import com.motorpast.additional.HttpStatusCode;
import com.motorpast.additional.MotorPages;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.MotorpastBusinessException;

@UrlRewrite(
    mappings={
        "/de/", "/"/*,"/en/", "/en"*/
    },
    sitemap={
        "changefreq", "monthly",
        "priority", "1.0"
})
//@Import(stylesheet = {"context:css/index.css"})
public class Index extends BasePage
{
    @SessionState(create = true)
    private UserSessionObj sessionObj;

    @Inject
    private Response response;

    @Inject
    private PageRenderLinkSource pageRenderLinkSource;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @InjectPage
    private ResultPage resultPage;

    @InjectPage
    private ConfirmationPage confirmationPage;

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
            /*
            try {
                response.sendError(404, "not found");
            } catch (IOException e) {
                logger.error("ioException while try sending error 404");
            }
            */
            response.disableCompression();
            return new HttpStatusCode(HttpServletResponse.SC_NOT_FOUND, pageRenderLinkSource.createPageRenderLink(Error404.class).toAbsoluteURI()) ;
        }
        return true;
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

    public String getPageDescription() {
        return messages.format("page.description-task", messages.get("page.description.searchlink"));
    }
}
