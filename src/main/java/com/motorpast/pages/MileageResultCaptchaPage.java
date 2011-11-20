package com.motorpast.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import com.motorpast.annotations.MileageResultCaptcha;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.captcha.CaptchaService;

/**
 * only renders a captcha in case of flasg are set und referer matches
 */
public class MileageResultCaptchaPage extends BasePage
{
    @SessionState
    private UserSessionObj sessionObj;

    @MileageResultCaptcha
    @Inject
    private CaptchaService mileageResultCaptchaService;

    @Inject
    private HttpServletResponse response;

    @Inject
    private HttpServletRequest request;

    @Inject
    private PageRenderLinkSource linkSource;


    @SetupRender
    void init() {
        if(isValidMileageResultRequest()) {
            mileageResultCaptchaService.createCaptcha(response);
        }
    }

    final private boolean isValidMileageResultRequest() {
        final String referer = (String)request.getHeader("referer");
        final String location = linkSource.createPageRenderLink(ResultPage.class).toAbsoluteURI();

        if(location.equals(referer) &&
              sessionObj != null && sessionObj.isMileageAvailable()
        ) {
            return true;
        } else {
            return false;
        }
    }
}
